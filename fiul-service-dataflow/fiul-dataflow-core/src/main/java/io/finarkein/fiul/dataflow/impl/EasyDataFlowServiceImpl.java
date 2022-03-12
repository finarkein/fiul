/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.impl;

import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.common.FIDataRange;
import io.finarkein.api.aa.consent.ConsentMode;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.api.aa.dataflow.Consent;
import io.finarkein.api.aa.dataflow.FIRequest;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.config.AAResponseHandlerConfig;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.converter.xml.XMLConverterFunctions;
import io.finarkein.fiul.dataflow.*;
import io.finarkein.fiul.dataflow.dto.FIDataDeleteResponse;
import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import io.finarkein.fiul.dataflow.dto.FIRequestDTO;
import io.finarkein.fiul.dataflow.easy.DataRequestStatus;
import io.finarkein.fiul.dataflow.easy.DataSaveRequest;
import io.finarkein.fiul.dataflow.easy.SessionStatus;
import io.finarkein.fiul.dataflow.easy.dto.KeyMaterialDataKey;
import io.finarkein.fiul.dataflow.response.decrypt.FIData;
import io.finarkein.fiul.dataflow.response.decrypt.FIDataI;
import io.finarkein.fiul.dataflow.response.decrypt.FIDataOutputFormat;
import io.finarkein.fiul.dataflow.store.EasyFIDataStore;
import io.finarkein.fiul.dataflow.store.FIFetchMetadataStore;
import io.finarkein.fiul.dataflow.store.FIRequestStore;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import io.finarkein.fiul.notification.callback.model.FICallback;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.finarkein.api.aa.consent.ConsentMode.STORE;
import static io.finarkein.api.aa.util.Functions.*;
import static io.finarkein.fiul.Functions.UUIDSupplier;

@Log4j2
@Service
public class EasyDataFlowServiceImpl implements EasyDataFlowService {

    protected final AAFIUClient fiuClient;
    protected final FIRequestStore fiRequestStore;
    protected final EasyFIDataStore easyFIDataStore;
    protected final CallbackRegistry callbackRegistry;
    protected final FIFetchMetadataStore fiFetchMetadataStore;
    protected final ConsentServiceClient consentServiceClient;
    protected final Scheduler postResponseProcessingScheduler;

    @Autowired
    protected EasyDataFlowServiceImpl(AAFIUClient fiuClient, FIRequestStore fiRequestStore,
                                      FIFetchMetadataStore fiFetchMetadataStore,
                                      EasyFIDataStore easyFIDataStore,
                                      CallbackRegistry callbackRegistry,
                                      ConsentServiceClient consentServiceClient,
                                      AAResponseHandlerConfig schedulerConfig) {
        this.fiuClient = fiuClient;
        this.fiRequestStore = fiRequestStore;
        this.fiFetchMetadataStore = fiFetchMetadataStore;
        this.easyFIDataStore = easyFIDataStore;
        this.callbackRegistry = callbackRegistry;
        this.consentServiceClient = consentServiceClient;
        this.postResponseProcessingScheduler = schedulerConfig.getScheduler();
    }

    @Override
    public Mono<DataRequestResponse> createDataRequest(DataRequest dataRequestInput) {
        DataRequestInternal dataRequest = new DataRequestInternal(dataRequestInput);
        log.debug("CreateDataRequest: start: request:{}", dataRequest);
        return Mono.just(validateDataRequest(dataRequest))
                .then(doCreateDataRequest(dataRequest))
                .doOnSuccess(response -> log.debug("CreateDataRequest: submitted: response:{}", response))
                .doOnError(error -> log.error("CreateDataRequest: error: request:{}", error.getMessage(), error));
    }

