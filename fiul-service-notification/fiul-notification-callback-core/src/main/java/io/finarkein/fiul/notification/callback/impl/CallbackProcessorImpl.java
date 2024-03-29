/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback.impl;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.ConsentStatusNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.notification.FIStatusNotification;
import io.finarkein.fiul.notification.callback.CallbackProcessor;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import io.finarkein.fiul.notification.callback.model.ConsentCallback;
import io.finarkein.fiul.notification.callback.model.ConsentWebhook;
import io.finarkein.fiul.notification.callback.model.FICallback;
import io.netty.handler.ssl.SslHandshakeTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.finarkein.api.aa.common.ConsentStateMachine.*;
import static io.finarkein.fiul.notification.callback.CallbackWebClientConfig.WEBHOOK_QUALIFIER_SUPPLIER_METHOD;


@Log4j2
@Service
public class CallbackProcessorImpl implements CallbackProcessor {
    protected static final Set<String> CONSENT_NEGATIVE_STATUS = Set.of(PAUSED.name(), EXPIRED.name(), REVOKED.name());
    protected static final Set<String> CONSENT_CLEANUP_STATUS = Set.of("REJECTED", EXPIRED.name(), REVOKED.name());
    protected static final Set<String> FI_NEGATIVE_STATUS = Set.of("COMPLETED", "EXPIRED", "FAILED");

    protected final CallbackRegistry registry;
    protected final WebClient webClient;

    @Value("${consent.callback.retry.count:3}")
    private int consentCallbackRetryCount;

    @Value("${consent.callback.timeout.in.seconds:10}")
    private int consentCallbackTimeoutInSeconds;

    @Value("${consent.callback.retry.delay.seconds:5}")
    private int consentCallbackRetryDelayInSeconds;

    @Autowired
    protected CallbackProcessorImpl(@Qualifier(WEBHOOK_QUALIFIER_SUPPLIER_METHOD) WebClient webClient,
                                    CallbackRegistry registry) {
        this.webClient = webClient;
        this.registry = registry;
    }

    public void handleConsentNotification(ConsentNotification notification) {
        doHandleConsentNotification(notification);
    }

    public void handleFINotification(FINotification notification) {
        doHandleFINotification(notification);
    }

    protected void doHandleConsentNotification(ConsentNotification notification) {
        log.debug("ConsentNotification received:{}", notification);
        ConsentStatusNotification statusNotification = notification.getConsentStatusNotification();
        String consentStatus = statusNotification.getConsentStatus();
        String consentHandleId = statusNotification.getConsentHandle();

        ConsentCallback callback = registry.consentCallback(consentHandleId);
        boolean doCleanup = false;
        if (Objects.nonNull(callback)) {
            statusNotification.setConsentHandle(consentHandleId);
            statusNotification.setRunId(callback.getRunId());
            statusNotification.setAaId(callback.getAaId());
            statusNotification.setAddOnParams(callback.getAddOnParams());
            handleConsentCallback(statusNotification, callback.getCallbackUrl());
            doCleanup = true;
        } else
            log.debug("no callback registered for consentNotification, consentHandle:{}", statusNotification.getConsentHandle());
        final List<ConsentWebhook> consentWebhooks = registry.consentWebhooks(statusNotification.getConsentHandle());
        if (consentWebhooks != null && !consentWebhooks.isEmpty()) {
            handleConsentWebhooks(statusNotification, consentWebhooks);
            doCleanup = true;
        } else
            log.debug("no webhook registered for consentNotification, consentHandle:{}", statusNotification.getConsentHandle());

        if (!doCleanup)
            return;

        if (CONSENT_NEGATIVE_STATUS.contains(consentStatus)) {
            // Clean up FI callbacks
            try {
                registry.deleteFICallbackByConsentId(consentHandleId);
            } catch (Exception e) {
                log.error("Failed to clean up FI callbacks for consentHandleId: {}", consentHandleId, e);
            }
        }
        if (CONSENT_CLEANUP_STATUS.contains(consentStatus)) {
            // Clean up Consent callbacks
            try {
                registry.deleteConsentCallback(consentHandleId);
            } catch (Exception e) {
                log.error("Failed to clean up FI callbacks for consentHandleId: {}", consentHandleId, e);
            }
        }
        log.debug("ConsentNotification handling done, notification:{}", notification);
    }

    private boolean handleConsentCallback(ConsentStatusNotification statusNotification, String callbackUrl) {

        try {
            // post consent HANDLE - CALL BACK URL
            // call back web client send finarkein creds
            // call back revoke on ACTIVE, PENDING, PAUSED, REJECTED, EXPIRED, REVOKED
            log.debug("Calling ConsentCallback on notification:{}, callback:{}", statusNotification, callbackUrl);
            processCallback(statusNotification, callbackUrl).subscribe();
        } catch (Exception e) {
            log.error("Error while calling ConsentCallback on notification:{}, callback-url:{}", statusNotification,
                    callbackUrl, e);
        }
        return false;
    }

