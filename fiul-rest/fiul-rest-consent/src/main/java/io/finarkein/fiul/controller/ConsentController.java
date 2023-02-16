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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@ConditionalOnProperty(value="fiu.controller", havingValue = "fiul", matchIfMissing = true)
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

    @GetMapping("/Consent/handle/{consentHandle}")
    public Mono<ConsentHandleResponse> getConsentHandle(@PathVariable String consentHandle,
                                                        @RequestParam(value = "aaHandle", required = false) String aaHandle) {
        Optional<String> aaNameOptional = Optional.ofNullable(aaHandle);
        return consentService.getConsentStatus(consentHandle, aaNameOptional);
    }

    @GetMapping("/Consent/{consentID}")
    public Mono<ConsentArtefact> getConsentArtifact(@PathVariable String consentID,
                                                    @RequestParam(value = "aaHandle", required = false) String aaHandle) {
        Optional<String> aaNameOptional = Optional.ofNullable(aaHandle);
        return consentService.getConsentArtefact(consentID, aaNameOptional);
    }

    @GetMapping("/consent/state/{consentHandle}")
    public Mono<ConsentState> getConsentState(@PathVariable String consentHandle,
                                              @RequestParam(value = "aaHandle", required = false) String aaHandle) {
        return consentService.getConsentState(consentHandle, Optional.ofNullable(aaHandle))
                .map(ConsentState::from);
    }
}
