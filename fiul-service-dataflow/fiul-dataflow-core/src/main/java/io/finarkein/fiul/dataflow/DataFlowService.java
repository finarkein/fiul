/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.fiul.dataflow.dto.FIDataDeleteResponse;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

public interface DataFlowService {

    Mono<FIRequestResponse> createFIRequest(FIUFIRequest fiRequest, String aaName);

    Mono<FIFetchResponse> fiFetch(String dataSessionId, String aaName, String fipId, String[] linkRefNumber);

    Mono<FIFetchResponse> fiGet(String dataSessionId, String fipId, String[] linkRefNumber);

    Mono<FIDataDeleteResponse> deleteDataForSession(String dataSessionId);

    Mono<FIDataDeleteResponse> deleteDataByConsentId(String consentId);

    Mono<Boolean> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp);
}
