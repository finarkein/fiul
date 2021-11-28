/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.fiul.dataflow.DataRequest;
import io.finarkein.fiul.dataflow.EasyDataFlowService;
import io.finarkein.fiul.dataflow.response.decrypt.FIDataOutputFormat;
import io.finarkein.fiul.dataflow.dto.FIDataDeleteResponse;
import io.finarkein.fiul.dataflow.easy.DataRequestStatus;
import io.finarkein.fiul.dataflow.response.decrypt.FIDataI;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/")
@Log4j2
public class EasyDataFlowController {

    protected final EasyDataFlowService easyDataFlowService;

    @Autowired
    protected EasyDataFlowController(EasyDataFlowService easyDataFlowService) {
        this.easyDataFlowService = easyDataFlowService;
    }

    @PostMapping("/FI/data/request")
    public Mono<FIRequestResponse> postDataRequest(@RequestBody DataRequest dataRequest) {
        return easyDataFlowService.createDataRequest(dataRequest);
    }

    @GetMapping("/FI/data/request/status/{consentHandleId}/{sessionId}")
    public Mono<DataRequestStatus> getDataRequestStatus(@PathVariable String consentHandleId, @PathVariable String sessionId) {
        return easyDataFlowService.dataRequestStatus(consentHandleId, sessionId);
    }

    @GetMapping({
            "/FI/data/fetch/{consentHandleId}/{sessionId}/{outputFormat}",
            "/FI/data/fetch/{consentHandleId}/{sessionId}"
    })
    public Mono<FIDataI> dataFetch(@PathVariable String consentHandleId, @PathVariable final String sessionId,
                                   @PathVariable final Optional<String> outputFormat) {
        return easyDataFlowService.fetchData(consentHandleId, sessionId, decideOutputFormat(outputFormat));
    }

    @GetMapping({
            "/FI/data/{consentHandleId}/{sessionId}/{outputFormat}",
            "/FI/data/{consentHandleId}/{sessionId}"
    })
    public Mono<FIDataI> getData(@PathVariable String consentHandleId, @PathVariable final String sessionId,
                                 @PathVariable final Optional<String> outputFormat) {
        return easyDataFlowService.getData(consentHandleId, sessionId, decideOutputFormat(outputFormat));
    }

    private FIDataOutputFormat decideOutputFormat(Optional<String> outputFormat) {
        return outputFormat
                .map(FIDataOutputFormat::validateAndGetValue)
                .orElse(FIDataOutputFormat.json);
    }

    @DeleteMapping({
            "/FI/data/{consentHandleId}",
            "/FI/data/{consentHandleId}/{sessionId}"
    })
    public Mono<FIDataDeleteResponse> deleteData(@PathVariable Map<String, String> map) {
        final String sessionId = map.get("sessionId");
        if (sessionId != null)
            return easyDataFlowService.deleteData(map.get("consentHandleId"), sessionId);
        return easyDataFlowService.deleteData(map.get("consentHandleId"));
    }
}
