/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.impl;

import io.finarkein.api.aa.consent.ConsentMode;
import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.consent.model.ConsentRequestDTO;
import io.finarkein.fiul.consent.model.ConsentState;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import io.finarkein.fiul.dataflow.dto.FIRequestDTO;
import io.finarkein.fiul.dataflow.easy.DataSaveRequest;
import io.finarkein.fiul.dataflow.store.AAFIDataStore;
import io.finarkein.fiul.dataflow.store.FIFetchMetadataStore;
import io.finarkein.fiul.dataflow.store.FIRequestStore;
import io.finarkein.fiul.ext.Callback;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import io.finarkein.fiul.notification.callback.model.FICallback;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static io.finarkein.api.aa.consent.ConsentMode.STORE;
import static io.finarkein.api.aa.util.Functions.*;

@Log4j2
@Service
class DataFlowServiceImpl implements DataFlowService {
    private final AAFIUClient fiuClient;
    private final FIRequestStore fiRequestStore;
    private final AAFIDataStore aafiDataStore;
    private final FIFetchMetadataStore fiFetchMetadataStore;
    private final ConsentService consentService;
    private final CallbackRegistry callbackRegistry;

    @Autowired
    DataFlowServiceImpl(AAFIUClient fiuClient, FIRequestStore fiRequestStore,
                        FIFetchMetadataStore fiFetchMetadataStore,
                        ConsentService consentService,
                        AAFIDataStore aafiDataStore,
                        CallbackRegistry callbackRegistry) {
        this.fiuClient = fiuClient;
        this.fiRequestStore = fiRequestStore;
        this.fiFetchMetadataStore = fiFetchMetadataStore;
        this.consentService = consentService;
        this.aafiDataStore = aafiDataStore;
        this.callbackRegistry = callbackRegistry;
    }

    @Override
    public Mono<FIRequestResponse> createFIRequest(FIUFIRequest fiRequest, String aaName) {
        Optional<String> aaNameOptional = aaName != null ? Optional.of(aaName) : Optional.empty();
        return aaNameOptional
                .or(() -> consentService
                        .getConsentRequestByConsentId(fiRequest.getConsent().getId())
                        .map(ConsentRequestDTO::getAaName))
                .map(aaId -> doCreateFIRequest(fiRequest, aaId))
                .orElseThrow(() -> Errors
                        .InvalidRequest
                        .with(fiRequest.getTxnid(), "Cannot find aaName to create FIRequest, please try with aaName"))
                ;
    }

    private Mono<FIRequestResponse> doCreateFIRequest(FIUFIRequest fiRequest, String aaName) {
        log.debug("SubmitFIRequest: start: request:{}", fiRequest);
        final var fiRequestStartTime = Timestamp.from(Instant.now());

        return fiuClient.createFIRequest(fiRequest, aaName)
                .doOnSuccess(response -> {
                    final var fiFetchMetadata = FIFetchMetadata.builder()
                            .txnId(fiRequest.getTxnid())
                            .aaName(aaName)
                            .consentId(response.getConsentId())
                            .sessionId(response.getSessionId())
                            .fiDataRangeFrom(strToTimeStamp.apply(fiRequest.getFIDataRange().getFrom()))
                            .fiDataRangeTo(strToTimeStamp.apply(fiRequest.getFIDataRange().getTo()))
                            .fiRequestSubmittedOn(fiRequestStartTime)
                            .build();
                    fiRequestStore.saveFIRequestAndFetchMetadata(fiFetchMetadata, fiRequest);
                    log.debug("SubmitFIRequest: success: response:{}", response);
                    consentService.updateConsentStateDataSession(response.getTxnid(), response.getSessionId(), true);
                })
                .doOnSuccess(saveRegisterCallback(fiRequest.getCallback()))
                .doOnError(SystemException.class, error -> {
                    consentService.updateConsentStateDataSession(error.txnId(), null, false);
                    log.error("SubmitFIRequest: error: {}", error.getMessage(), error);
                });
    }

    private Consumer<FIRequestResponse> saveRegisterCallback(Callback fiCallback) {
        return response -> {
            if (Objects.isNull(fiCallback) || Objects.isNull(fiCallback.getUrl()))
                return;
            callbackRegistry.registerFICallback(FICallback.builder()
                    .sessionId(response.getSessionId())
                    .consentId(response.getConsentId())
                    .callbackUrl(fiCallback.getUrl())
                    .build());
        };
    }

    @Override
    public Mono<FIFetchResponse> fiFetch(String dataSessionId, String aaName, String fipId, String[] linkRefNumbers) {
        return Mono.just(new FIFetchInput(dataSessionId, aaName, fipId, linkRefNumbers, Timestamp.from(Instant.now())))
                .doOnNext(input -> log.debug("FIFetch: start: sessionId:{}, aaName:{}, fipId:{}, linkRefNumbers:{}",
                        input.dataSessionId, input.aaName, input.fipId, input.linkRefNumbers))
                .flatMap(input -> {
                    if (input.aaName != null)
                        return Mono.just(input);
                    return fiRequestStore.getFIRequestBySessionId(input.dataSessionId)
                            .map(fiRequestDTO -> Mono.just(input.aaName(fiRequestDTO.getAaName())))
                            .orElseGet(Mono::empty);
                })
                .flatMap(input -> fiuClient
                        .fiFetch(input.dataSessionId, input.aaName, input.fipId, input.linkRefNumbers)
                        .doOnSuccess(saveDataIfStoreConsentMode(input.dataSessionId, input.aaName))
                        .doOnSuccess(updateFetchMetadata(input.dataSessionId, input.fipId, input.linkRefNumbers, input.startTime))
                        .doOnSuccess(response -> log.debug("FIFetch: success: sessionId:{}, aaName:{}, fipId:{}, linkRefNumbers:{}",
                                input.dataSessionId, input.aaName, input.fipId, input.linkRefNumbers))
                        .doOnError(error -> log.error("FIFetch: error: {}", error.getMessage(), error))
                )
                .switchIfEmpty(Mono.error(Errors.InvalidRequest
                        .with(uuidSupplier.get(), "Cannot find aaName to FI fetch, please try with aaName")));
    }