    protected Mono<DataRequestResponse> doCreateDataRequest(DataRequestInternal dataRequest) {
        final var startTime = Timestamp.from(Instant.now());
        final var serializedKeyPair = fiuClient.getOrCreateKeyMaterial();

        Mono<ConsentStateDTO> consentStateMono = consentServiceClient
                .getConsentState(dataRequest.getConsentHandle(), Optional.ofNullable(dataRequest.getCustomerAAId()))
                .flatMap(consentState -> {
                    if (consentState.getConsentId() == null) {
                        return consentServiceClient
                                .getConsentStatus(consentState.getConsentHandle(), Optional.of(consentState.getAaId()))
                                .flatMap(consentHandleResponse -> {
                                            if (!Objects.equals(consentHandleResponse.getConsentStatus().getStatus(), "READY"))
                                                return Mono.error(Errors.InvalidRequest.with(dataRequest.getTxnId(),
                                                        "ConsentStatus is not READY"));
                                            consentState.setConsentId(consentHandleResponse.getConsentStatus().getId());
                                            return Mono.just(consentState);
                                        }
                                );
                    }
                    return Mono.just(consentState);
                });

        return consentStateMono
                .map(consentState -> {
                    //set consentState on dataRequest
                    dataRequest.setConsentId(consentState.getConsentId());
                    if (dataRequest.getCustomerAAId() == null && consentState.getCustomerAAId() != null)
                        dataRequest.setCustomerAAId(consentState.getCustomerAAId());

                    return FIUFIRequest.builder()
                            .ver(FIRequest.VERSION)
                            .aaHandle(consentState.getAaId())
                            .txnid(dataRequest.getTxnId())
                            .timestamp(timestampToStr.apply(startTime))
                            .consent(new Consent(dataRequest.getConsentId(), null))
                            .callback(dataRequest.getCallback())
                            .fIDataRange(new FIDataRange(dataRequest.getDataRangeFrom(), dataRequest.getDataRangeTo()))
                            .keyMaterial(serializedKeyPair.getKeyMaterial())
                            .build();
                })
                .flatMap(consentServiceClient::setSignatureIfNotSet)
                .flatMap(fiuFiRequest -> {
                            final var aaName = aaNameExtractor.apply(dataRequest.getCustomerAAId());
                            return fiuClient.createFIRequest(fiuFiRequest, aaName)
                                    .publishOn(postResponseProcessingScheduler)
                                    .map(fiRequestResponse -> new DataRequestResponse(fiRequestResponse.getConsentId(),
                                            fiRequestResponse.getSessionId()))

                                    .map(dataRequestResponse -> {
                                        saveKeyMaterialDataKey(serializedKeyPair, dataRequest).accept(dataRequestResponse);
                                        saveFIRequestAndFetchMetadata(aaName, dataRequest, startTime, fiuFiRequest).accept(dataRequestResponse);

                                        final var fiCallback = dataRequest.getCallback();
                                        if (Objects.isNull(fiCallback) || Objects.isNull(fiCallback.getUrl()))
                                            return dataRequestResponse;
                                        var callback = new FICallback();
                                        callback.setSessionId(dataRequestResponse.getSessionId());
                                        callback.setConsentId(dataRequestResponse.getConsentId());
                                        callback.setCallbackUrl(fiCallback.getUrl());
                                        callbackRegistry.registerFICallback(callback);

                                        return dataRequestResponse;
                                    });
                        }
                );
    }

    protected boolean validateDataRequest(DataRequestInternal dataRequest) {
        String txnId = dataRequest.getTxnId();
        ArgsValidator.checkNotEmpty(txnId, dataRequest.getConsentHandle(), "consentHandle");
        ArgsValidator.checkTimestamp(txnId, dataRequest.getDataRangeFrom(), "dataRangeFrom");
        ArgsValidator.checkTimestamp(txnId, dataRequest.getDataRangeTo(), "dataRangeTo");
        ArgsValidator.validateDateRange(txnId, dataRequest.getDataRangeFrom(), dataRequest.getDataRangeTo());

        if (dataRequest.getCallback() != null)
            ArgsValidator.checkNotEmpty(txnId, dataRequest.getCallback().getUrl(), "Callback.Url");
        return true;
    }

    protected Consumer<DataRequestResponse> saveFIRequestAndFetchMetadata(String aaName, DataRequestInternal dataRequest,
                                                                          Timestamp startTime, FIUFIRequest fiufiRequest) {
        return response -> {
            final var fiFetchMetadata = FIFetchMetadata.builder()
                    .aaName(aaName)
                    .consentHandleId(dataRequest.getConsentHandle())
                    .consentId(response.getConsentId())
                    .sessionId(response.getSessionId())
                    .fiDataRangeFrom(strToTimeStamp.apply(dataRequest.getDataRangeFrom()))
                    .fiDataRangeTo(strToTimeStamp.apply(dataRequest.getDataRangeTo()))
                    .txnId(dataRequest.getTxnId())
                    .fiRequestSubmittedOn(startTime)
                    .easyDataFlow(true)
                    .build();
            fiRequestStore.saveFIRequestAndFetchMetadata(fiFetchMetadata, fiufiRequest);
        };
    }

    protected Consumer<DataRequestResponse> saveKeyMaterialDataKey(SerializedKeyPair serializedKeyPair,
                                                                   DataRequest dataRequest) {
        //TODO encrypt privateKey while storing
        return response -> easyFIDataStore.saveKey(KeyMaterialDataKey.builder()
                .consentHandleId(dataRequest.getConsentHandle())
                .consentId(response.getConsentId())
                .sessionId(response.getSessionId())
                .encryptedKey(serializedKeyPair.getPrivateKey()).build());
    }

