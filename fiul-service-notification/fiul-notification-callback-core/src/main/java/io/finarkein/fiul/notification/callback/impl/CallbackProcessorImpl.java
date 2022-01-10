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
import io.finarkein.fiul.notification.callback.model.FICallback;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Set;

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

    protected void doHandleConsentNotification(ConsentNotification notification){
        log.debug("ConsentNotification received:{}", notification);
        ConsentStatusNotification statusNotification = notification.getConsentStatusNotification();
        String consentHandleId = statusNotification.getConsentHandle();
        ConsentCallback callback = registry.consentCallback(consentHandleId);
        if (Objects.isNull(callback))
            return;

        // post consent HANDLE - CALL BACK URL
        // call back web client send finarkein creds
        // call back revoke on ACTIVE, PENDING, PAUSED, REJECTED, EXPIRED, REVOKED
        String consentStatus = statusNotification.getConsentStatus();
        try {
            log.debug("Calling ConsentCallback on notification:{}, callback-url:{}", statusNotification,
                    callback.getCallbackUrl());
            webClient.post()
                    .uri(callback.getCallbackUrl())
                    .body(Mono.just(statusNotification), ConsentStatusNotification.class)
                    .exchange().block();
        } catch (Exception e) {
            log.error("Error while calling ConsentCallback on notification:{}, callback-url:{}", statusNotification,
                    callback.getCallbackUrl(), e);
        }

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

    protected void doHandleFINotification(FINotification notification){
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
                    .exchange().block();
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
}
