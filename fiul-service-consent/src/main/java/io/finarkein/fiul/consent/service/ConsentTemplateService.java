/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.service;

import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.fiul.consent.model.ConsentRequestInput;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import io.finarkein.fiul.consent.model.ConsentTemplateDeleteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ConsentTemplateService {

    Mono<ConsentTemplateResponse> saveConsentTemplate(ConsentTemplate consentTemplate);

    Mono<ConsentTemplate> getConsentTemplate(String id);

    Mono<Page<ConsentTemplate>> getAllConsentTemplates(Pageable pageable);

    Mono<ConsentTemplateDeleteResponse> deleteConsentTemplate(String id);

    Mono<ConsentResponse> createConsentRequestUsingTemplate(ConsentRequestInput consentRequestInput);

    Mono<ConsentDetail> prepareConsentDetailsFromTemplate(ConsentRequestInput consentRequestInput);

    Page<ConsentTemplate> getConsentTemplatesByQuery(String tag, String consentVersion, Pageable pageRequest);
}
