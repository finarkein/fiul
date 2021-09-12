/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;


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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/")
@Log4j2
public class NotificationController {


    private final NotificationPublisher publisher;

    private final ConsentService consentService;

    @Autowired
    public NotificationController(NotificationPublisher publisher, ConsentService consentService) {
        this.publisher = publisher;
        this.consentService = consentService;
    }

    @PostMapping("/Consent/Notification")
    public ResponseEntity<Mono<NotificationResponse>> consentResponseMono(@RequestBody ConsentNotification consentNotification) {
        ConsentState consentState = consentService.getConsentStateByTxnId(consentNotification.getTxnid());
        if (consentState == null)
            consentState = consentService.getConsentStateByConsentHandle(consentNotification.getConsentStatusNotification().getConsentHandle());
        if (consentState != null) {
            if (consentState.getNotifierId() == null || consentState.getConsentId() == null) {
                consentState.setNotifierId(consentNotification.getNotifier().getId());
                consentState.setConsentId(consentNotification.getConsentStatusNotification().getConsentId());
                consentService.updateConsentState(consentState);
            }
            try {
                NotificationValidator.validateConsentNotification(consentNotification, consentState);
            } catch (SystemException e) {
                if (e.errorCode().httpStatusCode() == 404)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(NotificationResponse.notFoundResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
                return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
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

        return ResponseEntity.ok().body(Mono.just(NotificationResponse.okResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()))));
    }

    @PostMapping("/FI/Notification")
    public ResponseEntity<Mono<NotificationResponse>> fiNotification(@RequestBody FINotification fiNotification) {
        log.debug("FINotification received:{}", fiNotification);

        try {
            NotificationValidator.validateFINotification(fiNotification, consentService.getConsentStateByTxnId(fiNotification.getTxnid()));
        } catch (SystemException e) {
            if (e.errorCode().httpStatusCode() == 404)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(NotificationResponse.notFoundResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
            return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
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
}
