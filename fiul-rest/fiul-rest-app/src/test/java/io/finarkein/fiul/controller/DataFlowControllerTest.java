/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.exception.Error;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
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


@Import(DataFlowController.class)
@ContextConfiguration(classes = {TestConfig.class, ControllerAdvice.class})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DataFlowController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Log4j2
@Disabled("Need to check")
class DataFlowControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Value("${server.port}")
    private int port;

    @BeforeEach
    public void setUp(){
        webClient = webClient
                .mutate()
                .baseUrl("http://localhost:" + port + "/api")
                .defaultHeader("Content-Type", "application/json")
                .filter(logRequest())
                .responseTimeout(Duration.ofMillis(300000))
                .build();
    }

    private ExchangeFilterFunction logRequest(){
        return (clientRequest, next) -> {
            if (log.isDebugEnabled())
                log.debug("Calling: {} {}, headers: {}", clientRequest.method(), clientRequest.url(), clientRequest.headers().entrySet());
            return next.exchange(clientRequest);
        };
    }

    @Test()
    @DisplayName("Testing creating FI Fetch")
    void createFetchData() throws Exception{
        FIUFIRequest dataRequest = createDataRequest();
        final EntityExchangeResult<FIRequestResponse> result = webClient.post().uri("/FI/request/{aaName}", finvu)
                .bodyValue(dataRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FIRequestResponse.class)
                .returnResult();
        Assertions.assertEquals(result.getResponseBody(),createDataResponse());
    }

    @Test()
    @DisplayName("Testing creating FI Fetch with Callback")
    void createFetchDataCallback() throws Exception{
        final EntityExchangeResult<FIRequestResponse> result = webClient.post().uri("/FI/request/{aaName}", finvu)
                .bodyValue(createDataRequest1())
                .exchange()
                .expectStatus().isOk()
                .expectBody(FIRequestResponse.class)
                .returnResult();
        Assertions.assertEquals(result.getResponseBody(),createDataResponse());
    }

    @Test()
    @DisplayName("Testing creating FI Fetch Error")
    void createFetchDataError() throws Exception{
        FIUFIRequest dataRequest = createDataRequest2();
        final EntityExchangeResult<Error> result = webClient.post().uri("/FI/request/{aaName}", "finvus")
                .bodyValue(dataRequest)
                .exchange()
                .expectBody(Error.class)
                .returnResult();
        System.out.println(result.getResponseBody());
    }

    @Test()
    @DisplayName("Test Fetch Data")
    void fetchData() throws Exception{
        final EntityExchangeResult<FIFetchResponse> result = webClient.get().uri("/FI/fetch/{dataSessionId}/{aaName}",dataSessionId,finvu)
                .header("fipId","FIP-1")
                .header("linkRefNumber",LinkRefNUmber)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FIFetchResponse.class)
                .returnResult();
        Assertions.assertEquals(result.getResponseBody(),FIUFetchResponse());
    }

    @Test()
    @DisplayName("Test Fetch Data Error")
    void testFetchDataError() throws Exception{
        final EntityExchangeResult<Error> result = webClient.get().uri("/FI/fetch/{dataSessionId}/{aaName}",dataSessionId, "finvus")
                .header("fipId","FIP-1")
                .header("linkRefNumber",LinkRefNUmber)
                .exchange()
                .expectBody(Error.class)
                .returnResult();
    }

    @Test()
    @DisplayName("Test Get Data")
    void testGetData() throws Exception{
        final EntityExchangeResult<FIFetchResponse> result = webClient.get().uri("/FI/{dataSessionId}",finvu,dataSessionId)
                .header("fipId","FIP-1")
                .header("linkRefNumber",LinkRefNUmber)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FIFetchResponse.class)
                .returnResult();

    }
}
