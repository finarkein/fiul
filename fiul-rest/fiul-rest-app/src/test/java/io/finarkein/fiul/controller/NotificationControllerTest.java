/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.exception.Error;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.notification.NotificationResponse;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.time.Duration;

import static io.finarkein.fiul.TestValues.*;

@Import(NotificationController.class)
@ContextConfiguration(classes = {TestConfig.class, ControllerAdvice.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = NotificationController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Log4j2
class NotificationControllerTest {
    @Autowired
    private WebTestClient webClient;

    @Value("${server.port}")
    private int port;

    @BeforeEach
    public void setUp() {
        webClient = webClient
                .mutate()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("Content-Type", "application/json")
                .filter(logRequest())
                .responseTimeout(Duration.ofMillis(300000))
                .build();
    }


    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            if (log.isDebugEnabled())
                log.debug("Calling: {} {}, headers: {}", clientRequest.method(), clientRequest.url(), clientRequest.headers().entrySet());
            return next.exchange(clientRequest);
        };
    }

    @Test()
    @DisplayName("Testing Consent Notification")
    void consentNotificationTest() {
        ConsentNotification consentNotification = getConsentNotification();
        final EntityExchangeResult<NotificationResponse> result = webClient.post().uri("/Consent/Notification")
                .bodyValue(consentNotification)
                .exchange()
                .expectStatus().isOk()
                .expectBody(NotificationResponse.class)
                .returnResult();
        Assertions.assertEquals(result.getResponseBody().getTxnId(), getNotificationResponse().getTxnId());
    }

    @Test()
    @DisplayName("Testing Consent Notification with Error")
    void consentNotificationCallbackTest() {
        ConsentNotification consentNotification = getConsentNotifiCallback();
        final EntityExchangeResult<Error> result = webClient.post().uri("/Consent/Notification")
                .bodyValue(consentNotification)
                .exchange()
                .expectBody(Error.class)
                .returnResult();
        System.out.println(result.getResponseBody());
    }

    @Test()
    @DisplayName("Testing FI Notification")
    void FINotificationTest() {
        FINotification fiNotification = fiNotificationRequest();

        final EntityExchangeResult<NotificationResponse> result = webClient.post().uri("/FI/Notification")
                .bodyValue(fiNotificationRequest())
                .exchange()
                .expectStatus().isOk()
                .expectBody(NotificationResponse.class)
                .returnResult();
        Assertions.assertEquals(result.getResponseBody().getTxnId(), getNotificationResponse().getTxnId());
    }

    @Test()
    @DisplayName("Test FINotification Error")
    void FINotificationError() {
        final EntityExchangeResult<Error> result = webClient.post().uri("/FI/Notification")
                .bodyValue(fiNotificationCallBack())
                .exchange()
                .expectBody(Error.class)
                .returnResult();
        System.out.println(result.getResponseBody());
    }

}
