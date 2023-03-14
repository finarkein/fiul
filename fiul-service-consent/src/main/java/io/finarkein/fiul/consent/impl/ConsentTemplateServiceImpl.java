/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.consent.Customer;
import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.api.aa.consent.request.DataConsumer;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.Functions;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.ConsentRequestInput;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import io.finarkein.fiul.consent.model.ConsentTemplateDeleteResponse;
import io.finarkein.fiul.consent.repo.ConsentTemplateRepository;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.consent.service.ConsentTemplateResponse;
import io.finarkein.fiul.consent.service.ConsentTemplateService;
import io.finarkein.fiul.consent.service.PurposeFetcher;
import io.finarkein.fiul.consent.validators.ConsentRequestInputValidator;
import io.finarkein.fiul.consent.validators.ConsentTemplateValidator;
import io.finarkein.fiul.dto.ConsentTemplateDefinition;
import io.finarkein.platform.tenant.TenantInfoHolder;
import io.finarkein.tenantmanager.cache.service.TenantConfigCacheBuilder;
import io.finarkein.tenantmanager.cache.service.TenantManagerConfigCacheService;
import io.finarkein.tenantmanager.dto.TenantConfigSaveRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.finarkein.fiul.Functions.UUIDSupplier;
import static io.finarkein.fiul.Functions.currentTimestampSupplier;
import static io.finarkein.fiul.consent.impl.ConsentTemplateUtils.*;

@Service
@Log4j2
class ConsentTemplateServiceImpl implements ConsentTemplateService {

    public static final String CONFIG_TYPE = "consentTemplate";

    @Autowired
    private PurposeFetcher purposeFetcher;

    @Autowired
    private ConsentRequestInputValidator consentRequestInputValidator;

    @Autowired
    private ConsentTemplateValidator consentTemplateValidator;

    @Autowired
    private ConsentService consentService;

    @Autowired
    private ConsentTemplateRepository consentTemplateRepository;

    private final TenantManagerConfigCacheService tenantManagerConfigCacheService;

    private final String consentTemplateDefaultOrg;

    private final String consentTemplateDefaultWorkspace;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String dataConsumerId;

    @Autowired
    public ConsentTemplateServiceImpl(@Value("${fiul.entity_id}") String dataConsumerId,
                                      @Value("${tnt-mgr.base-url:http://localhost:13500}") String tenantCfgServiceBaseUrl,
                                      @Value("${consent.template.default.org:#{null}}") String consentTemplateDefaultOrg,
                                      @Value("${consent.template.default.workspace:#{null}}") String consentTemplateDefaultWorkspace) {
        this.dataConsumerId = dataConsumerId;
        tenantManagerConfigCacheService = TenantConfigCacheBuilder.createNew()
                .tenantManagerBaseUrl(tenantCfgServiceBaseUrl)
                .cacheType("caffeine")
                .useFinarkeinCreds(true)
                .build();
        this.consentTemplateDefaultOrg = consentTemplateDefaultOrg;
        this.consentTemplateDefaultWorkspace = consentTemplateDefaultWorkspace;
    }

    @Override
    public Mono<ConsentTemplateResponse> saveConsentTemplate(ConsentTemplate consentTemplate) {
        return Mono.just(consentTemplate)
                .doOnNext(input -> log.debug("SaveConsentTemplate: start: request:{}", input))
                .flatMap(this::doSaveConsentTemplate)
                .doOnSuccess(response -> log.debug("SaveConsentTemplate: success: consentTemplateId:{}",
                        response.getConsentTemplateId()))
                .doOnError(error -> log.error("SaveConsentTemplate: error:{}", error.getMessage(), error));
    }

    private Mono<ConsentTemplateResponse> doSaveConsentTemplate(ConsentTemplate consentTemplate) {
        consentTemplateValidator.validateConsentTemplate(consentTemplate, Functions.UUIDSupplier.get());
        consentTemplate.setId(UUIDSupplier.get());
        return TenantInfoHolder.getTenantInfo()
                .flatMap(tenantInfo ->
                        tenantManagerConfigCacheService
                                .getTenantConfigById(Objects
                                        .requireNonNull(consentTemplateDefaultOrg,
                                                tenantInfo.getOrg()), Objects
                                        .requireNonNull(consentTemplateDefaultWorkspace,
                                                tenantInfo.getWorkspace()), consentTemplate.getId())
                                .map(tenantConfig ->
                                        getConfigMap(consentTemplate)
                                )
                                .switchIfEmpty(Mono.defer(() ->
                                        Mono.just(getConfigMap(consentTemplate))
                                ))
                                .flatMap(stringObjectMap ->

                                        tenantManagerConfigCacheService
                                                .saveTenantConfig(TenantConfigSaveRequest.builder()
                                                        .tenantId(Objects
                                                                .requireNonNull(consentTemplateDefaultOrg,
                                                                        tenantInfo.getOrg()))
                                                        .workspaceId(Objects
                                                                .requireNonNull(consentTemplateDefaultWorkspace,
                                                                        tenantInfo.getWorkspace()))
                                                        .configId(consentTemplate.getId())
                                                        .configType(CONFIG_TYPE)
                                                        .config(stringObjectMap)
                                                        .build())
                                                .map(tenantConfigCrudResponse -> new ConsentTemplateResponse(consentTemplate.getId()))
                                )
                );
    }

