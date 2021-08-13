/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.store;

import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.fiul.dataflow.easy.DataSaveRequest;

import java.sql.Timestamp;
import java.util.Map;

public interface AAFIDataStore {

    Map<String, Integer> deleteFIDataByConsentId(String consentId);

    Map<String, Integer> deleteFIDataBySessionId(String sessionId);

    FIFetchResponse getFIData(String consentId, String sessionId, String aaName, String fipId, String[] linkRefNumber);

    void saveFIData(DataSaveRequest<FIFetchResponse> request);

    Map<String, Integer> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp);
}