    private Consumer<FIFetchResponse> updateFetchMetadata(String sessionId, String fipId, String[] linkRefNumbers, Timestamp fiFetchStartTime) {
        return fiFetchResponse -> {
            var metadataBuilder = FIFetchMetadata.builder()
                    .sessionId(sessionId)
                    .fiFetchSubmittedOn(fiFetchStartTime);
            var commaSeparatedLinkRefNumbers = (linkRefNumbers != null && linkRefNumbers.length > 0) ?
                    String.join(",", linkRefNumbers) : null;

            if (fipId != null && commaSeparatedLinkRefNumbers != null)
                metadataBuilder.fipId(fipId).linkRefNumbers(commaSeparatedLinkRefNumbers);

            metadataBuilder.fiFetchCompletedOn(Timestamp.from(Instant.now()));
            int rowsUpdated = fiFetchMetadataStore.updateSuccessFetchMetadata(metadataBuilder.build());
            log.debug("FI-Fetch-Metadata #OfRows updated:{}, sessionId:{}", rowsUpdated, sessionId);
        };
    }

    private Consumer<FIFetchResponse> saveDataIfStoreConsentMode(String sessionId, String aaName) {
        return fiFetchResponse -> {
            final var fiRequest = fiRequestStore.getFIRequestByAANameAndSessionId(sessionId, aaName);
            final var consentId = fiRequest.map(FIRequestDTO::getConsentId).orElse(null);
            final var consentDetail = consentService.consentDetail(consentId, aaName);
            if (consentId == null || ConsentMode.get(consentDetail.getConsentMode()) != STORE)
                return;

            aafiDataStore.saveFIData(DataSaveRequest.with(fiFetchResponse)
                    .consentId(consentId)
                    .sessionId(sessionId)
                    .aaName(aaName)
                    .dataLife(consentDetail.getDataLife())
                    .dataLifeExpireOn(calculateExpireTime.apply(consentDetail.getDataLife())).build());
        };
    }

    @Override
    public Mono<FIFetchResponse> fiGet(String sessionId, String fipId, String[] linkRefNumber) {
        if (fipId != null)
            log.debug("GetStoredFIData: start: sessionId:{}, fipId:{}, linkRefNumber:{}", sessionId, fipId, linkRefNumber);
        else
            log.debug("GetStoredFIData: start: sessionId:{}", sessionId);

        return fiFetchMetadataStore.getFIFetchMetadata(sessionId)
                .map(metadata -> Mono
                        .just(aafiDataStore.getFIData(metadata.getConsentId(), metadata.getSessionId(), metadata.getAaName(), fipId, linkRefNumber))
                        .doOnSuccess(response -> {
                                    if (fipId != null)
                                        log.debug("GetStoredFIData: success: sessionId:{}, fipId:{}, linkRefNumber:{}", sessionId, fipId, linkRefNumber);
                                    else
                                        log.debug("GetStoredFIData: success: sessionId:{}", sessionId);
                                }
                        ).doOnError(error -> log.error("GetStoredData: error: sessionId:{}, error:{}", sessionId, error.getMessage(), error)))
                .orElseGet(Mono::empty);
    }

    @Override
    public Mono<Boolean> deleteDataForSession(String dataSessionId) {
        log.debug("Deleting data for sessionId:{}", dataSessionId);
        final var deletionCounts = aafiDataStore.deleteFIDataBySessionId(dataSessionId);
        if (deletionCounts.isEmpty()) {
            log.debug("Data not present for sessionId:{}", dataSessionId);
            return Mono.just(Boolean.FALSE);
        }
        log.debug("Data deleted for sessionId:{}, deletionCounts:{}", dataSessionId, deletionCounts);
        return Mono.just(Boolean.TRUE);
    }

    @Override
    public Mono<Boolean> deleteDataByConsentId(String consentId) {
        log.debug("Deleting data for consentId:{}", consentId);

        final var deletionCounts = aafiDataStore.deleteFIDataByConsentId(consentId);
        if (deletionCounts.isEmpty()) {
            log.debug("Data not present for consentId:{}", consentId);
            return Mono.just(Boolean.FALSE);
        }
        log.debug("Data deleted for consentId:{}, deletionCounts:{}", consentId, deletionCounts);
        return Mono.just(Boolean.TRUE);
    }

    @Override
    public Mono<Boolean> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp) {
        final var deletionCounts = aafiDataStore.deleteByDataLifeExpireOnBefore(triggerTimestamp);
        if (deletionCounts.isEmpty())
            return Mono.just(Boolean.FALSE);
        log.debug("delete-data-by-dataLife-expire-on-before deletionCounts:{}", deletionCounts);
        return Mono.just(Boolean.TRUE);
    }

    @Data
    @Accessors(fluent = true)
    static class FIFetchInput {

        private final String dataSessionId;
        private String aaName;
        private final String fipId;
        private final String[] linkRefNumbers;
        private final Timestamp startTime;

        public FIFetchInput(String dataSessionId, String aaName, String fipId, String[] linkRefNumbers, Timestamp startTime) {
            this.dataSessionId = dataSessionId;
            this.aaName = aaName;
            this.fipId = fipId;
            this.linkRefNumbers = linkRefNumbers;
            this.startTime = startTime;
        }
    }
}