/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import io.finarkein.api.aa.consent.Customer;
import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.api.aa.consent.request.DataConsumer;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.Functions;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.ConsentRequestInput;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import io.finarkein.fiul.consent.model.ConsentTemplateDefinition;
import io.finarkein.fiul.consent.model.ConsentTemplateDeleteResponse;
import io.finarkein.fiul.consent.repo.ConsentTemplateRepository;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.consent.service.ConsentTemplateResponse;
import io.finarkein.fiul.consent.service.ConsentTemplateService;
import io.finarkein.fiul.consent.service.PurposeFetcher;
import io.finarkein.fiul.consent.validators.ConsentRequestInputValidator;
import io.finarkein.fiul.consent.validators.ConsentTemplateValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.finarkein.fiul.Functions.currentTimestampSupplier;
import static io.finarkein.fiul.consent.impl.ConsentTemplateUtils.*;

@Service
@Log4j2
class ConsentTemplateServiceImpl implements ConsentTemplateService {

    @Autowired
    private ConsentTemplateRepository consentTemplateRepository;

    @Autowired
    private PurposeFetcher purposeFetcher;

    @Autowired
    private ConsentRequestInputValidator consentRequestInputValidator;

    @Autowired
    private ConsentTemplateValidator consentTemplateValidator;

    @Autowired
    private ConsentService consentService;

    private final String dataConsumerId;

    @Autowired
    public ConsentTemplateServiceImpl(@Value("${fiul.entity_id}") String dataConsumerId) {
        this.dataConsumerId = dataConsumerId;
    }

    @Override
    public Mono<ConsentTemplateResponse> saveConsentTemplate(ConsentTemplate consentTemplate) {
        return Mono.just(consentTemplate)
                .doOnNext(input -> log.debug("SaveConsentTemplate: start: request:{}", input))
                .flatMap(this::doSaveConsentTemplate)
                .doOnSuccess(response -> log.debug("SaveConsentTemplate: success: consentTemplateId:{}", response.getConsentTemplateId()))
                .doOnError(error -> log.error("SaveConsentTemplate: error:{}", error.getMessage(), error));
    }

    private Mono<ConsentTemplateResponse> doSaveConsentTemplate(ConsentTemplate consentTemplate) {
        consentTemplateValidator.validateConsentTemplate(consentTemplate, Functions.UUIDSupplier.get());

        final var savedTemplate = consentTemplateRepository.save(consentTemplate);
        return Mono.just(new ConsentTemplateResponse(savedTemplate.getId()));
    }

    @Override
    public Optional<ConsentTemplate> getConsentTemplate(String id) {
        return consentTemplateRepository.findById(id);
    }

    @Override
    public Mono<ConsentTemplateDeleteResponse> deleteConsentTemplate(String id) {
        consentTemplateRepository.deleteById(id);
        return Mono.just(new ConsentTemplateDeleteResponse(id, true));
    }

    private ConsentTemplate getConsentTemplate(ConsentRequestInput consentRequestInput) {
        return consentTemplateRepository
                .findById(consentRequestInput.getConsentTemplateId())
                .orElseThrow(() -> Errors.InvalidRequest.with(consentRequestInput.getTxnId(), "ConsentTemplate not found for given consentTemplateId:" + consentRequestInput.getConsentTemplateId()));
    }

    @Override
    public Mono<ConsentResponse> createConsentRequestUsingTemplate(ConsentRequestInput consentRequestInput) {
        return Mono.just(consentRequestInput)
                .doOnNext(input -> log.debug("CreateConsentUsingTemplate: start: request:{}", input))
                .flatMap(this::doCreateConsentRequestUsingTemplate)
                .doOnSuccess(response -> log.debug("CreateConsentUsingTemplate: success: consentHandle:{}", response.getConsentHandle()))
                .doOnError(error -> log.error("CreateConsentUsingTemplate: error:{}", error.getMessage(), error))
                ;
    }

    private Mono<ConsentResponse> doCreateConsentRequestUsingTemplate(ConsentRequestInput consentRequestInput) {
        consentRequestInputValidator.validateConsentRequestInput(consentRequestInput);

        var consentTemplate = getConsentTemplate(consentRequestInput);
        final var consentTemplateDefinition = consentTemplate.getConsentTemplateDefinition();
        var consentDetail = prepareConsentDetailFromTemplate(consentTemplateDefinition, consentRequestInput.getCustomerId());

        return consentService.createConsent(FIUConsentRequest.builder()
                .ver(consentTemplate.getConsentVersion())
                .txnId(consentRequestInput.getTxnId())
                .timestamp(currentTimestampSupplier.get())
                .consentDetail(consentDetail)
                .callback(consentRequestInput.getCallback() != null? consentRequestInput.getCallback() : consentTemplateDefinition.getCallback())
                .build());
    }

    @Override
    public Mono<ConsentDetail> prepareConsentDetailsFromTemplate(ConsentRequestInput consentRequestInput) {
        consentRequestInputValidator.validateConsentRequestInput(consentRequestInput);

        var consentTemplate = getConsentTemplate(consentRequestInput);
        return Mono.just(prepareConsentDetailFromTemplate(consentTemplate.getConsentTemplateDefinition(), consentRequestInput.getCustomerId()));
    }

    @Override
    public Page<ConsentTemplate> getConsentTemplatesByQuery(String tag, String consentVersion, Pageable pageRequest) {
        return consentTemplateRepository.getByQuery(tag, consentVersion, pageRequest);
    }

    private ConsentDetail prepareConsentDetailFromTemplate(ConsentTemplateDefinition consentTemplateDefinition, String customerId) {
        ConsentDetail consentDetail = new ConsentDetail();

        String consentStart = Functions.currentTimestampSupplier.get();
        int currentYear = LocalDateTime.now().getYear();
        String[] strings = generateConsentDateRange(consentTemplateDefinition.getConsentStartOffset(), consentTemplateDefinition.getConsentExpiryDuration(), consentStart);
        consentDetail.setConsentStart(strings[0]);
        consentDetail.setConsentExpiry(strings[1]);
        consentDetail.setFIDataRange(generateFIDataRange(consentTemplateDefinition.getConsentTemplateDataRange(), strings[0], strings[1], currentYear));
        consentDetail.setConsentMode(consentTemplateDefinition.getConsentMode().toString());
        consentDetail.setFetchType(consentTemplateDefinition.getFetchType());
        consentDetail.setConsentTypes(consentTemplateDefinition.getConsentTypes());
        consentDetail.setFiTypes(consentTemplateDefinition.getFiTypes());
        consentDetail.setDataLife(generateDataLife(consentTemplateDefinition.getDataLife()));
        consentDetail.setFrequency(generateFrequency(consentTemplateDefinition.getFrequency()));
        consentDetail.setPurpose(purposeFetcher.fetchPurpose(consentTemplateDefinition.getPurposeCode()));
        DataConsumer dataConsumer = new DataConsumer();
        dataConsumer.setId(dataConsumerId);
        consentDetail.setDataConsumer(dataConsumer);
        Customer customer = new Customer();
        customer.setId(customerId);
        consentDetail.setCustomer(customer);
        consentDetail.setDataFilter(consentTemplateDefinition.getDataFilter());

        return consentDetail;
    }
}
