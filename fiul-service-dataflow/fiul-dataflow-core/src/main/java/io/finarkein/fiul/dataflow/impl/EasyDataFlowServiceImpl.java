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
import io.finarkein.api.aa.model.AccountData;
import io.finarkein.api.aa.model.FIData;
import io.finarkein.api.aa.model.FIPData;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.converter.xml.XMLConverterFunctions;
import io.finarkein.fiul.dataflow.DataRequest;
import io.finarkein.fiul.dataflow.EasyDataFlowService;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import io.finarkein.fiul.dataflow.dto.FIRequestDTO;
import io.finarkein.fiul.dataflow.easy.DataRequestStatus;
import io.finarkein.fiul.dataflow.easy.DataSaveRequest;
import io.finarkein.fiul.dataflow.easy.SessionStatus;
import io.finarkein.fiul.dataflow.easy.dto.KeyMaterialDataKey;
import io.finarkein.fiul.dataflow.response.decrypt.ObjectifiedFIFetchResponse;
import io.finarkein.fiul.dataflow.store.EasyFIDataStore;
import io.finarkein.fiul.dataflow.store.FIFetchMetadataStore;
import io.finarkein.fiul.dataflow.store.FIRequestStore;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import io.finarkein.fiul.notification.callback.model.FICallback;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.finarkein.api.aa.consent.ConsentMode.STORE;
import static io.finarkein.api.aa.util.Functions.*;
import static io.finarkein.fiul.Functions.UUIDSupplier;
import static io.finarkein.fiul.Functions.doGet;
import static io.finarkein.fiul.dataflow.easy.SessionStatus.PENDING;
import static io.finarkein.fiul.dataflow.easy.SessionStatus.READY;

@Log4j2
@Service
public class EasyDataFlowServiceImpl implements EasyDataFlowService {

    private final AAFIUClient fiuClient;
    private final FIRequestStore fiRequestStore;
    private final EasyFIDataStore easyFIDataStore;
    private final CallbackRegistry callbackRegistry;
    private final FIFetchMetadataStore fiFetchMetadataStore;
    private final ConsentService consentService;

    @Autowired
    EasyDataFlowServiceImpl(AAFIUClient fiuClient, FIRequestStore fiRequestStore,
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
        final var serializedKeyPair = doGet(fiuClient.generateKeyMaterial());
        final var fiuFIRequest = FIUFIRequest.builder()
                .ver(FIRequest.VERSION)
                .txnid(dataRequest.getTxnid())
                .timestamp(timestampToStr.apply(startTime))
                .consent(new Consent(dataRequest.getConsentId(), null))
                .callback(dataRequest.getCallback())
                .fIDataRange(new FIDataRange(dataRequest.getDataRageFrom(), dataRequest.getDataRageTo()))
                .keyMaterial(serializedKeyPair.getKeyMaterial())
                .build();

        final var aaName = aaNameExtractor.apply(dataRequest.getCustomerAAId());
        return Mono.just(fiuFIRequest)
                .flatMap(request -> fiuClient.createFIRequest(request, aaName))
                .doOnSuccess(saveKeyMaterialDataKey(serializedKeyPair))
                .doOnSuccess(saveFIRequestAndFetchMetadata(aaName, dataRequest, startTime, fiuFIRequest))
                .doOnSuccess(response -> {
                    final var fiCallback = dataRequest.getCallback();
                    if (Objects.isNull(fiCallback) || Objects.isNull(fiCallback.getUrl()))
                        return;
                    var callback = new FICallback();
                    callback.setSessionId(response.getSessionId());
                    callback.setConsentId(response.getConsentId());
                    callback.setCallbackUrl(fiCallback.getUrl());
                    callbackRegistry.registerFICallback(callback);
                });
    }

