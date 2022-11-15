/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.aa.registry.RegistryService;
import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.notification.NotificationResponse;
import io.finarkein.fiul.config.model.AaApiKeyBody;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.notification.NotificationPublisher;
import io.finarkein.fiul.validator.NotificationValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static io.finarkein.fiul.controller.DefaultFINotificationProcessor.DEFAULT_PROCESSOR_NAME;

@RestController
@RequestMapping("/")
@Log4j2
public class NotificationController {

    private final NotificationPublisher publisher;

    private final ConsentService consentService;

    private final RegistryService registryService;

    private final Base64.Decoder decoder = Base64.getDecoder();

    private final ObjectMapper objectMapper;

    private final Map<String, FINotificationProcessor> applicableProcessors;

    @Autowired
    public NotificationController(NotificationPublisher publisher, ConsentService consentService,
                                  RegistryService registryService,
                                  List<FINotificationProcessor> fiNotificationProcessors,
                                  ObjectMapper mapper) {
        this.publisher = publisher;
        this.consentService = consentService;
        this.registryService = registryService;
        this.objectMapper = mapper;

        Map<String, FINotificationProcessor> processorMap = new HashMap<>();
        for (FINotificationProcessor processor : fiNotificationProcessors) {
            processor.applicableEntities()
                    .forEach(entity -> processorMap.put(entity, processor));
        }
        applicableProcessors = Collections.unmodifiableMap(processorMap);
        log.debug("applicableProcessorsMap: {}", applicableProcessors);
    }

    @PostMapping("/Consent/Notification")
    public Mono<ResponseEntity<NotificationResponse>> consentResponseMono(@RequestBody ConsentNotification consentNotification,
                                                                          @RequestHeader("aa_api_key") String aaApiKey) {
        log.debug("ConsentNotification received:{}", consentNotification);

        String[] chunks = aaApiKey.split("\\.");

        String payload = new String(decoder.decode(chunks[1]));
        AaApiKeyBody aaApiKeyBody = null;
        try {
            aaApiKeyBody = objectMapper.readValue(payload, AaApiKeyBody.class);
        } catch (JsonProcessingException e) {
            return Mono.just(ResponseEntity.badRequest().body(NotificationResponse
                    .invalidResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
        }

        ArgsValidator.isValidUUID(consentNotification.getTxnid(), consentNotification.getTxnid(), "TxnId");
        final String consentHandle = consentNotification.getConsentStatusNotification().getConsentHandle();
        ConsentStateDTO consentStateDTO;
        if (consentHandle != null)
            consentStateDTO = consentService.getConsentStateByConsentHandle(consentHandle);
        else
            consentStateDTO = consentService.getConsentStateByConsentId(consentNotification.getConsentStatusNotification()
                    .getConsentId());

        if (consentStateDTO == null)
            consentStateDTO = consentService.getConsentStateByTxnId(consentNotification.getTxnid());

        if (consentStateDTO != null) {
            try {
                log.debug("{}: Validating ConsentNotification", consentHandle);
                NotificationValidator.validateConsentNotification(consentNotification, consentStateDTO,
                        registryService.getEntityInfoByAAName(consentStateDTO.getAaId()), aaApiKeyBody);

                if (consentStateDTO.getConsentHandleStatus() != null
                        && consentStateDTO.getConsentHandleStatus().equalsIgnoreCase("FAILED")
                        && consentNotification.getConsentStatusNotification().getConsentStatus().equalsIgnoreCase("ACTIVE")) {
                    log.error("{} : Invalid notification, consentHandle status is FAILED and consent notification" +
                            "status is active", consentHandle);
                    return Mono.just(ResponseEntity.badRequest().body(NotificationResponse.invalidResponse(consentNotification.getTxnid(),
                            Timestamp.from(Instant.now()), "Invalid Request")));
                }
                log.debug("{}: ConsentNotification.publishing (consentNotification)", consentHandle);
                publisher.publishConsentNotification(consentNotification);
                log.debug("{}: NotificationPublisher.publish(consentNotification) done", consentHandle);
                return Mono.just(ResponseEntity.ok(NotificationResponse.okResponse(consentNotification.getTxnid(),
                        Timestamp.from(Instant.now()))));
//                Mono<SignedConsentDTO> signedConsent = consentService
//                        .getSignedConsent(consentNotification.getConsentStatusNotification().getConsentId(),
//                                Optional.of(consentStateDTO.getAaId()));
//                return signedConsent
//                        .map(signedConsentDTO -> {
//                            log.debug("{}: ConsentNotification.publishing (consentNotification)", consentHandle);
//                            publisher.publishConsentNotification(consentNotification);
//                            log.debug("{}: NotificationPublisher.publish(consentNotification) done", consentHandle);
//                            return NotificationResponse.okResponse(consentNotification.getTxnid(),
//                                    Timestamp.from(Instant.now()));
//                        })
//                        .map(notificationResponseMono -> ResponseEntity.ok().body(notificationResponseMono))
//                        .onErrorResume(throwable -> Mono.just(ResponseEntity.badRequest()
//                                .body(NotificationResponse.invalidResponse(consentNotification.getTxnid(),
//                                        Timestamp.from(Instant.now()), "Invalid Request"))))
//                        ;
            } catch (SystemException e) {
                log.error("Error while processing ConsentNotification:{}, error:{}", consentNotification, e.getMessage(), e);
                if (e.errorCode().httpStatusCode() == 404)
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(NotificationResponse.notFoundResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
                return Mono.just(ResponseEntity.badRequest().body(NotificationResponse.invalidResponse(consentNotification.getTxnid(),
                        Timestamp.from(Instant.now()), e.getMessage())));
            } catch (Exception e) {
                log.error("Error while processing ConsentNotification:{}, error:{}", consentNotification, e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        }
        log.debug("{}: For ConsentNotification consentState not found returning invalid request", consentHandle);
        return Mono.just(ResponseEntity.badRequest().body(NotificationResponse.invalidResponse(consentNotification.getTxnid(),
                Timestamp.from(Instant.now()), "Invalid Request")));
    }

    @PostMapping("/FI/Notification")
    public ResponseEntity<Mono<NotificationResponse>> fiNotification(@RequestBody FINotification fiNotification,
                                                                     @RequestHeader("aa_api_key") String aaApiKey) {
        String processorName = DEFAULT_PROCESSOR_NAME;

        if (fiNotification.getNotifier() != null && fiNotification.getNotifier().getId() != null)
            processorName = fiNotification.getNotifier().getId();

        FINotificationProcessor processor = applicableProcessors.get(processorName);
        if (processor == null) {
            processorName = DEFAULT_PROCESSOR_NAME;
            processor = applicableProcessors.get(processorName);
        }

        log.debug("FINotification processorName:{}, className:{}", processorName, processor.getClass().getName());
        return processor.process(fiNotification, aaApiKey);
    }
}
