/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.service;

import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.model.ConsentRequestDTO;
import io.finarkein.fiul.consent.model.ConsentState;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ConsentService {

    Mono<ConsentResponse> createConsent(FIUConsentRequest consentRequest);

    Mono<ConsentHandleResponse> getConsentStatus(String consentHandle, Optional<String> aaName);

    Mono<ConsentArtefact> getConsentArtefact(String consentId, Optional<String> aaName);

    Optional<ConsentRequestDTO> getConsentRequestByConsentId(String consentId);

    ConsentDetail consentDetail(String consentId, String aaName);

    void handleConsentNotification(ConsentNotificationLog consentNotificationLog);

    Mono<ConsentState> getConsentState(String consentHandle, Optional<String> customerAAId);
}
