/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.service;

import io.finarkein.api.aa.consent.request.ConsentRequest;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.*;

import java.util.Optional;

public interface ConsentStore {

    ConsentRequestLog logConsentRequest(FIUConsentRequest consentRequest);

    void updateConsentRequestLog(ConsentRequestLog consentRequestLog);

    void saveConsentRequest(String consentHandle, ConsentRequest consentRequest);

    void updateConsentRequest(String consentHandleId, String consentId);

    Optional<ConsentRequestDTO> findRequestByConsentHandle(String consentHandle);

    Optional<ConsentRequestDTO> findRequestByConsentId(String consentId);

    void logConsentNotification(ConsentNotificationLog consentNotificationLog);

    ConsentNotification getConsentNotification(String consentHandle);

    Optional<ConsentState> getConsentStateByHandle(String consentHandle);

    ConsentState getConsentStateById(String consentId);

    void saveCreateConsentState(String txnId, boolean state, String aaId, String consentHandle);

    void setConsentStateConsentId(String consentHandle, String consentId);

    CreateConsentState getCreateConsentState(String txnId);
}
