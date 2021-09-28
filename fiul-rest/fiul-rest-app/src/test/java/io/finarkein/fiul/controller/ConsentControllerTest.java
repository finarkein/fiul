/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.api.aa.exception.Error;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import java.time.Duration;

import static io.finarkein.fiul.TestValues.*;

@Import(ConsentController.class)
@ContextConfiguration(classes = {TestConfig.class, ControllerAdvice.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ConsentController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Log4j2
@Disabled("Need to check")
class ConsentControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    TestConfig config;

    @Value("${server.port}")
    private int port;

    @BeforeEach
    public void setUp() {
        webClient = webClient
                .mutate()
                .baseUrl("http://localhost:" + port + "/api")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
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
    @DisplayName("Testing Post Consent")
    void testConsent() throws Exception {
        final EntityExchangeResult<ConsentResponse> result = webClient.post().uri("/Consent")
                .bodyValue(config.finvuConsentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConsentResponse.class)
                .returnResult();
        Assertions.assertEquals(getFinvuConsentResponse(), result.getResponseBody());
    }

    @Test()
    @DisplayName("Testing Post Consent with Callback")
    void testConsentCallback() throws Exception {
        final EntityExchangeResult<ConsentResponse> result = webClient.post().uri("/Consent")
                .bodyValue(config.consentRequestWithCallback)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConsentResponse.class)
                .returnResult();
        Assertions.assertEquals(result.getResponseBody(), getFinvuConsentResponse());
    }

    @Test()
    @DisplayName("Test Post Consent Error")
    void testPostConsentError() throws Exception {
        final EntityExchangeResult<Error> result =  webClient.post().uri("/Consent")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(config.consentRequestError)
                .exchange()
                .expectBody(Error.class)
                .returnResult();
        System.out.println(result.getResponseBody());
    }

    @Test()
    @DisplayName("Testing ConsentHandle")
    void testConsentHandle() throws Exception {
        final EntityExchangeResult<ConsentHandleResponse> result = webClient.get().uri("/Consent/handle/{consentHandle}/{aaName}", consentHandle, finvu)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConsentHandleResponse.class)
                .returnResult();
        Assertions.assertEquals(getConsentHandleResponse(), result.getResponseBody());
    }

    @Test()
    @DisplayName("Testing Consent Artifact")
    void testConsentArtifact() throws Exception {
        final EntityExchangeResult<ConsentArtefact> result = webClient.get().uri("/Consent/{consentId}/{aaName}", consentId, finvu)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConsentArtefact.class)
                .returnResult();
        Assertions.assertEquals(getConsentArtefact(), result.getResponseBody());
    }

}
