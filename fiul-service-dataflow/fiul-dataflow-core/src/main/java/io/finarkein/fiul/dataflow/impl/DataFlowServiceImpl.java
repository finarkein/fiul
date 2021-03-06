/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.impl;

import io.finarkein.api.aa.consent.ConsentMode;
import io.finarkein.api.aa.consent.DataLife;
import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.config.DBCallHandlerSchedulerConfig;
import io.finarkein.fiul.consent.ConsentShortMeta;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.dataflow.ConsentServiceClient;
import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.dto.FIDataDeleteResponse;
import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import io.finarkein.fiul.dataflow.dto.FIRequestDTO;
import io.finarkein.fiul.dataflow.dto.FIRequestState;
import io.finarkein.fiul.dataflow.easy.DataSaveRequest;
import io.finarkein.fiul.dataflow.store.AAFIDataStore;
import io.finarkein.fiul.dataflow.store.FIFetchMetadataStore;
import io.finarkein.fiul.dataflow.store.FIRequestStore;
import io.finarkein.fiul.ext.Callback;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import io.finarkein.fiul.notification.callback.model.FICallback;
import io.finarkein.fiul.validation.FIRequestValidator;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static io.finarkein.api.aa.consent.ConsentMode.STORE;
import static io.finarkein.api.aa.util.Functions.*;

@Log4j2
@Service
public class DataFlowServiceImpl implements DataFlowService {
    protected final AAFIUClient fiuClient;
    protected final FIRequestStore fiRequestStore;
    protected final AAFIDataStore aafiDataStore;
    protected final FIFetchMetadataStore fiFetchMetadataStore;
    protected final CallbackRegistry callbackRegistry;
    protected final ConsentServiceClient consentServiceClient;
    protected final Scheduler postResponseProcessingScheduler;

    @Autowired
    protected DataFlowServiceImpl(AAFIUClient fiuClient, FIRequestStore fiRequestStore,
                                  FIFetchMetadataStore fiFetchMetadataStore,
                                  AAFIDataStore aafiDataStore,
                                  CallbackRegistry callbackRegistry,
                                  ConsentServiceClient consentServiceClient,
                                  DBCallHandlerSchedulerConfig schedulerConfig) {
        this.fiuClient = fiuClient;
        this.fiRequestStore = fiRequestStore;
        this.fiFetchMetadataStore = fiFetchMetadataStore;
        this.aafiDataStore = aafiDataStore;
        this.callbackRegistry = callbackRegistry;
        this.consentServiceClient = consentServiceClient;
        this.postResponseProcessingScheduler = schedulerConfig.getScheduler();
    }

    @Override
    public Mono<FIRequestResponse> createFIRequest(FIUFIRequest fiRequest, String aaName) {
        validateFIUFIParam(fiRequest);

        ConsentStateDTO consentState = null;
        if (aaName == null) {
            consentState = consentServiceClient.getConsentStateByConsentId(fiRequest.getConsent().getId());
            if (consentState != null)
                aaName = consentState.getAaId();
        }
        if (aaName != null) {
            return doCreateFIRequest(fiRequest, aaName, consentState);
        }

        return Mono.error(() ->
                Errors.InvalidRequest.with(fiRequest.getTxnid(),
                        "Cannot find aaHandle to create FIRequest, please try with aaHandle"))
                ;
    }

    private void validateFIUFIParam(FIUFIRequest fiRequest) {
        final String txnId = FIRequestValidator.validateTxnId(fiRequest);
        FIRequestValidator.validateConsentId(fiRequest, txnId);
    }

    protected Mono<FIRequestResponse> doCreateFIRequest(FIUFIRequest fiRequest, String aaName, final ConsentStateDTO retrievedConsentState) {
        log.debug("SubmitFIRequest: start: request:{}", fiRequest);
        final var fiRequestStartTime = Timestamp.from(Instant.now());

        return consentServiceClient.setSignatureIfNotSet(fiRequest)
                .flatMap(fiufiRequest -> fiuClient.createFIRequest(fiufiRequest, aaName)
                        .publishOn(postResponseProcessingScheduler)
                        .doOnSuccess(response -> {
                            final var builder = FIFetchMetadata.builder()
                                    .txnId(fiufiRequest.getTxnid())
                                    .aaName(aaName)
                                    .consentId(response.getConsentId())
                                    .sessionId(response.getSessionId())
                                    .fiDataRangeFrom(strToTimeStamp.apply(fiufiRequest.getFIDataRange().getFrom()))
                                    .fiDataRangeTo(strToTimeStamp.apply(fiufiRequest.getFIDataRange().getTo()))
                                    .fiRequestSubmittedOn(fiRequestStartTime);

                            ConsentStateDTO consentState = retrievedConsentState;
                            if (consentState == null)
                                consentState = consentServiceClient.getConsentStateByConsentId(response.getConsentId());
                            if (consentState != null)
                                builder.consentHandleId(consentState.getConsentHandle());

                            fiRequestStore.saveFIRequestAndFetchMetadata(builder.build(), fiufiRequest);
                            log.debug("SubmitFIRequest: success: response:{}", response);
                        })
                        .doOnSuccess(saveRegisterCallback(fiufiRequest.getCallback()))
                        .doOnError(SystemException.class, error -> {
                            fiRequestStore.updateFIRequestStateOnError(fiufiRequest, aaName, fiRequestStartTime, error.getParamValue("dataSessionId"));
                            log.error("SubmitFIRequest: error: {}", error.getMessage(), error);
                        }))
                ;
    }

