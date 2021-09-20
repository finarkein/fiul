/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;


import io.finarkein.aa.registry.RegistryService;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.notification.NotificationResponse;
import io.finarkein.fiul.consent.model.ConsentState;
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

@RestController
@RequestMapping("/")
@Log4j2
public class NotificationController {

    private final NotificationPublisher publisher;

    private final ConsentService consentService;

    private final RegistryService registryService;

    @Autowired
    public NotificationController(NotificationPublisher publisher, ConsentService consentService,
                                  RegistryService registryService) {
        this.publisher = publisher;
        this.consentService = consentService;
        this.registryService = registryService;
    }

    @PostMapping("/Consent/Notification")
    public ResponseEntity<Mono<NotificationResponse>> consentResponseMono(@RequestBody ConsentNotification consentNotification,
                                                                          @RequestHeader("x-jws-signature") String jwsSignature) {
        validateJWS(consentNotification.getTxnid(), jwsSignature);
        if (!NotificationValidator.isValidUUID(consentNotification.getTxnid())) {
            return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()), "Invalid TxnId")));
        }
        ConsentState consentState = consentService.getConsentStateByTxnId(consentNotification.getTxnid());
        if (consentState == null)
            consentState = consentService.getConsentStateByConsentHandle(consentNotification.getConsentStatusNotification().getConsentHandle());
        if (consentState != null) {
            try {
                NotificationValidator.validateConsentNotification(consentNotification, consentState,
                        registryService.getEntityInfoByAAName(consentState.getAaId()));
            } catch (SystemException e) {
                if (e.errorCode().httpStatusCode() == 404)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(NotificationResponse.notFoundResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
                return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(consentNotification.getTxnid(),
                        Timestamp.from(Instant.now()), e.getMessage())));
            }
        }

        log.debug("ConsentNotification received:{}", consentNotification);
        try {
            publisher.publishConsentNotification(consentNotification);
            log.debug("NotificationPublisher.publish(consentNotification) done");
        } catch (Exception e) {
            log.error("Error while publishing ConsentNotification for handling:{}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        return ResponseEntity.ok().body(Mono.just(NotificationResponse.okResponse(consentNotification.getTxnid(),
                Timestamp.from(Instant.now()))));
    }

    @PostMapping("/FI/Notification")
    public ResponseEntity<Mono<NotificationResponse>> fiNotification(@RequestBody FINotification fiNotification, @RequestHeader("x-jws-signature") String jwsSignature) {
        log.debug("FINotification received:{}", fiNotification);
        validateJWS(fiNotification.getTxnid(), jwsSignature);
        if (!NotificationValidator.isValidUUID(fiNotification.getTxnid())) {
            return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(fiNotification.getTxnid(),
                    Timestamp.from(Instant.now()), "Invalid TxnId")));
        }
        ConsentState consentState = consentService.getConsentStateByTxnId(fiNotification.getTxnid());
        if (consentState != null) {
            try {
                NotificationValidator.validateFINotification(fiNotification, consentState, registryService.getEntityInfoByAAName(consentState.getAaId()));
            } catch (SystemException e) {
                if (e.errorCode().httpStatusCode() == 404)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(NotificationResponse.notFoundResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
                return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
            }
        }
        try {
            publisher.publishFINotification(fiNotification);
            log.debug("FINotification.publish(fiNotification) done");
        } catch (Exception e) {
            log.error("Error while publishing fiNotification for handling:{}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        return ResponseEntity.ok(Mono.just(NotificationResponse.okResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now()))));
    }

    private static void validateJWS(String txnId, String jwsSignature) {
        if (jwsSignature == null)
            throw Errors.InvalidRequest.with(txnId, "Null JWS Signature");
        if (jwsSignature.split("\\.").length != 3)
            throw Errors.InvalidRequest.with(txnId, "Invalid JWS Signature");
    }
}