    private Map<String, Object> getConfigMap(ConsentTemplate consentTemplate) {
        Map<String, Object> map = new HashMap<>(2);
        map.put(consentTemplate.getId(), consentTemplate);
        return map;
    }

    private Mono<Boolean> saveConsentTemplates(List<ConsentTemplate> consentTemplates) {

        List<TenantConfigSaveRequest> saveRequests = new ArrayList<>();

        consentTemplates.forEach(consentTemplate -> saveRequests.add(TenantConfigSaveRequest.builder()
                .tenantId(consentTemplateDefaultOrg)
                .workspaceId(consentTemplateDefaultWorkspace)
                .configId(consentTemplate.getId())
                .config(getConfigMap(consentTemplate))
                .configType(CONFIG_TYPE)
                .build()));

        return tenantManagerConfigCacheService.saveTenantConfig(saveRequests).map(tenantConfigCrudResponse -> true);
    }

    @Override
    public Mono<ConsentTemplate> getConsentTemplate(String id) {

        return TenantInfoHolder.getTenantInfo().flatMap(tenantInfo ->
                tenantManagerConfigCacheService
                        .getTenantConfigById(Objects
                                .requireNonNull(consentTemplateDefaultOrg,
                                        tenantInfo.getOrg()), Objects
                                .requireNonNull(consentTemplateDefaultWorkspace,
                                        tenantInfo.getWorkspace()), id)
                        .flatMap(tenantConfig ->
                                tenantConfig.getConfig().get(id) != null
                                        ? Mono.just(tenantConfig.getConfig().get(id)) : Mono.empty()
                        )
                        .switchIfEmpty(Mono.defer(() -> {
                            throw Errors.InvalidRequest.with(UUIDSupplier.get(),
                                    "ConsentTemplate not found for given consentTemplateId:" + id);
                        }))
                        .map(o -> {
                            if (o instanceof Map)
                                return objectMapper.convertValue(o, ConsentTemplate.class);
                            return (ConsentTemplate) o;
                        })
        );
    }

    @Override
    public Mono<Page<ConsentTemplate>> getAllConsentTemplates(Pageable pageable) {

        return TenantInfoHolder.getTenantInfo()
                .flatMap(tenantInfo ->
                        tenantManagerConfigCacheService
                                .getTenantConfigByIds(Objects
                                                .requireNonNull(consentTemplateDefaultOrg,
                                                        tenantInfo.getOrg()),
                                        Objects
                                                .requireNonNull(consentTemplateDefaultWorkspace,
                                                        tenantInfo.getWorkspace()),
                                        null,
                                        Set.of(CONFIG_TYPE))
                                .map(tenantConfigs -> {

                                    List<ConsentTemplate> consentTemplates = new ArrayList<>();
                                    tenantConfigs.getConfigs().values()
                                            .forEach(tenantConfigBase -> {
                                                Collection<Object> objects = tenantConfigBase.getConfig().values();

                                                objects.forEach(o -> {
                                                    if (o instanceof Map)
                                                        consentTemplates.add(objectMapper.convertValue(o, ConsentTemplate.class));
                                                    else
                                                        consentTemplates.add((ConsentTemplate) o);
                                                });
                                            });
                                    return consentTemplates;
                                })
                )
                .map(consentTemplates -> new PageImpl(consentTemplates, pageable, consentTemplates.size()))
                ;
    }

