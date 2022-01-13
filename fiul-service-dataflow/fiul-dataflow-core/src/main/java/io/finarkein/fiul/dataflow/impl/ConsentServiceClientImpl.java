/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.impl;

import io.finarkein.api.aa.consent.artefact.SignedConsent;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.dataflow.ConsentServiceClient;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static io.finarkein.api.aa.util.Functions.aaNameExtractor;

@Service
public class ConsentServiceClientImpl implements ConsentServiceClient {

    protected final ConsentService consentService;

    @Autowired
    ConsentServiceClientImpl(ConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    public Mono<FIUFIRequest> setSignatureIfNotSet(FIUFIRequest fiRequest) {
        final var consent = fiRequest.getConsent();
        if (consent.getDigitalSignature() != null && !"NA".equalsIgnoreCase(consent.getDigitalSignature()))
            return Mono.just(fiRequest);

        return consentService
                .getSignedConsent(consent.getId(), Optional.ofNullable(fiRequest.getAaHandle()))
                .map(signedConsentDTO -> {
                    consent.setDigitalSignature(signedConsentDTO.getSignature());
                    return fiRequest;
                })
                ;
    }

    @Override
    public ConsentStateDTO getConsentStateByConsentId(String consentId) {
        return consentService.getConsentStateByConsentId(consentId);
    }

    @Override
    public Mono<SignedConsent> getSignedConsentDetail(String consentId, String aaName) {
        return consentService.getSignedConsentDetail(consentId, aaName);
    }

    @Override
    public Mono<ConsentStateDTO> getConsentState(String consentHandle, Optional<String> customerAAId) {
        final Optional<String> aaHandle = customerAAId.map(aaNameExtractor);
        return consentService.getConsentState(consentHandle, aaHandle);
    }

    @Override
    public Mono<ConsentHandleResponse> getConsentStatus(String consentHandle, Optional<String> aaName) {
        return consentService.getConsentStatus(consentHandle, aaName);
    }
}
