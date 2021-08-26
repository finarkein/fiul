/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;


import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.notification.NotificationResponse;
import io.finarkein.fiul.notification.NotificationPublisher;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;

@RestController
@RequestMapping("/")
@Log4j2
public class NotificationController {

    private final NotificationPublisher publisher;

    @Autowired
    public NotificationController(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/Consent/Notification")
    public Mono<NotificationResponse> consentResponseMono(@RequestBody ConsentNotification consentNotification) {
        log.debug("ConsentNotification received:{}", consentNotification);
        try {
            publisher.publishConsentNotification(consentNotification);
            log.debug("NotificationPublisher.publish(consentNotification) done");
        } catch (Exception e) {
            log.error("Error while publishing ConsentNotification for handling:{}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        return Mono.just(NotificationResponse.okResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now())));
    }

    @PostMapping("/FI/Notification")
    public Mono<NotificationResponse> fiNotification(@RequestBody FINotification fiNotification) {
        log.debug("FINotification received:{}", fiNotification);

        try {
            publisher.publishFINotification(fiNotification);
            log.debug("FINotification.publish(fiNotification) done");
        } catch (Exception e) {
            log.error("Error while publishing fiNotification for handling:{}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        return Mono.just(NotificationResponse.okResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now())));
    }
}
