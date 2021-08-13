/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.exception.Error;
import io.finarkein.api.aa.heartbeat.HeartbeatResponse;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.time.Duration;

import static io.finarkein.fiul.TestValues.getHeartbeatResponse;
import static io.finarkein.fiul.TestValues.oneMoney;

@Import(FiulController.class)
@ContextConfiguration(classes = {TestConfig.class, ControllerAdvice.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = FiulController.class)
@Log4j2
class FiulControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    TestConfig testConfig;

    @Value("${server.port}")
    private int port;


    @BeforeEach
    public void setUp() {
        webClient = webClient
                .mutate()
                .baseUrl("http://localhost:" + port + "/api")
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
    @DisplayName("Test Heartbeat")
    void getHeartbeat() throws Exception {
        final EntityExchangeResult<HeartbeatResponse> result = webClient.get().uri("/{aaName}/Heartbeat", oneMoney)
                .exchange()
                .expectStatus().isOk()
                .expectBody(HeartbeatResponse.class)
                .returnResult();

        Assertions.assertEquals(result.getResponseBody(),getHeartbeatResponse());
    }

    @Test()
    @DisplayName("Testing Controller Advice")
    void testControllerAdvice() throws Exception{
        final EntityExchangeResult<Error> result = webClient.get().uri("/{aaName}/Heartbeat","oneMoneys")
                .exchange()
                .expectBody(Error.class)
                .returnResult();
        System.out.println(result.getResponseBody());
    }
}