    private Mono<String> processCallback(ConsentStatusNotification statusNotification, String callbackUrl) {
        return configureRetry(webClient.post()
                        .uri(callbackUrl)
                        .body(Mono.just(statusNotification), ConsentStatusNotification.class)
                        .retrieve()
                        .onStatus(
                                status -> status.is5xxServerError() || status.value() == 408 || status.value() == 429,
                                response -> {
                                    String message = String.format("Error %d-%s, while sending consent status " +
                                                    "notification, object is '%s'",
                                            response.rawStatusCode(), response.statusCode().getReasonPhrase(),
                                            statusNotification);
                                    return Mono.error(new CallbackServiceException(message));
                                })
                        .bodyToMono(String.class)
                , statusNotification, callbackUrl);
    }

    protected void doHandleFINotification(FINotification notification) {
        log.debug("handleFINotification: notification:{}", notification);
        FIStatusNotification statusNotification = notification.getFIStatusNotification();
        String sessionId = statusNotification.getSessionId();
        FICallback callback = registry.fiCallback(sessionId);
        if (Objects.isNull(callback))
            return;

        // Trigger callback!
        try {
            log.debug("Calling FICallback on notification:{}, callback-url:{}", statusNotification,
                    callback.getCallbackUrl());
            webClient.post()
                    .uri(callback.getCallbackUrl())
                    .body(Mono.just(statusNotification), FINotification.class)
                    .exchange()
                    .block();
        } catch (Exception e) {
            log.error("Error while calling FICallback on notification:{}, callback-url:{}", statusNotification,
                    callback.getCallbackUrl(), e);
        }

        // ACTIVE, COMPLETED, EXPIRED, FAILED
        if (FI_NEGATIVE_STATUS.contains(statusNotification.getSessionStatus())) {
            try {
                registry.deleteFICallbacksBySession(sessionId);
            } catch (Exception e) {
                log.error("Failed to clean up FI callbacks for sessionId: {}", sessionId, e);
            }
        }
        log.debug("FINotification handling done, notification:{}", notification);
    }

    private Throwable logAndCreateException(String message, Throwable error) {
        log.debug("Encountered '{}' error while calling external callback api : ", message, error);
        return new CallbackServiceException(message);
    }

    static class CallbackServiceException extends Exception {
        CallbackServiceException(String message) {
            super(message);
        }
    }

    private boolean handleConsentWebhooks(ConsentStatusNotification statusNotification, List<ConsentWebhook> consentWebhooks) {

        final List<String> webhookUrls = consentWebhooks.stream()
                .map(ConsentWebhook::getCallbackUrl)
                .collect(Collectors.toList());
        try {
            log.debug("Calling ConsentWebhook on ConsentNotification:{}, webhookUrls:{}", statusNotification, webhookUrls);
            Flux.fromIterable(consentWebhooks)
                    .map(consentWebhook -> {
                        ConsentStatusNotification notification = new ConsentStatusNotification();
                        notification.setConsentHandle(consentWebhook.getConsentHandle());
                        notification.setConsentId(statusNotification.getConsentId());
                        notification.setAaId(consentWebhook.getAaId());
                        notification.setRunId(consentWebhook.getRunId());
                        notification.setAddOnParams(consentWebhook.getAddOnParams());
                        notification.setConsentStatus(statusNotification.getConsentStatus());
                        return Tuples.of(consentWebhook, notification);
                    })
                    .publishOn(Schedulers.boundedElastic())
                    .flatMap(objects -> processCallback(objects.getT2(), objects.getT1().getCallbackUrl()))
                    .collectList()
                    .map(responses -> {
                        log.debug("webhook responses:{}", responses);
                        return responses;
                    })
                    .subscribe()
            ;
        } catch (Exception e) {
            log.error("Error while calling ConsentWebhook on notification:{}, urls:{}", statusNotification,
                    webhookUrls, e);
        }
        return true;
    }

    public <T> Mono<T> configureRetry(Mono<T> inputMono, Object statusNotification, String url) {
        return inputMono
                .timeout(Duration.ofSeconds(consentCallbackTimeoutInSeconds))
                .onErrorMap(java.util.concurrent.TimeoutException.class, ex -> logAndCreateException("java.util.concurrent.TimeoutException", ex))
                .onErrorMap(TimeoutException.class, ex -> logAndCreateException("Channel TimeoutException", ex))
                .onErrorMap(SslHandshakeTimeoutException.class, ex -> logAndCreateException("SslHandshakeTimeout", ex))
                .onErrorMap(WebClientRequestException.class, ex -> logAndCreateException("WebClientRequestException", ex))

                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(string -> log.debug("Notification sent Successfully {} :" +
                        " response string is '{}'", statusNotification, string))
                .doOnError(error -> log.error("Failed to send notification {}, error:{}", statusNotification, error.getMessage(), error))
                .retryWhen(
                        Retry.backoff(consentCallbackRetryCount, Duration.ofSeconds(consentCallbackRetryDelayInSeconds))
                                .jitter(0.6)
                                .filter(throwable -> throwable instanceof CallbackServiceException)
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                        new IllegalStateException(
                                                "Failed to send consent notification to '" + url + "'" +
                                                        " after " + retrySignal.totalRetries() + " attempts, notification object : " + statusNotification)));
    }
}
