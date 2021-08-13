/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.model.FIData;
import io.finarkein.fiul.dataflow.DataRequest;
import io.finarkein.fiul.dataflow.EasyDataFlowService;
import io.finarkein.fiul.dataflow.easy.DataRequestStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Log4j2
public class EasyDataFlowController {

    private final EasyDataFlowService easyDataFlowService;

    @Autowired
    EasyDataFlowController(EasyDataFlowService easyDataFlowService) {
        this.easyDataFlowService = easyDataFlowService;
    }

    @PostMapping("/FI/data/request")
    public Mono<FIRequestResponse> postDataRequest(@RequestBody DataRequest dataRequest) {
        return easyDataFlowService.createDataRequest(dataRequest);
    }

    @GetMapping("/FI/data/request/status/{consentId}/{sessionId}")
    public Mono<DataRequestStatus> getDataRequestStatus(@PathVariable String consentId, @PathVariable String sessionId) {
        return easyDataFlowService.dataRequestStatus(consentId, sessionId);
    }

    @GetMapping("/FI/data/fetch/{consentId}/{sessionId}")
    public Mono<FIData> dataFetch(@PathVariable String consentId, @PathVariable final String sessionId) {
        return easyDataFlowService.fetchData(consentId, sessionId);
    }

    @GetMapping("/FI/data/{consentId}/{sessionId}")
    public Mono<FIData> getData(@PathVariable String consentId, @PathVariable final String sessionId) {
        return easyDataFlowService.getData(consentId, sessionId);
    }

    @DeleteMapping({"/FI/data/{consentId}", "/FI/data/{consentId}/{sessionId}"})
    public Mono<Boolean> deleteData(@PathVariable Map<String, String> map) {
        final String sessionId = map.get("sessionId");
        if (sessionId != null)
            return easyDataFlowService.deleteData(map.get("consentId"), sessionId);
        return easyDataFlowService.deleteData(map.get("consentId"));
    }
}
