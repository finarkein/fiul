/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.dto.FIDataDeleteResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/")
@Log4j2
public class DataFlowController {

    private final DataFlowService dataFlowService;

    @Autowired
    DataFlowController(DataFlowService dataFlowService) {
        this.dataFlowService = dataFlowService;
    }

    @PostMapping("/FI/request")
    public Mono<FIRequestResponse> postFIRequest(@RequestBody FIUFIRequest fiRequest) {
        return dataFlowService.createFIRequest(fiRequest, fiRequest.getAaName());
    }

    @GetMapping("/FI/fetch/{dataSessionId}")
    public Mono<FIFetchResponse> fetch(@PathVariable String dataSessionId,
                                       @RequestParam(value = "aaName", required = false) String aaName,
                                       @RequestHeader(value = "fipId", required = false) final String fipId,
                                       @RequestHeader(value = "linkRefNumber", required = false) final String[] linkRefNumber) {
        return dataFlowService.fiFetch(dataSessionId, aaName, fipId, linkRefNumber);
    }

    @GetMapping("/FI/{dataSessionId}")
    public Mono<FIFetchResponse> getData(@PathVariable String dataSessionId,
                                         @RequestHeader(value = "fipId", required = false) final String fipId,
                                         @RequestHeader(value = "linkRefNumber", required = false) final String[] linkRefNumber) {
        return dataFlowService.fiGet(dataSessionId, fipId, linkRefNumber);
    }

    @DeleteMapping({"/FI/{dataSessionId}", "/FI/consent/data/{consentId}"})
    public Mono<FIDataDeleteResponse> deleteData(@PathVariable Map<String, String> pathVariables) {
        final String sessionId = pathVariables.get("dataSessionId");
        if (sessionId != null && !sessionId.isEmpty())
            return dataFlowService.deleteDataForSession(sessionId);
        return dataFlowService.deleteDataByConsentId(pathVariables.get("consentId"));
    }
}
