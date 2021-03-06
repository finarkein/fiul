/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa;

import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.util.Functions;
import io.finarkein.fiul.config.DBCallHandlerSchedulerConfig;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import io.finarkein.fiul.dataflow.dto.FINotificationLogEntry;
import io.finarkein.fiul.dataflow.dto.FIRequestDTO;
import io.finarkein.fiul.dataflow.dto.FIRequestState;
import io.finarkein.fiul.dataflow.store.FIRequestStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.Optional;

import static io.finarkein.api.aa.util.Functions.strToTimeStamp;
import static io.finarkein.api.aa.util.Functions.uuidSupplier;

@Service
public class FIRequestStoreImpl implements FIRequestStore {

    @Autowired
    private RepoFIRequestDTO repoFIRequestDTO;

    @Autowired
    private RepoFIFetchMetadata repoFIFetchMetadata;

    @Autowired
    private RepoFINotificationLog repoFINotificationLog;

    @Autowired
    private RepoFIRequestState repoFIRequestState;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DBCallHandlerSchedulerConfig schedulerConfig;

    @Override
    public void saveFIRequestAndFetchMetadata(FIFetchMetadata fiFetchMetadata, FIUFIRequest fiRequest) {
        final Timestamp timestamp = strToTimeStamp.apply(fiRequest.getTimestamp());
        final var fiRequestDTO = FIRequestDTO.builder()
                .sessionId(fiFetchMetadata.getSessionId())
                .consentId(fiFetchMetadata.getConsentId())
                .consentHandleId(fiFetchMetadata.getConsentHandleId())
                .timestamp(timestamp)
                .version(fiRequest.getVer())
                .txnId(fiFetchMetadata.getTxnId())
                .aaName(fiFetchMetadata.getAaName())
                .keyMaterial(fiRequest.getKeyMaterial())
                .fiDataRangeFrom(fiFetchMetadata.getFiDataRangeFrom())
                .fiDataRangeTo(fiFetchMetadata.getFiDataRangeTo())
                .callback(fiRequest.getCallback())
                .build();

        final var fiRequestState = FIRequestState.builder()
                .sessionId(fiFetchMetadata.getSessionId())
                .consentHandleId(fiFetchMetadata.getConsentHandleId())
                .notifierId(null)
                .txnId(fiFetchMetadata.getTxnId())
                .sessionStatus("ACTIVE")
                .fiStatusResponse(null)
                .fiRequestSuccessful(true)
                .fiRequestSubmittedOn(fiFetchMetadata.getFiRequestSubmittedOn())
                .notificationTimestamp(timestamp)
                .aaId(fiFetchMetadata.getAaName())
                .build();

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            repoFIRequestDTO.save(fiRequestDTO);
            repoFIFetchMetadata.save(fiFetchMetadata);
            repoFIRequestState.save(fiRequestState);
        });
    }

    @Override
    public void updateFIRequestStateOnError(FIUFIRequest fiRequest, String aaName, Timestamp fiRequestStartTime, String dataSessionId) {
        if (dataSessionId == null)
            dataSessionId = uuidSupplier.get(); //TODO why this is needed?
        final var fiRequestState = FIRequestState.builder()
                .sessionId(dataSessionId)
                .notifierId(null)
                .txnId(fiRequest.getTxnid())
                .sessionStatus(null)
                .fiStatusResponse(null)
                .fiRequestSuccessful(false)
                .aaId(aaName)
                .fiRequestSubmittedOn(fiRequestStartTime)
                .notificationTimestamp(strToTimeStamp.apply(fiRequest.getTimestamp()))
                .build();
        repoFIRequestState.save(fiRequestState);
    }

    @Override
    public Mono<Optional<FIRequestDTO>> getFIRequest(String consentHandleId, String sessionId) {
        return Mono.fromCallable(() -> repoFIRequestDTO.findBySessionIdAndConsentHandleId(sessionId, consentHandleId))
                .subscribeOn(schedulerConfig.getScheduler());
    }

    @Override
    public Optional<FIRequestDTO> getFIRequestByAANameAndSessionId(String sessionId, String aaName) {
        final var requestDTOExample = Example.of(FIRequestDTO.builder().aaName(aaName).sessionId(sessionId).build());
        return repoFIRequestDTO.findOne(requestDTOExample);
    }

    @Override
    public Optional<FIRequestDTO> getFIRequestBySessionId(String sessionId) {
        final var requestDTOExample = Example.of(FIRequestDTO.builder().sessionId(sessionId).build());
        return repoFIRequestDTO.findOne(requestDTOExample);
    }

    @Override
    public void logNotificationAndUpdateState(FINotification fiNotification) {
        final var notificationLogEntry = FINotificationLogEntry.builder()
                .version(fiNotification.getVer())
                .txnId(fiNotification.getTxnid())
                .fiStatusNotification(fiNotification.getFIStatusNotification().getFiStatusResponse())
                .notifierId(fiNotification.getNotifier().getId())
                .notifierType(fiNotification.getNotifier().getType())
                .sessionId(fiNotification.getFIStatusNotification().getSessionId())
                .sessionStatus(fiNotification.getFIStatusNotification().getSessionStatus())
                .notificationTimestamp(Functions.strToTimeStamp.apply(fiNotification.getTimestamp()))
                .build();

        final var fiRequestState = repoFIRequestState
                .findById(fiNotification.getFIStatusNotification().getSessionId())
                .orElse(new FIRequestState());

        fiRequestState.setSessionId(notificationLogEntry.getSessionId());
        fiRequestState.setNotifierId(notificationLogEntry.getNotifierId());
        fiRequestState.setTxnId(notificationLogEntry.getTxnId());
        fiRequestState.setSessionStatus(notificationLogEntry.getSessionStatus());
        fiRequestState.updateFiStatusResponse(fiNotification.getFIStatusNotification().getFiStatusResponse());
        fiRequestState.setNotificationTimestamp(notificationLogEntry.getNotificationTimestamp());

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            repoFINotificationLog.save(notificationLogEntry);
            repoFIRequestState.delete(fiRequestState);
            repoFIRequestState.save(fiRequestState);
        });
    }

    @Override
    public Optional<FIRequestState> getFIRequestState(String consentHandleId, String sessionId) {
        return repoFIRequestState.findBySessionIdAndConsentHandleId(sessionId, consentHandleId);
    }

    @Override
    public Optional<FIRequestState> getFIRequestStateByTxnId(String txnId) {
        return repoFIRequestState.findByTxnId(txnId);
    }

    @Override
    public Optional<FIRequestState> getFIRequestStateBySessionId(String sessionId) {
        return repoFIRequestState.findBySessionId(sessionId);
    }
}