    @Override
    public Mono<FIDataI> fetchData(String consentHandleId, String sessionId, FIDataOutputFormat fiDataOutputFormat) {
        log.debug("FetchData: start: consentHandleId:{}, sessionId:{}", consentHandleId, sessionId);
        final var fetchDataStartTime = Timestamp.from(Instant.now());
        final var fiRequestDTO = validateAndGetFIRequest(consentHandleId, sessionId);
        requireDataKeyPresent(consentHandleId, sessionId);

        return fiuClient
                .fiFetch(sessionId, fiRequestDTO.getAaName())
                .publishOn(postResponseProcessingScheduler)
                .flatMap(saveDataIfStoreConsentMode(fiRequestDTO, consentHandleId, sessionId, fiDataOutputFormat))
                .doOnSuccess(updateFetchMetadata(sessionId, fetchDataStartTime))
                .doOnSuccess(response -> log.debug("FetchData: success: consentHandleId:{}, sessionId:{}", consentHandleId, sessionId))
                .doOnError(error -> log.error("FetchData: error: consentId:{}, consentHandleId:{}, error:{}", consentHandleId, sessionId, error.getMessage(), error))
                ;
    }

    protected Function<FIFetchResponse, Mono<FIDataI>> saveDataIfStoreConsentMode(FIRequestDTO fiRequestDTO,
                                                                                  String consentHandleId, String sessionId,
                                                                                  FIDataOutputFormat fiDataOutputFormat) {
        return fiFetchResponse -> {
            log.debug("inside saveDataIfStoreConsentMode:{},{}", consentHandleId, sessionId);
            return easyFIDataStore
                    .getKeyConsentId(fiRequestDTO.getConsentId(), sessionId)
                    .map(privateKey ->
                            fiuClient
                                    .decryptFIFetchResponse(fiFetchResponse, new SerializedKeyPair(privateKey.getEncryptedKey(), fiRequestDTO.getKeyMaterial()))
                                    .doOnNext(fiFetchResponse1 -> log.debug("data-decrypted:{},{}", consentHandleId, sessionId))
                                    .doOnNext(decryptedResponse -> easyFIDataStore.deleteKey(fiRequestDTO.getConsentId(), sessionId))
                                    .doOnNext(decryptedResponse -> consentServiceClient
                                            .getSignedConsentDetail(fiRequestDTO.getConsentId(), fiRequestDTO.getAaName())
                                            .subscribe(consentDetail -> {
                                                if (ConsentMode.get(consentDetail.getConsentMode()) == STORE) {
                                                    easyFIDataStore.saveFIData(DataSaveRequest.with(decryptedResponse)
                                                            .aaName(fiRequestDTO.getAaName())
                                                            .consentId(fiRequestDTO.getConsentId())
                                                            .consentHandleId(consentHandleId)
                                                            .sessionId(sessionId)
                                                            .dataLife(consentDetail.getDataLife())
                                                            .dataLifeExpireOn(calculateExpireTime.apply(consentDetail.getDataLife()))
                                                            .build());
                                                }
                                            })
                                    )
                                    .flatMap(decryptedResponse -> toFIData(decryptedResponse, fiDataOutputFormat)))
                    .orElseThrow(() -> Errors.InternalError.with(fiFetchResponse.getTxnid(),
                            "Unable to decode FIFetchResponse, data-key not found for consentId:" + fiRequestDTO.getConsentId() + ", sessionId:" + sessionId));
        };
    }

    protected Mono<FIDataI> toFIData(io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse decryptedFIData,
                                     FIDataOutputFormat fiDataOutputFormat) {
        if (FIDataOutputFormat.xml == fiDataOutputFormat)
            return Mono.just(decryptedFIData);
        FIData response = XMLConverterFunctions.fiFetchResponseToObject.apply(decryptedFIData);
        return Mono.just(response);
    }

    protected Consumer<FIDataI> updateFetchMetadata(String sessionId, Timestamp fetchDataStartTime) {
        return fiFetchResponse -> {
            var metadataBuilder = FIFetchMetadata.builder()
                    .sessionId(sessionId)
                    .fiFetchSubmittedOn(fetchDataStartTime);

            metadataBuilder.fiFetchCompletedOn(Timestamp.from(Instant.now()));
            int rowsUpdated = fiFetchMetadataStore.updateSuccessFetchMetadata(metadataBuilder.build());
            log.debug("FI-Fetch-Metadata #OfRows updated:{}", rowsUpdated);
        };
    }

    protected FIRequestDTO validateAndGetFIRequest(String consentHandleId, String sessionId) {
        final String txnId = UUIDSupplier.get();
        ArgsValidator.checkNotEmpty(txnId, consentHandleId, "consentHandleId");
        ArgsValidator.checkNotEmpty(txnId, sessionId, "SessionId");

        final var fiRequestOptional = fiRequestStore.getFIRequest(consentHandleId, sessionId);
        return fiRequestOptional
                .orElseThrow(() -> Errors.InvalidRequest.with(txnId,
                        "FIRequest metadata not found for given consentHandleId:" + consentHandleId
                                + ", sessionId:" + sessionId + " not found"));
    }

