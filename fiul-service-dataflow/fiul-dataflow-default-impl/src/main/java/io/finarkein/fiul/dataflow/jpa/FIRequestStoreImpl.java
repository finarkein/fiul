/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa;

import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.util.Functions;
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

import java.util.Optional;

import static io.finarkein.api.aa.util.Functions.strToTimeStamp;

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

    @Override
    public void saveFIRequestAndFetchMetadata(FIFetchMetadata fiFetchMetadata, FIUFIRequest fiRequest) {
        final var fiRequestDTO = FIRequestDTO.builder()
                .sessionId(fiFetchMetadata.getSessionId())
                .consentId(fiFetchMetadata.getConsentId())
                .timestamp(strToTimeStamp.apply(fiRequest.getTimestamp()))
                .version(fiRequest.getVer())
                .txnId(fiFetchMetadata.getTxnId())
                .aaName(fiFetchMetadata.getAaName())
                .keyMaterial(fiRequest.getKeyMaterial())
                .fiDataRangeFrom(fiFetchMetadata.getFiDataRangeFrom())
                .fiDataRangeTo(fiFetchMetadata.getFiDataRangeTo())
                .callback(fiRequest.getCallback())
                .build();

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            repoFIRequestDTO.save(fiRequestDTO);
            repoFIFetchMetadata.save(fiFetchMetadata);
        });
    }

    @Override
    public Optional<FIRequestDTO> getFIRequest(String consentId, String sessionId) {
        return repoFIRequestDTO.findById(new FIRequestDTO.Key(consentId, sessionId));
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
        final var fiRequestState = FIRequestState.builder()
                .sessionId(notificationLogEntry.getSessionId())
                .notifierId(notificationLogEntry.getNotifierId())
                .txnId(notificationLogEntry.getTxnId())
                .sessionStatus(notificationLogEntry.getSessionStatus())
                .fiStatusResponse(notificationLogEntry.getFiStatusNotification())
                .notificationTimestamp(notificationLogEntry.getNotificationTimestamp())
                .build();

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            repoFINotificationLog.save(notificationLogEntry);
            repoFIRequestState.save(fiRequestState);
        });
    }

    @Override
    public Optional<FIRequestState> getFIRequestState(String sessionId) {
        return repoFIRequestState.findById(sessionId);
    }
}
