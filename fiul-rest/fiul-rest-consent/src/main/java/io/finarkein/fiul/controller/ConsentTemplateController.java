/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.fiul.consent.model.ConsentRequestInput;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import io.finarkein.fiul.consent.model.ConsentTemplateDeleteResponse;
import io.finarkein.fiul.consent.service.ConsentTemplateResponse;
import io.finarkein.fiul.consent.service.ConsentTemplateService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/")
@Log4j2
public class ConsentTemplateController {

    private final ConsentTemplateService consentTemplateService;

    @Autowired
    ConsentTemplateController(ConsentTemplateService consentTemplateService) {
        this.consentTemplateService = consentTemplateService;
    }

    @PostMapping("/consent/template")
    public Mono<ConsentTemplateResponse> saveConsentTemplate(@RequestBody ConsentTemplate consentTemplate) {
        return consentTemplateService.saveConsentTemplate(consentTemplate);
    }

    @PostMapping("/consent/request")
    public Mono<ConsentResponse> postConsentUsingTemplate(@RequestBody ConsentRequestInput consentRequestInput) {
        return consentTemplateService.createConsentRequestUsingTemplate(consentRequestInput);
    }

    @GetMapping("/consent/template/{consentTemplateId}")
    public Mono<ConsentTemplate> getConsentTemplate(@PathVariable String consentTemplateId) {
        return consentTemplateService.getConsentTemplate(consentTemplateId);
    }

    @GetMapping("/consent/template/q")
    public Page<ConsentTemplate> getConsentTemplateByQuery(@RequestParam(value = "tag", required = false) String tag,
                                                           @RequestParam(value = "consentVersion", required = false) String consentVersion,
                                                           @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                           @RequestParam(value = "pageNumber", required = false) Integer pageNumber
    ) {
        return consentTemplateService.getConsentTemplatesByQuery(tag,
                consentVersion,
                PageRequest.of(
                        (pageNumber == null) ? 0 : pageNumber,
                        (pageSize == null) ? 20 : pageSize,
                        Sort.by(Sort.Order.desc("createdOn"))
                )
        );
    }

    @PostMapping("/consent/template/detail")
    public Mono<ConsentDetail> prepareConsentDetailsFromTemplate(@RequestBody ConsentRequestInput consentRequestInput) {
        return consentTemplateService.prepareConsentDetailsFromTemplate(consentRequestInput);
    }

    @DeleteMapping("/consent/template/{consentTemplateId}")
    public Mono<ConsentTemplateDeleteResponse> deleteConsentTemplate(@PathVariable String consentTemplateId) {
        return consentTemplateService.deleteConsentTemplate(consentTemplateId);
    }
}