    @Override
    public Mono<ConsentTemplateDeleteResponse> deleteConsentTemplate(String id) {

        return TenantInfoHolder.getTenantInfo().flatMap(tenantInfo ->

                tenantManagerConfigCacheService.deleteTenantConfigById(Objects
                                .requireNonNull(consentTemplateDefaultOrg,
                                        tenantInfo.getOrg()), Objects
                                .requireNonNull(consentTemplateDefaultWorkspace,
                                        tenantInfo.getWorkspace()), id)
                        .map(tenantConfigCrudResponse -> new ConsentTemplateDeleteResponse(id, true))
                        .switchIfEmpty(Mono.just(new ConsentTemplateDeleteResponse(id, false)))
        );
    }

    private Mono<ConsentTemplate> getConsentTemplate(ConsentRequestInput consentRequestInput) {
        return getConsentTemplate(consentRequestInput.getConsentTemplateId())
                .switchIfEmpty(Mono.defer(() -> {
                    throw Errors.InvalidRequest.with(consentRequestInput.getTxnId(),
                            "ConsentTemplate not found for given consentTemplateId:" + consentRequestInput.getConsentTemplateId());
                }));
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

        Mono<ConsentTemplate> templateMono;

        if (consentRequestInput.getConsentTemplateDefinition() != null) {
            ConsentTemplate consentTemplate = new ConsentTemplate();
            consentTemplate.setConsentVersion("1.1.2");
            consentTemplate.setConsentTemplateDefinition(consentRequestInput.getConsentTemplateDefinition());
            templateMono = Mono.just(consentTemplate);
        } else
            templateMono = getConsentTemplate(consentRequestInput);
        return templateMono.flatMap(consentTemplate -> {
            final var consentTemplateDefinition = consentTemplate.getConsentTemplateDefinition();
            var consentDetail = prepareConsentDetailFromTemplate(consentTemplateDefinition, consentRequestInput.getCustomerId());

            return consentService.createConsent(FIUConsentRequest.builder()
                    .ver(consentTemplate.getConsentVersion())
                    .txnId(consentRequestInput.getTxnId())
                    .timestamp(currentTimestampSupplier.get())
                    .consentDetail(consentDetail)
                    .callback(consentRequestInput.getCallback() != null
                            ? consentRequestInput.getCallback() : consentTemplateDefinition.getCallback())
                    .webhooks(consentRequestInput.getWebhooks())
                    .keyIdentifier(consentRequestInput.getKeyIdentifier())
                    .tenant(consentRequestInput.getTenant())
                    .workspace(consentRequestInput.getWorkspace())
                    .build());
        });
    }

    @Override
    public Mono<ConsentDetail> prepareConsentDetailsFromTemplate(ConsentRequestInput consentRequestInput) {
        consentRequestInputValidator.validateConsentRequestInput(consentRequestInput);

        var templateMono = getConsentTemplate(consentRequestInput);
        return templateMono.map(consentTemplate ->
                prepareConsentDetailFromTemplate(consentTemplate.getConsentTemplateDefinition(), consentRequestInput.getCustomerId()));
    }

    @Override
    public Mono<Page<ConsentTemplate>> getConsentTemplatesByQuery(String tag, String consentVersion, Pageable pageRequest) {

        return TenantInfoHolder.getTenantInfo()
                .flatMap(tenantInfo ->
                        tenantManagerConfigCacheService
                                .getTenantConfigByIds(Objects
                                        .requireNonNull(consentTemplateDefaultOrg,
                                                tenantInfo.getOrg()), Objects
                                        .requireNonNull(consentTemplateDefaultWorkspace,
                                                tenantInfo.getWorkspace()), null, Set.of(CONFIG_TYPE))
                                .map(tenantConfigs ->
                                        tenantConfigs.getConfigs().values()
                                                .stream()
                                                .flatMap(tenantConfigBase -> {
                                                    List<ConsentTemplate> consentTemplates = new ArrayList<>();
                                                    tenantConfigBase.getConfig().values().forEach(o -> {

                                                        if (o instanceof Map)
                                                            consentTemplates.add(objectMapper.convertValue(o, ConsentTemplate.class));
                                                        else
                                                            consentTemplates.add((ConsentTemplate) o);
                                                    });
                                                    return consentTemplates.stream();
                                                })
                                                .filter(consentTemplate -> {
                                                    if (tag == null)
                                                        return true;
                                                    return consentTemplate.getTags().matches(".*" + tag + ".*");
                                                })
                                                .filter(consentTemplate -> {
                                                    if (consentVersion == null)
                                                        return true;
                                                    return consentTemplate.getConsentVersion().matches(".*" + consentVersion + ".*");
                                                })
                                                .collect(Collectors.toList())
                                )
                )
                .map(consentTemplates -> new PageImpl(consentTemplates, pageRequest, consentTemplates.size()));
    }

