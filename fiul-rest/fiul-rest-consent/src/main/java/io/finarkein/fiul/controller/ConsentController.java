/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.ConsentState;
import io.finarkein.fiul.consent.service.ConsentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/")
@Log4j2
public class ConsentController {

    private final ConsentService consentService;

    @Autowired
    ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping("/Consent")
    public Mono<ConsentResponse> postConsentRequest(@RequestBody FIUConsentRequest consentRequest) {
        return consentService.createConsent(consentRequest);
    }

    @GetMapping({"/Consent/handle/{consentHandle}/{aaName}", "/Consent/handle/{consentHandle}"})
    public Mono<ConsentHandleResponse> getConsentHandle(@PathVariable String consentHandle, @PathVariable Optional<String> aaName) {
        return consentService.getConsentStatus(consentHandle, aaName);
    }

    @GetMapping({"/Consent/{consentID}/{aaName}", "/Consent/{consentID}"})
    public Mono<ConsentArtefact> getConsentArtifact(@PathVariable String consentID, @PathVariable Optional<String> aaName) {
        return consentService.getConsentArtefact(consentID, aaName);
    }

    @GetMapping("/consent/state/{consentHandleID}/{customerAAId}")
    public Mono<ConsentState> getConsentState(@PathVariable String consentHandleID, @PathVariable String customerAAId) {
        return consentService.getConsentState(consentHandleID, Optional.of(customerAAId));
    }

    @GetMapping("/consent/state/{consentHandleID}")
    public Mono<ConsentState> getConsentStateByHandleId(@PathVariable String consentHandleID) {
        return consentService.getConsentState(consentHandleID, Optional.empty());
    }
}
