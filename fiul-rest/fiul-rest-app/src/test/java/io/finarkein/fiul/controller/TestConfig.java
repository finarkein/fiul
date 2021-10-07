/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.api.aa.jws.JWSSigner;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.TestValues;
import io.finarkein.fiul.aa.FiulWebClientConfig;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.consent.service.ConsentStore;
import io.finarkein.fiul.consent.service.ConsentTemplateService;
import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.store.FIFetchMetadataStore;
import io.finarkein.fiul.dataflow.store.FIRequestStore;
import io.finarkein.fiul.notification.NotificationPublisher;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import io.finarkein.fiul.notification.callback.RepoConsentCallback;
import io.finarkein.fiul.notification.callback.RepoFICallback;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.JmsListenerContainerFactory;
import reactor.core.publisher.Mono;

import javax.jms.ConnectionFactory;
import java.io.IOException;
import java.util.Optional;

import static io.finarkein.fiul.TestValues.*;
import static io.finarkein.fiul.notification.config.CommonConfig.FIUL_EVENT_FACTORY;

/**
 * Configuration for test which mocks the beans required in the flow
 */
@Configuration
public class TestConfig {
    @MockBean
    private RepoFICallback repoFICallback;
    @MockBean
    private RepoConsentCallback repoConsentCallback;
    @MockBean
    private JWSSigner jwsSignatureUtil;
    @MockBean
    private ConnectionFactory connectionFactory;
    @MockBean(name = FIUL_EVENT_FACTORY)
    private JmsListenerContainerFactory<?> listenerContainerFactory;
    @MockBean
    private CallbackRegistry callbackRegistry;
    @MockBean
    FiulWebClientConfig config;
    @MockBean
    NotificationPublisher publisher;
    @MockBean
    ConsentStore consentStore;
    @MockBean
    FIRequestStore fiRequestStore;
    @MockBean
    FIFetchMetadataStore fiFetchMetadataStore;
    @MockBean
    DataFlowService dataFlowService;
    @MockBean
    ConsentTemplateService consentTemplateService;

    FIUConsentRequest consentRequestWithCallback = consentRequestCallback();
    FIUConsentRequest consentRequestError = consentRequestError();
    FIUConsentRequest finvuConsentRequest = TestValues.getFinvuConsentRequest();

    @Bean
    public AAFIUClient fiuClient(JWSSigner signer) throws IOException {
        AAFIUClient aafiuClient = Mockito.mock(AAFIUClient.class);
        ConsentStore consentStore1 = consentStore;

        SystemException exception = exception();

        Mockito.when(aafiuClient.heartBeat(oneMoney)).thenReturn(Mono.just(getHeartbeatResponse()));

        Mockito.when(aafiuClient.heartBeat("oneMoneys")).thenThrow(exception);

        Mockito.when(aafiuClient.createConsent(finvuConsentRequest)).thenReturn(Mono.just(getFinvuConsentResponse()));

        Mockito.when(consentStore1.logConsentRequest(consentRequestWithCallback)).thenReturn(getConsentRequestLogCallback());

        Mockito.when(consentStore1.logConsentRequest(consentRequestError)).thenReturn(getConsentRequestLog());

        Mockito.when(aafiuClient.createConsent(consentRequestError)).thenReturn(Mono.error(exception));

        Mockito.when(aafiuClient.generateJWS("abcd")).thenReturn(Mono.just(expectedValue));

        Mockito.when(aafiuClient.generateKeyMaterial()).thenReturn(Mono.just(getSerializedPair()));

        Mockito.when(dataFlowService.createFIRequest(createDataRequest(), finvu)).thenReturn(Mono.just(createDataResponse()));

        Mockito.when(dataFlowService.createFIRequest(createDataRequest1(), finvu)).thenReturn(Mono.just(createDataResponse()));

        Mockito.doReturn(Mono.error(exception)).when(dataFlowService).createFIRequest(createDataRequest2(), "finvus");

        Mockito.when(aafiuClient.fiFetch(dataSessionId, finvu, "FIP-1", LinkRefNUmber)).thenReturn(Mono.just(FIUFetchResponse()));

        Mockito.when(aafiuClient.fiFetchData(getDataRequest(), finvu)).thenReturn(Mono.just(FIUFetchResponse()));

        Mockito.when(aafiuClient.fetchDecryptedData(getDataRequest(), finvu)).thenReturn(Mono.just(FIUFetchDecryptResponse()));

        Mockito.doReturn(Mono.just(FIUFetchResponse())).when(aafiuClient).fiFetch(dataSessionId, finvu);

        Mockito.doNothing().when(publisher).publishFINotification(fiNotificationRequest());

        Mockito.doThrow(exception).when(publisher).publishFINotification(fiNotificationCallBack());

        Mockito.doNothing().when(publisher).publishConsentNotification(getConsentNotification());

        Mockito.doThrow(exception).when(publisher).publishConsentNotification(getConsentNotifiCallback());

        Mockito.doReturn(Mono.just(FIUFetchResponse())).when(dataFlowService).fiFetch(dataSessionId, finvu, "FIP-1", linkRefArray);

        Mockito.doReturn(Mono.error(exception)).when(dataFlowService).fiFetch(dataSessionId, "finvus", "FIP-1", linkRefArray);

        Mockito.doReturn(Mono.just(FIUFetchResponse())).when(dataFlowService).fiGet(dataSessionId, "FIP-1", linkRefArray);

        Mockito.doNothing().when(consentStore1).updateConsentRequest(consentHandle, consentId);

        return aafiuClient;
    }

    @Bean
    public ConsentService consentService() {
        ConsentService consentService = Mockito.mock(ConsentService.class);
        Mockito.when(consentService.createConsent(finvuConsentRequest)).thenReturn(Mono.just(getFinvuConsentResponse()));

        final ConsentArtefact consentArtefact = getConsentArtefact();
        Mockito.when(consentService.getConsentArtefact(consentArtefact.getConsentId(), Optional.of(finvu))).thenReturn(Mono.just(consentArtefact));

        final ConsentHandleResponse consentHandleResponse = getConsentHandleResponse();
        Mockito.when(consentService.getConsentStatus(consentHandleResponse.getConsentHandle(), Optional.of(finvu))).thenReturn((Mono.just(consentHandleResponse)));

        Mockito.when(consentService.createConsent(consentRequestWithCallback)).thenReturn(Mono.just(getFinvuConsentResponse()));
        return consentService;
    }

}