    private boolean validateDataRequest(DataRequest dataRequest) {
        ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getTxnid(), "txnId");
        ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getCustomerAAId(), "customerAAId");
        ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getConsentHandleId(), "consentHandleId");
        ArgsValidator.checkTimestamp(dataRequest.getTxnid(), dataRequest.getDataRageFrom(), "dataRageFrom");
        ArgsValidator.checkTimestamp(dataRequest.getTxnid(), dataRequest.getDataRageTo(), "dataRageTo");
        ArgsValidator.validateDateRange(dataRequest.getTxnid(), dataRequest.getDataRageFrom(), dataRequest.getDataRageTo());

        if (dataRequest.getCallback() != null)
            ArgsValidator.checkNotEmpty(dataRequest.getTxnid(), dataRequest.getCallback().getUrl(), "Callback.Url");
        return true;
    }

    Consumer<FIRequestResponse> saveFIRequestAndFetchMetadata(String aaName, DataRequest dataRequest, Timestamp startTime, FIUFIRequest fiufiRequest) {
        return response -> {
            final var fiFetchMetadata = FIFetchMetadata.builder()
                    .aaName(aaName)
                    .consentId(response.getConsentId())
                    .sessionId(response.getSessionId())
                    .fiDataRangeFrom(strToTimeStamp.apply(dataRequest.getDataRageFrom()))
                    .fiDataRangeTo(strToTimeStamp.apply(dataRequest.getDataRageTo()))
                    .txnId(dataRequest.getTxnid())
                    .fiRequestSubmittedOn(startTime)
                    .build();
            fiRequestStore.saveFIRequestAndFetchMetadata(fiFetchMetadata, fiufiRequest);
        };
    }

    Consumer<FIRequestResponse> saveKeyMaterialDataKey(SerializedKeyPair serializedKeyPair) {
        //TODO encrypt privateKey while storing
        return response -> easyFIDataStore.saveKey(KeyMaterialDataKey.builder()
                .consentId(response.getConsentId())
                .sessionId(response.getSessionId())
                .encryptedKey(serializedKeyPair.getPrivateKey()).build());
    }

    @Override
    public Mono<FIData> fetchData(String consentId, String sessionId) {
        log.debug("FetchData: start: consentId:{}, sessionId:{}", consentId, sessionId);
        final var fetchDataStartTime = Timestamp.from(Instant.now());
        final var fiRequestDTO = validateAndGetFIRequest(consentId, sessionId);
        requireDataKeyPresent(consentId, sessionId);

        return fiuClient
                .fiFetch(sessionId, fiRequestDTO.getAaName())
                .flatMap(saveDataIfStoreConsentMode(fiRequestDTO, consentId, sessionId))
                .doOnSuccess(updateFetchMetadata(sessionId, fetchDataStartTime))
                .doOnSuccess(response -> log.debug("FetchData: success: consentId:{}, sessionId:{}", consentId, sessionId))
                .doOnError(error -> log.error("FetchData: error: consentId:{}, sessionId:{}, error:{}", consentId, sessionId, error.getMessage(), error))
                ;
    }

    private Function<FIFetchResponse, Mono<FIData>> saveDataIfStoreConsentMode(FIRequestDTO fiRequestDTO, String consentId, String sessionId) {
        return fiFetchResponse -> {

            final var privateKey = easyFIDataStore.getKey(consentId, sessionId);
            if (privateKey.isEmpty())
                throw Errors.InternalError.with(fiFetchResponse.getTxnid(),
                        "Unable to decode FIFetchResponse, data-key not found for consentId:" + consentId + ", sessionId:" + sessionId);

            var decryptedFIData = doGet(
                    fiuClient.decryptFIFetchResponse(fiFetchResponse,
                            new SerializedKeyPair(privateKey.get().getEncryptedKey(), fiRequestDTO.getKeyMaterial()))
            );

            easyFIDataStore.deleteKey(consentId, sessionId);

            final var consentDetail = consentService.consentDetail(consentId, fiRequestDTO.getAaName());

            if (ConsentMode.get(consentDetail.getConsentMode()) == STORE) {
                easyFIDataStore.saveFIData(DataSaveRequest.with(decryptedFIData)
                        .consentId(consentId)
                        .sessionId(sessionId)
                        .aaName(fiRequestDTO.getAaName())
                        .dataLife(consentDetail.getDataLife())
                        .dataLifeExpireOn(calculateExpireTime.apply(consentDetail.getDataLife()))
                        .build());
            }
            return toFIData(consentId, sessionId, decryptedFIData, consentDetail);
        };
    }

    private Mono<FIData> toFIData(String consentId, String sessionId,
                                  io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse decryptedFIData,
                                  io.finarkein.api.aa.consent.request.ConsentDetail consentDetail) {
        final var builder = FIData.builder()
                .customerAAId(consentDetail.getCustomer().getId())
                .consentId(consentId)
                .sessionId(sessionId);

        ObjectifiedFIFetchResponse response = XMLConverterFunctions.fiFetchResponseToObject.apply(decryptedFIData);
        final var fipDataList = response.getObjectifiedFI()
                .stream()
                .map(objectifiedFI -> FIPData.builder()
                        .fipId(objectifiedFI.getFipID())
                        .accountsData(objectifiedFI.getObjectifiedDatumList()
                                .stream()
                                .map(obj -> AccountData.builder()
                                        .linkRefNumber(obj.getLinkRefNumber())
                                        .maskedAccNumber(obj.getMaskedAccNumber())
                                        .summary(obj.getFi().getSummary())
                                        .profile(obj.getFi().getProfile())
                                        .transactions(obj.getFi().getTransactions())
                                        .type(obj.getFi().getType())
                                        .version("" + obj.getFi().getVersion())
                                        .build()
                                ).collect(Collectors.toList())
                        ).build()
                ).collect(Collectors.toList());
        return Mono.just(builder.fipData(fipDataList).build());
    }

    private Consumer<FIData> updateFetchMetadata(String sessionId, Timestamp fetchDataStartTime) {
        return fiFetchResponse -> {
            var metadataBuilder = FIFetchMetadata.builder()
                    .sessionId(sessionId)
                    .fiFetchSubmittedOn(fetchDataStartTime);

            metadataBuilder.fiFetchCompletedOn(Timestamp.from(Instant.now()));
            int rowsUpdated = fiFetchMetadataStore.updateSuccessFetchMetadata(metadataBuilder.build());
            log.debug("FI-Fetch-Metadata #OfRows updated:{}", rowsUpdated);
        };
    }

    private FIRequestDTO validateAndGetFIRequest(String consentId, String sessionId) {
        final String txnId = UUIDSupplier.get();
        ArgsValidator.checkNotEmpty(txnId, consentId, "ConsentId");
        ArgsValidator.checkNotEmpty(txnId, sessionId, "SessionId");

        final var fiRequestOptional = fiRequestStore.getFIRequest(consentId, sessionId);
        if (fiRequestOptional.isEmpty())
            throw Errors.InvalidRequest.with(txnId, "FIRequest metadata not found for given consentId:" + consentId + ", sessionId:" + sessionId + " not found");

        return fiRequestOptional.get();
    }

    private void requireDataKeyPresent(String consentId, String sessionId) {
        final var dataKey = easyFIDataStore.getKey(consentId, sessionId);
        if (dataKey.isEmpty())
            throw Errors.InvalidRequest.with(UUIDSupplier.get(), "Invalid sessionId, KeyPair not found for consentId:" + consentId + ", sessionId:" + sessionId);
    }

    @Override
    public Mono<DataRequestStatus> dataRequestStatus(String consentId, String sessionId) {
        log.debug("Getting data request status, consentId:{}, sessionId:{}", consentId, sessionId);
        final var optionalState = fiRequestStore.getFIRequestState(sessionId);
        if (optionalState.isPresent()) {
            var fiRequestState = optionalState.get();
            SessionStatus status = fiRequestState.getSessionStatus().equalsIgnoreCase(READY.name()) ?
                    READY : PENDING;

            return Mono.just(DataRequestStatus.builder()
                    .sessionStatus(status)
                    .sessionId(sessionId)
                    .consentId(consentId)
                    .txnId(fiRequestState.getTxnId())
                    .build());
        }

        /* TODO this is temporary implementation remove this once we start getting fi-notifications on local machine */
        try {
            //It FIP needs some time to prepare FI after FIFetch request is raised successfully,
            //hence adding sleep
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        return Mono.just(DataRequestStatus.builder().consentId(consentId).sessionId(sessionId).sessionStatus(READY).build());
    }

    @Override
    public Mono<FIData> getData(String consentId, String sessionId) {
        log.debug("GetStoredData: start: consentId:{}, sessionId:{}", consentId, sessionId);
        return easyFIDataStore.getFIData(consentId, sessionId)
                .map(fiFetchResponse -> {
                    final FIRequestDTO fiRequestDTO = validateAndGetFIRequest(consentId, sessionId);
                    final var consentDetail = consentService.consentDetail(consentId, fiRequestDTO.getAaName());
                    return toFIData(consentId, sessionId, fiFetchResponse, consentDetail)
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
    public Mono<Boolean> deleteData(String consentId) {
        final var deletionCounts = easyFIDataStore.deleteFIDataByConsentId(consentId);
        if (deletionCounts.isEmpty()) {
            log.debug("DeleteData: data not present for consentId:{}", consentId);
            return Mono.just(Boolean.FALSE);
        }
        log.debug("DeleteData: success: consentId:{}, deletionCounts:{}", consentId, deletionCounts);
        return Mono.just(Boolean.TRUE);
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
    public Mono<Boolean> deleteData(String consentId, String sessionId) {
        final var deletionCounts = easyFIDataStore.deleteFIDataByConsentIdAndSessionId(consentId, sessionId);
        if (deletionCounts.isEmpty()) {
            log.debug("Data not present for consentId:{}, sessionId:{}", consentId, sessionId);
            return Mono.just(Boolean.FALSE);
        }
        log.debug("Data deleted for consentId:{}, sessionId:{}, deletionCounts:{}", consentId, sessionId, deletionCounts);
        return Mono.just(Boolean.TRUE);
    }
}