    protected void requireDataKeyPresent(String consentHandleId, String sessionId) {
        final var dataKey = easyFIDataStore.getKeyConsentHandleId(consentHandleId, sessionId);
        if (dataKey.isEmpty())
            throw Errors.InvalidRequest.with(UUIDSupplier.get(),
                    "Invalid sessionId, KeyPair not found for consentHandleId:" + consentHandleId
                            + ", sessionId:" + sessionId);
    }

    @Override
    public Mono<DataRequestStatus> dataRequestStatus(String consentHandle, String sessionId) {
        final String txnId = UUIDSupplier.get();
        ArgsValidator.checkNotEmpty(txnId, consentHandle, "consentHandle");
        ArgsValidator.checkNotEmpty(txnId, sessionId, "sessionId");

        log.debug("Getting data request status, sessionId:{}", sessionId);
        return fiRequestStore.getFIRequestState(consentHandle, sessionId)
                .map(fiRequestState -> Mono.just(DataRequestStatus.builder()
                        .aaHandle(fiRequestState.getAaId())
                        .requestSubmittedOn(fiRequestState.getFiRequestSubmittedOn())
                        .updatedOn(fiRequestState.getUpdatedOn())
                        .sessionStatus(SessionStatus.get(fiRequestState.getSessionStatus()))
                        .sessionId(sessionId)
                        .consentHandle(consentHandle)
                        .fiStatus(fiRequestState.getFiStatusResponse())
                        .build()))
                .orElseThrow(() -> Errors.NoDataFound.with(txnId, "FIRequest not found for given sessionId:" + sessionId))
                ;
    }

    @Override
    public Mono<FIDataI> getData(String consentHandleId, String sessionId, FIDataOutputFormat fiDataOutputFormat) {
        log.debug("GetStoredData: start: consentHandle:{}, sessionId:{}", consentHandleId, sessionId);
        return easyFIDataStore.getFIData(consentHandleId, sessionId)
                .map(fiFetchResponse -> {
                    validateAndGetFIRequest(consentHandleId, sessionId);
                    return toFIData(fiFetchResponse, fiDataOutputFormat)
                            .doOnSuccess(response -> log.debug("GetStoredData: success: consentHandleId:{}, sessionId:{}", consentHandleId, sessionId))
                            .doOnError(error -> log.error("GetStoredData: error: consentHandleId:{}, sessionId:{}, error:{}", consentHandleId, sessionId, error.getMessage(), error))
                            ;
                }).orElseGet(() -> {
                    log.debug("GetStoredData: DataNotFound: consentHandleId:{}, sessionId:{}", consentHandleId, sessionId);
                    return Mono.empty();
                })
                ;
    }

    @Override
    public Mono<FIDataDeleteResponse> deleteData(String consentHandleId) {
        FIDataDeleteResponse response = new FIDataDeleteResponse(null, consentHandleId, null, false);

        fiFetchMetadataStore.deleteByConsentHandleId(consentHandleId);
        final var deletionCounts = easyFIDataStore.deleteFIDataByConsentHandleId(consentHandleId);
        if (deletionCounts.isEmpty()) {
            log.debug("DeleteData: data not present for consentHandleId:{}", consentHandleId);
            return Mono.just(response);
        }

        response.setDeleted(true);
        log.debug("DeleteData: success: consentHandleId:{}, deletionCounts:{}", consentHandleId, deletionCounts);
        return Mono.just(response);
    }

    @Override
    public Mono<Boolean> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp) {
        final Map<String, Integer> deletionCounts = easyFIDataStore.deleteByDataLifeExpireOnBefore(triggerTimestamp);
        if (deletionCounts.isEmpty())
            return Mono.just(Boolean.FALSE);
        log.debug("delete-data-by-dataLife-expire-on-before deletionCounts:{}", deletionCounts);
        return Mono.just(Boolean.TRUE);
    }

    @Override
    public Mono<FIDataDeleteResponse> deleteData(String consentHandleId, String sessionId) {
        FIDataDeleteResponse response = new FIDataDeleteResponse(sessionId, consentHandleId, null, false);

        fiFetchMetadataStore.deleteBySessionId(sessionId);
        final var deletionCounts = easyFIDataStore.deleteFIDataByConsentHandleIdAndSessionId(consentHandleId, sessionId);
        if (deletionCounts.isEmpty()) {
            log.debug("Data not present for consentHandleId:{}, sessionId:{}", consentHandleId, sessionId);
            return Mono.just(response);
        }
        response.setDeleted(true);
        log.debug("Data deleted for consentHandleId:{}, sessionId:{}, deletionCounts:{}", consentHandleId, sessionId, deletionCounts);
        return Mono.just(response);
    }
}