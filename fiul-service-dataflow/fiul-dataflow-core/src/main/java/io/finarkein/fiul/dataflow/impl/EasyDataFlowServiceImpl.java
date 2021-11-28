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
import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.consent.model.ConsentState;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.converter.xml.XMLConverterFunctions;
import io.finarkein.fiul.dataflow.DataRequest;
import io.finarkein.fiul.dataflow.EasyDataFlowService;
import io.finarkein.fiul.dataflow.FIUFIRequest;
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
import reactor.util.context.Context;

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
import static io.finarkein.fiul.Functions.doGet;

@Log4j2
@Service
public class EasyDataFlowServiceImpl implements EasyDataFlowService {

    protected final AAFIUClient fiuClient;
    protected final FIRequestStore fiRequestStore;
    protected final EasyFIDataStore easyFIDataStore;
    protected final CallbackRegistry callbackRegistry;
    protected final FIFetchMetadataStore fiFetchMetadataStore;
    protected final ConsentService consentService;

    @Autowired
    protected EasyDataFlowServiceImpl(AAFIUClient fiuClient, FIRequestStore fiRequestStore,
                                      FIFetchMetadataStore fiFetchMetadataStore,
                                      ConsentService consentService,
                                      EasyFIDataStore easyFIDataStore,
                                      CallbackRegistry callbackRegistry) {
        this.fiuClient = fiuClient;
        this.fiRequestStore = fiRequestStore;
        this.fiFetchMetadataStore = fiFetchMetadataStore;
        this.consentService = consentService;
        this.easyFIDataStore = easyFIDataStore;
        this.callbackRegistry = callbackRegistry;
    }

    @Override
    public Mono<FIRequestResponse> createDataRequest(DataRequest dataRequest) {
        log.debug("CreateDataRequest: start: request:{}", dataRequest);
        return Mono.just(validateDataRequest(dataRequest))
                .then(doCreateDataRequest(dataRequest))
                .doOnSuccess(response -> log.debug("CreateDataRequest: submitted: response:{}", response))
                .doOnError(error -> log.error("CreateDataRequest: error: request:{}", error.getMessage(), error));
    }

    protected Mono<FIRequestResponse> doCreateDataRequest(DataRequest dataRequest) {
        final var startTime = Timestamp.from(Instant.now());
        final var aaName = aaNameExtractor.apply(dataRequest.getCustomerAAId());
        Mono<String> consentIdMono = prepareConsentIdMono(dataRequest, dataRequest.getCustomerAAId());
        final var serializedKeyPair = doGet(fiuClient.generateKeyMaterial());

        return consentIdMono
                .map(consentId -> {
                    //set consentId on dataRequest
                    dataRequest.setConsentId(consentId);
                    return FIUFIRequest.builder()
                            .ver(FIRequest.VERSION)
                            .txnid(dataRequest.getTxnid())
                            .timestamp(timestampToStr.apply(startTime))
                            .consent(new Consent(dataRequest.getConsentId(), null))
                            .callback(dataRequest.getCallback())
                            .fIDataRange(new FIDataRange(dataRequest.getDataRageFrom(), dataRequest.getDataRageTo()))
                            .keyMaterial(serializedKeyPair.getKeyMaterial())
                            .build();
                })
                .flatMap(fiufiRequest ->
                        fiuClient.createFIRequest(fiufiRequest, aaName)
                                .doOnSuccess(saveKeyMaterialDataKey(serializedKeyPair, dataRequest))
                                .doOnSuccess(saveFIRequestAndFetchMetadata(aaName, dataRequest, startTime, fiufiRequest))
                                .doOnSuccess(response -> {
                                    final var fiCallback = dataRequest.getCallback();
                                    if (Objects.isNull(fiCallback) || Objects.isNull(fiCallback.getUrl()))
                                        return;
                                    var callback = new FICallback();
                                    callback.setSessionId(response.getSessionId());
                                    callback.setConsentId(response.getConsentId());
                                    callback.setCallbackUrl(fiCallback.getUrl());
                                    callbackRegistry.registerFICallback(callback);
                                })
                )
                ;
    }

