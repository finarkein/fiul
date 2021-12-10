/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.dataflow.impl.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static io.finarkein.fiul.dataflow.Util.loadJsonFromFile;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = TestConfig.class)
@ExtendWith(SpringExtension.class)
class EasyDataFlowServiceTest {

    @Autowired
    private EasyDataFlowService dataFlowService;

    @Autowired
    private AAFIUClient fiuClient;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void createDataRequestTest() {
        var dataRequest = loadJsonFromFile("dataRequest.json", DataRequest.class);
        final var fiRequestResponse = loadJsonFromFile("fiRequestResponse.json", FIRequestResponse.class);
        Mockito.when(fiuClient.createFIRequest(any(FIUFIRequest.class),any(String.class))).thenReturn(Mono.just(fiRequestResponse));

        final var requestResponse = dataFlowService.createDataRequest(dataRequest).block();

        Assertions.assertNotNull(requestResponse);
        Assertions.assertEquals(fiRequestResponse.getConsentId(), requestResponse.getConsentId());
        Assertions.assertNotNull(fiRequestResponse.getVer());
        Assertions.assertNotNull(fiRequestResponse.getSessionId());
        Assertions.assertNotNull(fiRequestResponse.getTimestamp());
    }

}
