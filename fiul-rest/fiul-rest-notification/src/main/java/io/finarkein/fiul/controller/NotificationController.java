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
import io.finarkein.fiul.common.RequestUpdater;
import io.finarkein.fiul.notification.NotificationPublisher;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
            log.debug("NotificationPublisher.publish(consentNotification) in-progress");
            publisher.publishConsentNotification(consentNotification);
            log.debug("NotificationPublisher.publish(consentNotification) done");
        } catch (Exception e) {
            log.error("Error while publishing ConsentNotification for callback handling:{}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        var response = NotificationResponse.okResponse(consentNotification.getTxnid(),
                RequestUpdater.TimestampUpdaters.currentTimestamp());
        return Mono.just(response);
    }

    @PostMapping("/FI/Notification")
    public Mono<NotificationResponse> fiNotification(@RequestBody FINotification fiNotification) {
        log.debug("FINotification received:{}", fiNotification);

        try {
            publisher.publishFINotification(fiNotification);
        } catch (Exception e) {
            log.error("Error while publishing fiNotification callback:{}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        return Mono.just(NotificationResponse.okResponse(fiNotification.getTxnid(), RequestUpdater.TimestampUpdaters.currentTimestamp()));
    }
}