    protected Mono<String> prepareConsentIdMono(DataRequest dataRequest, String customerAAId) {
        if (dataRequest.getConsentId() == null) {
            return consentService
                    .getConsentState(dataRequest.getConsentHandleId(), Optional.of(customerAAId))
                    .map(ConsentState::getConsentId);
        }
        return Mono.just(dataRequest.getConsentId());
    }

    public static Context withFiuFiRequest(FIUFIRequest fiuFiRequest) {
        return Context.of("FIUFIRequest", Mono.just(fiuFiRequest));
    }

    protected boolean validateDataRequestPre(DataRequest dataRequest) {
        ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getCustomerAAId(), "customerAAId");
        return true;
    }

    protected boolean validateDataRequest(DataRequest dataRequest) {
        ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getTxnid(), "txnId");
        ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getConsentHandleId(), "consentHandleId");
        ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getCustomerAAId(), "customerAAId");
        ArgsValidator.checkTimestamp(dataRequest.getTxnid(), dataRequest.getDataRageFrom(), "dataRageFrom");
        ArgsValidator.checkTimestamp(dataRequest.getTxnid(), dataRequest.getDataRageTo(), "dataRageTo");
        ArgsValidator.validateDateRange(dataRequest.getTxnid(), dataRequest.getDataRageFrom(), dataRequest.getDataRageTo());

        if (dataRequest.getCallback() != null)
            ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getCallback().getUrl(), "Callback.Url");
        return true;
    }

    protected Consumer<FIRequestResponse> saveFIRequestAndFetchMetadata(String aaName, DataRequest dataRequest, Timestamp startTime, FIUFIRequest fiufiRequest) {
        return response -> {
            final var fiFetchMetadata = FIFetchMetadata.builder()
                    .aaName(aaName)
                    .consentHandleId(dataRequest.getConsentHandleId())
                    .sessionId(response.getSessionId())
                    .fiDataRangeFrom(strToTimeStamp.apply(dataRequest.getDataRageFrom()))
                    .fiDataRangeTo(strToTimeStamp.apply(dataRequest.getDataRageTo()))
                    .txnId(dataRequest.getTxnid())
                    .fiRequestSubmittedOn(startTime)
                    .easyDataFlow(true)
                    .build();
            fiRequestStore.saveFIRequestAndFetchMetadata(fiFetchMetadata, fiufiRequest);
        };
    }

    protected Consumer<FIRequestResponse> saveKeyMaterialDataKey(SerializedKeyPair serializedKeyPair,
                                                                 DataRequest dataRequest) {
        //TODO encrypt privateKey while storing
        return response -> easyFIDataStore.saveKey(KeyMaterialDataKey.builder()
                .consentHandleId(dataRequest.getConsentHandleId())
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

            final var privateKey = easyFIDataStore.getKey(consentHandleId, sessionId);
            if (privateKey.isEmpty())
                throw Errors.InternalError.with(fiFetchResponse.getTxnid(),
                        "Unable to decode FIFetchResponse, data-key not found for consentHandleId:" + consentHandleId + ", sessionId:" + sessionId);

            var decryptedFIData = doGet(
                    fiuClient.decryptFIFetchResponse(fiFetchResponse,
                            new SerializedKeyPair(privateKey.get().getEncryptedKey(), fiRequestDTO.getKeyMaterial()))
            );

            easyFIDataStore.deleteKey(consentHandleId, sessionId);

            final var consentDetailMono = consentService.getSignedConsentDetail(consentHandleId, fiRequestDTO.getAaName());
            consentDetailMono.subscribe(consentDetail -> {
                if (ConsentMode.get(consentDetail.getConsentMode()) == STORE) {
                    easyFIDataStore.saveFIData(DataSaveRequest.with(decryptedFIData)
                            .consentHandleId(consentHandleId)
                            .sessionId(sessionId)
                            .aaName(fiRequestDTO.getAaName())
                            .dataLife(consentDetail.getDataLife())
                            .dataLifeExpireOn(calculateExpireTime.apply(consentDetail.getDataLife()))
                            .build());
                }
            });

            return toFIData(decryptedFIData, fiDataOutputFormat);
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
        if (fiRequestOptional.isEmpty())
            throw Errors.InvalidRequest.with(txnId, "FIRequest metadata not found for given consentHandleId:"
                    + consentHandleId + ", sessionId:" + sessionId + " not found");

        return fiRequestOptional.get();
    }

    protected void requireDataKeyPresent(String consentHandleId, String sessionId) {
        final var dataKey = easyFIDataStore.getKey(consentHandleId, sessionId);
        if (dataKey.isEmpty())
            throw Errors.InvalidRequest.with(UUIDSupplier.get(),
                    "Invalid sessionId, KeyPair not found for consentHandleId:" + consentHandleId
                            + ", sessionId:" + sessionId);
    }

    @Override
    public Mono<DataRequestStatus> dataRequestStatus(String consentHandleId, String sessionId) {
        log.debug("Getting data request status, consentHandleId:{}, sessionId:{}", consentHandleId, sessionId);
        final var optionalState = fiRequestStore.getFIRequestState(sessionId);
        if (optionalState.isPresent()) {
            var fiRequestState = optionalState.get();
            SessionStatus status = SessionStatus.get(fiRequestState.getSessionStatus());

            return Mono.just(DataRequestStatus.builder()
                    .sessionStatus(status)
                    .sessionId(sessionId)
                    .consentHandleId(consentHandleId)
                    .build());
        }

        return Mono.error(Errors.NoDataFound.with(UUIDSupplier.get(), "FIRequest not found"));
    }

    @Override
    public Mono<FIDataI> getData(String consentId, String sessionId, FIDataOutputFormat fiDataOutputFormat) {
        log.debug("GetStoredData: start: consentId:{}, sessionId:{}", consentId, sessionId);
        return easyFIDataStore.getFIData(consentId, sessionId)
                .map(fiFetchResponse -> {
                    final FIRequestDTO fiRequestDTO = validateAndGetFIRequest(consentId, sessionId);
                    return toFIData(fiFetchResponse, fiDataOutputFormat)
                            .doOnSuccess(response -> log.debug("GetStoredData: success: consentId:{}, sessionId:{}", consentId, sessionId))
                            .doOnError(error -> log.error("GetStoredData: error: consentId:{}, sessionId:{}, error:{}", consentId, sessionId, error.getMessage(), error))
                            ;
                }).orElseGet(() -> {
                    log.debug("GetStoredData: DataNotFound: consentId:{}, sessionId:{}", consentId, sessionId);
                    return Mono.empty();
                })
                ;
    }

    @Override
    public Mono<FIDataDeleteResponse> deleteData(String consentId) {
        FIDataDeleteResponse response = new FIDataDeleteResponse(null, consentId, false);
        final var deletionCounts = easyFIDataStore.deleteFIDataByConsentId(consentId);
        if (deletionCounts.isEmpty()) {
            log.debug("DeleteData: data not present for consentId:{}", consentId);
            return Mono.just(response);
        }
        response.setDeleted(true);
        log.debug("DeleteData: success: consentId:{}, deletionCounts:{}", consentId, deletionCounts);
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
    public Mono<FIDataDeleteResponse> deleteData(String consentId, String sessionId) {
        FIDataDeleteResponse response = new FIDataDeleteResponse(sessionId, consentId, false);
        final var deletionCounts = easyFIDataStore.deleteFIDataByConsentIdAndSessionId(consentId, sessionId);
        if (deletionCounts.isEmpty()) {
            log.debug("Data not present for consentId:{}, sessionId:{}", consentId, sessionId);
            return Mono.just(response);
        }
        response.setDeleted(true);
        log.debug("Data deleted for consentId:{}, sessionId:{}, deletionCounts:{}", consentId, sessionId, deletionCounts);
        return Mono.just(response);
    }
}