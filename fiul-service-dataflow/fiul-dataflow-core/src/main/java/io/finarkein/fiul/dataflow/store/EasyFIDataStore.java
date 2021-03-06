/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.store;

import io.finarkein.fiul.dataflow.easy.DataSaveRequest;
import io.finarkein.fiul.dataflow.easy.dto.KeyMaterialDataKey;
import io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

public interface EasyFIDataStore {

    void saveKey(KeyMaterialDataKey entry);

    Optional<KeyMaterialDataKey> getKeyConsentId(String consentId, String sessionId);

    Mono<Optional<KeyMaterialDataKey>> getKeyConsentHandleId(String consentHandleId, String sessionId);

    void deleteKey(String consentId, String sessionId);

    void saveFIData(DataSaveRequest<FIFetchResponse> request);

    Optional<FIFetchResponse> getFIData(String consentHandleId, String sessionId);

    Map<String, Integer> deleteFIDataByConsentHandleId(String consentHandleId);

    Map<String, Integer> deleteFIDataByConsentHandleIdAndSessionId(String consentHandleId, String sessionId);

    Map<String, Integer> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp);
}
