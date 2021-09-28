/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback;

import io.finarkein.fiul.notification.callback.model.ConsentCallback;
import io.finarkein.fiul.notification.callback.model.FICallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CallbackRegistryJPAImpl implements CallbackRegistry{

    @Autowired
    private RepoFICallback repoFICallback;

    @Autowired
    private RepoConsentCallback repoConsentCallback;

    @Override
    public void registerFICallback(FICallback fiCallback) {
        repoFICallback.save(fiCallback);
    }

    @Override
    public void registerConsentCallback(ConsentCallback consentCallback) {
        repoConsentCallback.save(consentCallback);
    }

    @Override
    public ConsentCallback consentCallback(String consentHandleId) {
        final Optional<ConsentCallback> optional = repoConsentCallback.findById(consentHandleId);
        return optional.orElse(null);
    }

    @Override
    public void deleteFICallbackByConsentId(String consentId) {
        repoFICallback.deleteByConsentId(consentId);
    }

    @Override
    public FICallback fiCallback(String sessionId) {
        final Optional<FICallback> optional = repoFICallback.findById(sessionId);
        return optional.orElse(null);
    }

    @Override
    public void deleteFICallbacksBySession(String sessionId) {
        repoFICallback.deleteById(sessionId);
    }

    @Override
    public void deleteConsentCallback(String consentHandleId) {
        repoConsentCallback.deleteById(consentHandleId);
    }
}
