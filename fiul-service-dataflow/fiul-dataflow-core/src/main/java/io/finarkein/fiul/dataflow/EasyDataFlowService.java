/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.model.FIData;
import io.finarkein.fiul.dataflow.dto.FIDataDeleteResponse;
import io.finarkein.fiul.dataflow.easy.DataRequestStatus;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

public interface EasyDataFlowService {

    Mono<FIRequestResponse> createDataRequest(DataRequest dataRequest);

    Mono<DataRequestStatus> dataRequestStatus(String consentId, String sessionId);

    Mono<FIData> fetchData(String consentId, String sessionId);

    Mono<FIData> getData(String consentId, String sessionId);

    Mono<FIDataDeleteResponse> deleteData(String consentId, String dataSessionId);

    Mono<FIDataDeleteResponse> deleteData(String consentId);

    Mono<Boolean> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp);
}