    @Override
    public ConsentTemplateDefinition mergeConsentTemplates(ConsentTemplateDefinition baseConsentTemplateDefinition,
                                                           ConsentTemplateDefinition overridingConsentTemplateDefinition) {

        if (overridingConsentTemplateDefinition == null)
            return baseConsentTemplateDefinition;
        if (overridingConsentTemplateDefinition.getConsentStartOffset() != null
                && !overridingConsentTemplateDefinition.getConsentStartOffset().isEmpty())
            baseConsentTemplateDefinition.setConsentStartOffset(overridingConsentTemplateDefinition.getConsentStartOffset());
        if (overridingConsentTemplateDefinition.getConsentExpiryDuration() != null
                && !overridingConsentTemplateDefinition.getConsentExpiryDuration().isEmpty())
            baseConsentTemplateDefinition.setConsentExpiryDuration(overridingConsentTemplateDefinition.getConsentExpiryDuration());
        if (overridingConsentTemplateDefinition.getConsentMode() != null)
            baseConsentTemplateDefinition.setConsentMode(overridingConsentTemplateDefinition.getConsentMode());
        if (overridingConsentTemplateDefinition.getConsentTypes() != null)
            baseConsentTemplateDefinition.setConsentTypes(overridingConsentTemplateDefinition.getConsentTypes());
        if (overridingConsentTemplateDefinition.getFiTypes() != null)
            baseConsentTemplateDefinition.setFiTypes(overridingConsentTemplateDefinition.getFiTypes());
        if (overridingConsentTemplateDefinition.getPurposeCode() != null
                && !overridingConsentTemplateDefinition.getPurposeCode().isEmpty())
            baseConsentTemplateDefinition.setPurposeCode(overridingConsentTemplateDefinition.getPurposeCode());
        if (overridingConsentTemplateDefinition.getFetchType() != null
                && !overridingConsentTemplateDefinition.getFetchType().isEmpty())
            baseConsentTemplateDefinition.setFetchType(overridingConsentTemplateDefinition.getFetchType());
        if (overridingConsentTemplateDefinition.getFrequency() != null
                && !overridingConsentTemplateDefinition.getFrequency().isEmpty())
            baseConsentTemplateDefinition.setFrequency(overridingConsentTemplateDefinition.getFrequency());
        if (overridingConsentTemplateDefinition.getDataLife() != null
                && !overridingConsentTemplateDefinition.getDataLife().isEmpty())
            baseConsentTemplateDefinition.setDataLife(overridingConsentTemplateDefinition.getDataLife());
        if (overridingConsentTemplateDefinition.getConsentTemplateDataRange() != null)
            baseConsentTemplateDefinition.setConsentTemplateDataRange(overridingConsentTemplateDefinition.getConsentTemplateDataRange());
        if (overridingConsentTemplateDefinition.getDataFilter() != null)
            baseConsentTemplateDefinition.setDataFilter(overridingConsentTemplateDefinition.getDataFilter());
        if (overridingConsentTemplateDefinition.getCallback() != null)
            baseConsentTemplateDefinition.setCallback(overridingConsentTemplateDefinition.getCallback());

        return baseConsentTemplateDefinition;
    }

    private ConsentDetail prepareConsentDetailFromTemplate(ConsentTemplateDefinition consentTemplateDefinition, String customerId) {
        ConsentDetail consentDetail = new ConsentDetail();

        String consentStart = Functions.currentTimestampSupplier.get();
        int currentYear = LocalDateTime.now().getYear();
        String[] strings = generateConsentDateRange(consentTemplateDefinition.getConsentStartOffset(),
                consentTemplateDefinition.getConsentExpiryDuration(), consentStart);
        consentDetail.setConsentStart(strings[0]);
        consentDetail.setConsentExpiry(strings[1]);
        consentDetail.setFIDataRange(generateFIDataRange(consentTemplateDefinition.getConsentTemplateDataRange(),
                strings[0], strings[1], currentYear));
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

    @PostConstruct
    void pushConsentTemplateToConfigManager() {

        List<ConsentTemplate> consentTemplates = consentTemplateRepository.findAll();
        if (consentTemplates.isEmpty()) {
            log.debug("No consent templates found in local storage");
            return;
        }
        saveConsentTemplates(consentTemplates)
                .subscribe(saveSuccessful -> {
                    log.debug("Consent template push successful, deleting the local templates");
                    consentTemplateRepository.deleteAll();
                });
    }
}
