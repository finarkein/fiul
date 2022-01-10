/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import io.finarkein.api.aa.consent.artefact.SignedConsent;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ConsentServiceClient {
    Mono<FIUFIRequest> setSignatureIfNotSet(FIUFIRequest fiRequest);

    ConsentStateDTO getConsentStateByConsentId(String consentId);

    Mono<SignedConsent> getSignedConsentDetail(String consentId, String aaName);

    Mono<ConsentStateDTO> getConsentState(String consentHandle, Optional<String> customerAAId);

    Mono<ConsentHandleResponse> getConsentStatus(String consentHandle, Optional<String> aaName);
}
