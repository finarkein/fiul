/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback;

import io.finarkein.fiul.notification.callback.model.ConsentCallback;
import io.finarkein.fiul.notification.callback.model.FICallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class CallbackRegistryImplTest {

    @InjectMocks
    private CallbackRegistryJPAImpl callbackRegistryJPA;

    @Mock
    private RepoFICallback repoFICallback;

    @Mock
    private RepoConsentCallback repoConsentCallback;


    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void fiCallBackFindByIdTest() {
        FICallback fiCallback = new FICallback();
        fiCallback.setSessionId("sessionId");
        fiCallback.setConsentId("consentId");
        when(repoFICallback.findById("sessionId")).thenReturn(Optional.of(fiCallback));

        FICallback returnedCallback = callbackRegistryJPA.fiCallback("sessionId");
        Assertions.assertEquals(returnedCallback, fiCallback);
    }

    @Test
    void fiCallBackSaveTest() {
        FICallback fiCallback = new FICallback();
        fiCallback.setSessionId("sessionId");
        fiCallback.setConsentId("consentId");
        when(repoFICallback.save(fiCallback)).thenReturn(fiCallback);

        assertDoesNotThrow(() -> callbackRegistryJPA.registerFICallback(fiCallback));
        verify(repoFICallback, times(1)).save(fiCallback);
    }

    @Test
    void fiCallBackDeleteByConsentIdTest() {
        doNothing().when(repoFICallback).deleteByConsentId("consentId");
        callbackRegistryJPA.deleteFICallbackByConsentId("consentId");
        verify(repoFICallback, times(1)).deleteByConsentId("consentId");
    }

    @Test
    void fiCallBackDeleteBySessionIdTest() {
        doNothing().when(repoFICallback).deleteById("sessionId");
        callbackRegistryJPA.deleteFICallbacksBySession("sessionId");
        verify(repoFICallback, times(1)).deleteById("sessionId");
    }

    @Test
    void consentCallBackFindByIdTest() {
        ConsentCallback consentCallback = new ConsentCallback();
        consentCallback.setConsentHandleId("consentHandleId");
        when(repoConsentCallback.findById("consentHandleId")).thenReturn(Optional.of(consentCallback));

        ConsentCallback returnedConsentCallback = callbackRegistryJPA.consentCallback("consentHandleId");
        Assertions.assertEquals(returnedConsentCallback, consentCallback);
    }

    @Test
    void consentCallSaveTest() {
        ConsentCallback consentCallback = new ConsentCallback();
        consentCallback.setConsentHandleId("consentHandleId");
        when(repoConsentCallback.save(consentCallback)).thenReturn(consentCallback);

        assertDoesNotThrow(() -> callbackRegistryJPA.registerConsentCallback(consentCallback));
        verify(repoConsentCallback, times(1)).save(consentCallback);

    }

    @Test
    void consentCallDeleteTest() {
        doNothing().when(repoConsentCallback).deleteById("consentHandleId");
        callbackRegistryJPA.deleteConsentCallback("consentHandleId");
        verify(repoConsentCallback, times(1)).deleteById("consentHandleId");
    }
}
