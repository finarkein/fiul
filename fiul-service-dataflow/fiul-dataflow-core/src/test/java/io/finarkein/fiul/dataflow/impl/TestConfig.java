/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.dataflow.ConsentServiceClient;
import io.finarkein.fiul.dataflow.EasyDataFlowService;
import io.finarkein.fiul.dataflow.PostResponseSchedulerConfig;
import io.finarkein.fiul.dataflow.store.AAFIDataStore;
import io.finarkein.fiul.dataflow.store.EasyFIDataStore;
import io.finarkein.fiul.dataflow.store.FIFetchMetadataStore;
import io.finarkein.fiul.dataflow.store.FIRequestStore;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static io.finarkein.fiul.dataflow.Util.loadJsonFromFile;

@Configuration
public class TestConfig {

    @MockBean
    private AAFIUClient fiuClient;
    @MockBean
    private FIRequestStore fiRequestStore;
    @MockBean
    private EasyFIDataStore easyFIDataStore;
    @MockBean
    private AAFIDataStore aafiDataStore;
    @MockBean
    private FIFetchMetadataStore fiFetchMetadataStore;
    @MockBean
    private ConsentService consentService;
    @MockBean
    private CallbackRegistry callbackRegistry;
    @MockBean
    private ConsentServiceClient consentServiceClient;

    @Bean
    EasyDataFlowService getDataFlowServiceImpl() {
        final var serializedKeyPair = loadJsonFromFile("serializedKeyPair.json", SerializedKeyPair.class);
        Mockito.when(fiuClient.generateKeyMaterial()).thenReturn(Mono.just(serializedKeyPair));

        return new EasyDataFlowServiceImpl(fiuClient, fiRequestStore, fiFetchMetadataStore, easyFIDataStore,
                callbackRegistry, consentServiceClient , new PostResponseSchedulerConfig("immediate", "immediate"));
    }

    @Bean
    ObjectMapper mapper(){
        return new ObjectMapper();
    }
}
