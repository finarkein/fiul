/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.common.FIDataRange;
import io.finarkein.api.aa.consent.*;
import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentRequest;
import io.finarkein.api.aa.consent.request.DataConsumer;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.ConsentStatusNotification;
import io.finarkein.api.aa.notification.Notifier;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.model.ConsentRequestDTO;
import io.finarkein.fiul.consent.model.ConsentState;
import io.finarkein.fiul.consent.repo.ConsentNotificationLogRepository;
import io.finarkein.fiul.consent.repo.ConsentRequestDTORepository;
import io.finarkein.fiul.consent.repo.ConsentStateRepository;
import io.finarkein.fiul.consent.repo.ConsentTemplateRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static io.finarkein.api.aa.util.Functions.strToTimeStamp;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ConsentStoreImplTest {

    @InjectMocks
    private ConsentStoreImpl consentStoreImpl;

    @Mock
    private ConsentRequestDTORepository consentRequestDTORepository;

    @Mock
    private ConsentStateRepository consentStateRepository;

    @Mock
    private ConsentNotificationLogRepository consentNotificationLogRepository;

    @Mock
    private ConsentTemplateRepository consentTemplateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Get ConsentRequestDTO Test")
    void getConsentRequestDTOTest() {
        ConsentRequestDTO consentRequestDTO = ConsentRequestDTO.builder()
                .consentHandle("consentHandle")
                .consentId("consentId")
                .build();
        when(consentRequestDTORepository.findById("consentHandle")).thenReturn(Optional.of(consentRequestDTO));
        when(consentRequestDTORepository.findByConsentId("consentId")).thenReturn(Optional.of(consentRequestDTO));

        ConsentRequestDTO returnedConsentRequestDTO = consentStoreImpl.findRequestByConsentHandle("consentHandle").orElse(null);
        Assertions.assertEquals(returnedConsentRequestDTO, consentRequestDTO);

        returnedConsentRequestDTO = consentStoreImpl.findRequestByConsentId("consentId").orElse(null);
        Assertions.assertEquals(returnedConsentRequestDTO, consentRequestDTO);
    }

    @Test
    @DisplayName("Save ConsentRequestDTO Test")
    void saveConsentRequestDTOTest() {
        ConsentRequestDTO consentRequestDTO = ConsentRequestDTO.builder()
                .consentHandle("consentHandle")
                .consentId("consentId")
                .build();
        when(consentRequestDTORepository.save(consentRequestDTO)).thenReturn(consentRequestDTO);

        ConsentRequest consentRequest = new ConsentRequest();
        consentRequest.setTimestamp("2021-06-21T11:57:55.499Z");
        consentRequest.setTxnid("txnId");
        consentRequest.setVer("ver");
        ConsentDetail consentDetail = new ConsentDetail();
        consentDetail.setConsentStart("2021-06-21T11:57:55.499Z");
        consentDetail.setConsentExpiry("2021-07-21T11:57:55.499Z");
        consentDetail.setConsentMode("STORE");
        consentDetail.setFetchType("ONETIME");
        consentDetail.setConsentTypes(Arrays.asList("fdas", "dsafdas"));
        consentDetail.setFiTypes(Arrays.asList("fitypes", "fitypes1"));
        consentDetail.setTimestamp("2021-06-21T11:57:55.499Z");
        Customer customer = new Customer();
        customer.setId("id@klcda");
        consentDetail.setCustomer(customer);
        consentDetail.setPurpose(new Purpose());
        consentDetail.setFIDataRange(new FIDataRange("2021-06-21T11:57:55.499Z", "2021-08-21T11:57:55.499Z"));
        DataLife dataLife = new DataLife();
        dataLife.setUnit("fda");
        dataLife.setValue(1);
        consentDetail.setDataLife(dataLife);
        Frequency frequency = new Frequency();
        frequency.setUnit("unit");
        frequency.setValue(1);
        consentDetail.setFrequency(frequency);
        consentDetail.setDataFilter(Arrays.asList(new DataFilter()));
        DataConsumer dataConsumer = new DataConsumer();
        dataConsumer.setId("dataConsumerId");
        consentDetail.setDataConsumer(dataConsumer);
        consentRequest.setConsentDetail(consentDetail);

        assertDoesNotThrow(() -> consentStoreImpl.saveConsentRequest("consentHandle", consentRequest));
    }

    private ConsentNotificationLog createConsentNotificationLog() {
        return ConsentNotificationLog.builder()
                .version("1.0")
                .txnId("TxnId")
                .notificationTimestamp(strToTimeStamp.apply("2021-06-21T11:57:55.499Z"))
                .notifierType("NotifierType")
                .notifierId("AA")
                .consentHandle("ConsentHandle")
                .consentId("ConsentId")
                .consentState("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Save ConsentNotification and ConsentState Test")
    @Disabled
    void saveConsentNotificationAndConsentState() {
        ConsentNotificationLog consentNotificationLog = createConsentNotificationLog();

        ConsentState consentState = new ConsentState();
        consentState.setConsentHandle(consentNotificationLog.getConsentHandle());
        consentState.setConsentId(consentNotificationLog.getConsentId());
        consentState.setConsentStatus(consentNotificationLog.getConsentState());
        consentState.setTxnId(consentNotificationLog.getTxnId());

        when(consentNotificationLogRepository.save(consentNotificationLog)).thenReturn(consentNotificationLog);
        when(consentStateRepository.save(consentState)).thenReturn(consentState);

        assertDoesNotThrow(() -> consentStoreImpl.logConsentNotification(consentNotificationLog));

        verify(consentNotificationLogRepository, times(1)).save(consentNotificationLog);
        verify(consentStateRepository, times(1)).save(consentState);
    }

    @Test
    @DisplayName("Get ConsentNotification Test")
    void getConsentNotificationTest() {
        ConsentNotification consentNotification = new ConsentNotification();
        ConsentStatusNotification consentStatusNotification = new ConsentStatusNotification();
        consentStatusNotification.setConsentId("consentId");
        consentStatusNotification.setConsentId("ConsentId");
        consentStatusNotification.setConsentStatus("ACTIVE");
        consentStatusNotification.setConsentHandle("ConsentHandle");
        consentNotification.setConsentStatusNotification(consentStatusNotification);
        consentNotification.setNotifier(new Notifier("NotifierType", "AA"));
        consentNotification.setTxnid("TxnId");
        consentNotification.setTimestamp("2021-06-21T11:57:55.499");

        ConsentNotificationLog consentNotificationLog = createConsentNotificationLog();

        when(consentNotificationLogRepository.findConsentNotificationLogByConsentHandle("consentHandle")).thenReturn(Optional.of(consentNotificationLog));

        ConsentNotification returnedConsentNotification = consentStoreImpl.getConsentNotification("consentHandle");
        Assertions.assertEquals(returnedConsentNotification.getVer(), consentNotification.getVer());
        Assertions.assertEquals(returnedConsentNotification.getTxnid(), consentNotification.getTxnid());
        Assertions.assertEquals(returnedConsentNotification.getTimestamp(), consentNotification.getTimestamp());
        Assertions.assertEquals(returnedConsentNotification.getConsentStatusNotification(), consentNotification.getConsentStatusNotification());
        Assertions.assertEquals(returnedConsentNotification.getNotifier(), consentNotification.getNotifier());
    }

    @Test
    @DisplayName("Get ConsentNotification not found Test")
    void getConsentNotificationNotFoundTest() {
        assertNull(consentStoreImpl.getConsentNotification("consentHandle"));
    }

    @Test
    @DisplayName("Get ConsentState Test")
    void getConsentStateTest() {
        ConsentState consentState = new ConsentState();
        consentState.setTxnId("TxnId");
        consentState.setConsentHandle("consentHandle");
        consentState.setConsentId("consentId");
        consentState.setConsentStatus("consentState");
        when(consentStateRepository.findById("consentHandle")).thenReturn(Optional.of(consentState));
        when(consentStateRepository.findByConsentId("consentId")).thenReturn(Optional.of(consentState));

        Optional<ConsentState> returnedOptional = consentStoreImpl.getConsentStateByHandle("consentHandle");
        Assertions.assertEquals(returnedOptional.orElseGet(null), consentState);

        ConsentState returnedConsentState = consentStoreImpl.getConsentStateById("consentId");
        Assertions.assertEquals(returnedConsentState, consentState);
    }
}
