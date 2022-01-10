/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.store;

import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import io.finarkein.fiul.dataflow.dto.FIRequestDTO;
import io.finarkein.fiul.dataflow.dto.FIRequestState;

import java.sql.Timestamp;
import java.util.Optional;

public interface FIRequestStore {

    void saveFIRequestAndFetchMetadata(FIFetchMetadata fiFetchMetadata, FIUFIRequest fiRequest);

    void updateFIRequestStateOnError(FIUFIRequest fiRequest, String aaName, Timestamp fiRequestStartTime, String dataSessionId);

    Optional<FIRequestDTO> getFIRequest(String consentHandleId, String sessionId);

    Optional<FIRequestDTO> getFIRequestByAANameAndSessionId(String sessionId, String aaName);

    Optional<FIRequestDTO> getFIRequestBySessionId(String sessionId);

    void logNotificationAndUpdateState(FINotification fiNotification);

    Optional<FIRequestState> getFIRequestState(String consentHandleId, String sessionId);

    Optional<FIRequestState> getFIRequestStateByTxnId(String txnId);
}
