/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.store;

import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;

public interface FIFetchMetadataStore {

    int deleteByConsentId(String consentId);

    int deleteByConsentHandleId(String consentHandleId);

    int deleteBySessionId(String sessionId);

    FIFetchMetadata saveFIFetchMetadata(FIFetchMetadata metadata);

    int updateSuccessFetchMetadata(FIFetchMetadata fetchMetadata);

    Optional<FIFetchMetadata> getFIFetchMetadata(String sessionId);

    Set<String> completedSessionIds(Set<String> sessionIds);

    Mono<Optional<FIFetchMetadata>> getLatestFIFetchMetadata(String consentHandleId, Timestamp fromValue, Timestamp toValue, boolean easyDataFlow);

    Optional<FIFetchMetadata> getFIFetchMetadata(String sessionId, String aaName);

    Optional<FIFetchMetadata> getCompletedFIFetchMetadata(String sessionId, String fipId, String linkRefNumbers,
                                                          Timestamp fiDataFrom, Timestamp fiDataTo);

    Optional<FIFetchMetadata> getFIFetchMetadata(String sessionId, Timestamp fiDataFrom, Timestamp fiDataTo);
}
