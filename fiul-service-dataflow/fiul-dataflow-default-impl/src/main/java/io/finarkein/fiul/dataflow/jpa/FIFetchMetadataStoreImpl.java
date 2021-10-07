/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa;

import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import io.finarkein.fiul.dataflow.store.FIFetchMetadataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
public class FIFetchMetadataStoreImpl implements FIFetchMetadataStore {

    private final RepoFIFetchMetadata repoFIFetchMetadata;

    @Autowired
    FIFetchMetadataStoreImpl(RepoFIFetchMetadata repoFIFetchMetadata) {
        this.repoFIFetchMetadata = repoFIFetchMetadata;
    }

    @Override
    public FIFetchMetadata saveFIFetchMetadata(FIFetchMetadata metadata) {
        return repoFIFetchMetadata.save(metadata);
    }

    @Override
    public int updateSuccessFetchMetadata(FIFetchMetadata fetchMetadata) {
        return repoFIFetchMetadata.updateStatus(fetchMetadata.getSessionId(),
                fetchMetadata.getFiFetchSubmittedOn(), fetchMetadata.getFiFetchCompletedOn(),
                Timestamp.from(Instant.now()), fetchMetadata.getFipId(), fetchMetadata.getLinkRefNumbers());
    }

    @Override
    public Optional<FIFetchMetadata> getFIFetchMetadata(String sessionId) {
        return repoFIFetchMetadata.findById(sessionId);
    }

    @Override
    public Optional<FIFetchMetadata> getFIFetchMetadata(String sessionId, String aaName) {
        return repoFIFetchMetadata.findOne(Example.of(FIFetchMetadata.builder().sessionId(sessionId).aaName(aaName).build()));
    }

    @Override
    public Optional<FIFetchMetadata> getCompletedFIFetchMetadata(String sessionId, String fipId, String linkRefNumbers, Timestamp fiDataFrom, Timestamp fiDataTo) {
        if (fipId == null || fipId.trim().isEmpty())
            return repoFIFetchMetadata.getCompletedFetchMetaData(sessionId, fiDataFrom, fiDataTo);
        else
            return repoFIFetchMetadata.getCompletedFetchMetaDataForFIP(sessionId, fipId, linkRefNumbers, fiDataFrom, fiDataTo);
    }

    @Override
    public Optional<FIFetchMetadata> getFIFetchMetadata(String sessionId, Timestamp fiDataFrom, Timestamp fiDataTo) {
        return repoFIFetchMetadata.getFetchMetaData(sessionId, fiDataFrom, fiDataTo);
    }
}