    protected Consumer<FIRequestResponse> saveRegisterCallback(Callback fiCallback) {
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
                        .publishOn(postResponseProcessingScheduler)
                        .doOnSuccess(saveDataIfStoreConsentMode(input.dataSessionId, input.aaName))
                        .doOnSuccess(updateFetchMetadata(input.dataSessionId, input.fipId, input.linkRefNumbers, input.startTime))
                        .doOnSuccess(response -> log.debug("FIFetch: success: sessionId:{}, aaName:{}, fipId:{}, linkRefNumbers:{}",
                                input.dataSessionId, input.aaName, input.fipId, input.linkRefNumbers))
                        .doOnError(error -> log.error("FIFetch: error: {}", error.getMessage(), error))
                )
                .switchIfEmpty(Mono.error(Errors.InvalidRequest
                        .with(uuidSupplier.get(), "Cannot find aaName to FI fetch, please try with aaName")));
    }

    protected Consumer<FIFetchResponse> updateFetchMetadata(String sessionId, String fipId, String[] linkRefNumbers, Timestamp fiFetchStartTime) {
        return fiFetchResponse -> {
            var metadataBuilder = FIFetchMetadata.builder()
                    .sessionId(sessionId)
                    .fiFetchSubmittedOn(fiFetchStartTime);
            var commaSeparatedLinkRefNumbers = Arrays.isNullOrEmpty(linkRefNumbers) ?
                    null : String.join(",", linkRefNumbers);

            if (fipId != null && commaSeparatedLinkRefNumbers != null)
                metadataBuilder.fipId(fipId).linkRefNumbers(commaSeparatedLinkRefNumbers);

            metadataBuilder.fiFetchCompletedOn(Timestamp.from(Instant.now()));
            int rowsUpdated = fiFetchMetadataStore.updateSuccessFetchMetadata(metadataBuilder.build());
            log.debug("FI-Fetch-Metadata #OfRows updated:{}, sessionId:{}", rowsUpdated, sessionId);
        };
    }

    protected Consumer<FIFetchResponse> saveDataIfStoreConsentMode(String sessionId, String aaName) {
        return fiFetchResponse -> {
            final var fiRequest = fiRequestStore.getFIRequestByAANameAndSessionId(sessionId, aaName);
            final var consentId = fiRequest.map(FIRequestDTO::getConsentId).orElse(null);
            final Mono<ConsentShortMeta> consentMetaMono = consentServiceClient.getConsentMeta(consentId, aaName);
            consentMetaMono.subscribe(consentMeta -> {
                if (consentId == null || ConsentMode.get(consentMeta.getMode()) != STORE)
                    return;
                final DataLife dataLife = new DataLife(consentMeta.getDataLifeUnit(), consentMeta.getDataLifeValue());
                aafiDataStore.saveFIData(DataSaveRequest.with(fiFetchResponse)
                        .consentId(consentId)
                        .sessionId(sessionId)
                        .aaName(aaName)
                        .dataLife(dataLife)
                        .dataLifeExpireOn(calculateExpireTime.apply(dataLife)).build());
            });
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
    public Mono<FIDataDeleteResponse> deleteDataForSession(String dataSessionId) {
        FIDataDeleteResponse response = new FIDataDeleteResponse(dataSessionId, null, null, false);
        log.debug("Deleting data for sessionId:{}", dataSessionId);

        fiFetchMetadataStore.deleteBySessionId(dataSessionId);

        final var deletionCounts = aafiDataStore.deleteFIDataBySessionId(dataSessionId);
        if (deletionCounts.isEmpty()) {
            log.debug("Data not present for sessionId:{}", dataSessionId);
            return Mono.just(response);
        }
        response.setDeleted(true);
        log.debug("Data deleted for sessionId:{}, deletionCounts:{}", dataSessionId, deletionCounts);
        return Mono.just(response);
    }

    @Override
    public Mono<FIDataDeleteResponse> deleteDataByConsentId(String consentId) {
        log.debug("Deleting data for consentId:{}", consentId);
        FIDataDeleteResponse response = new FIDataDeleteResponse(null, null, consentId, false);

        fiFetchMetadataStore.deleteByConsentId(consentId);

        final var deletionCounts = aafiDataStore.deleteFIDataByConsentId(consentId);
        if (deletionCounts.isEmpty()) {
            log.debug("Data not present for consentId:{}", consentId);
            return Mono.just(response);
        }
        response.setDeleted(true);
        log.debug("Data deleted for consentId:{}, deletionCounts:{}", consentId, deletionCounts);
        return Mono.just(response);
    }

    @Override
    public Mono<Boolean> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp) {
        final var deletionCounts = aafiDataStore.deleteByDataLifeExpireOnBefore(triggerTimestamp);
        if (deletionCounts.isEmpty())
            return Mono.just(Boolean.FALSE);
        log.debug("delete-data-by-dataLife-expire-on-before deletionCounts:{}", deletionCounts);
        return Mono.just(Boolean.TRUE);
    }

    @Override
    public Optional<FIRequestState> getFIRequestStateByTxnId(String txnId) {
        return fiRequestStore.getFIRequestStateByTxnId(txnId);
    }

    @Override
    public Optional<FIRequestState> getFIRequestStateBySessionId(String sessionId) {
        return fiRequestStore.getFIRequestStateBySessionId(sessionId);
    }

    @Data
    @Accessors(fluent = true)
    protected static class FIFetchInput {

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