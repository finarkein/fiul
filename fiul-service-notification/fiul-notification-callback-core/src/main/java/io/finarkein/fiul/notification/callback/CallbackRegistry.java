/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback;

import io.finarkein.fiul.notification.callback.model.ConsentCallback;
import io.finarkein.fiul.notification.callback.model.ConsentWebhook;
import io.finarkein.fiul.notification.callback.model.FICallback;

import java.util.List;

/**
 * Class to actively maintain registry of callbacks.
 */
public interface CallbackRegistry {

    void registerFICallback(FICallback fiCallback);

    void registerConsentCallback(ConsentCallback consentCallback);

    void registerConsentWebhooks(List<ConsentWebhook> consentWebhooks);

    ConsentCallback consentCallback(String consentHandleId);

    List<ConsentWebhook> consentWebhooks(String consentHandleId);

    void deleteFICallbackByConsentId(String consentHandleId);

    FICallback fiCallback(String sessionId);

    void deleteFICallbacksBySession(String sessionId);

    void deleteConsentCallback(String consentHandleId);
}
