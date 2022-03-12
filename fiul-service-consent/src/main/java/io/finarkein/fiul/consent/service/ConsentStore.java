/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.service;

import io.finarkein.api.aa.consent.request.ConsentRequest;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.model.ConsentRequestDTO;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.consent.model.SignedConsentDTO;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ConsentStore {

    void saveConsentRequest(String consentHandle, ConsentRequest consentRequest);

    void updateConsentRequest(String consentHandleId, String consentId);

    Optional<SignedConsentDTO> findSignedConsent(String consentId);

    Mono<Optional<ConsentRequestDTO>> findRequestByConsentHandle(String consentHandle);

    Optional<ConsentRequestDTO> findRequestByConsentId(String consentId);

    void logConsentNotification(ConsentNotificationLog consentNotificationLog);

    ConsentNotification getConsentNotification(String consentHandle);

    void saveConsentState(ConsentStateDTO consentStateDTO);

    Optional<ConsentStateDTO> getConsentStateByHandle(String consentHandle);

    Mono<Optional<ConsentStateDTO>> consentStateByHandle(String consentHandle);

    ConsentStateDTO getConsentStateById(String consentId);

    ConsentStateDTO getConsentStateByTxnId(String txnId);

    Mono<ConsentStateDTO> consentStateByTxnId(String txnId);

    ConsentStateDTO updateConsentState(ConsentStateDTO consentStateDTO);

    void saveSignedConsent(SignedConsentDTO signedConsentDTO);
}
