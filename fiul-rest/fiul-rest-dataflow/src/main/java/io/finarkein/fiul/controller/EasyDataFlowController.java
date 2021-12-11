/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.fiul.dataflow.DataRequest;
import io.finarkein.fiul.dataflow.DataRequestResponse;
import io.finarkein.fiul.dataflow.EasyDataFlowService;
import io.finarkein.fiul.dataflow.dto.FIDataDeleteResponse;
import io.finarkein.fiul.dataflow.easy.DataRequestStatus;
import io.finarkein.fiul.dataflow.response.decrypt.FIDataI;
import io.finarkein.fiul.dataflow.response.decrypt.FIDataOutputFormat;
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
    public Mono<DataRequestResponse> postDataRequest(@RequestBody DataRequest dataRequest) {
        return easyDataFlowService.createDataRequest(dataRequest);
    }

    @GetMapping("/FI/data/request/status/{consentHandle}/{sessionId}")
    public Mono<DataRequestStatus> getDataRequestStatus(@PathVariable String consentHandle, @PathVariable String sessionId) {
        return easyDataFlowService.dataRequestStatus(consentHandle, sessionId);
    }

    @GetMapping("/FI/data/fetch/{consentHandle}/{sessionId}")
    public Mono<FIDataI> dataFetch(@PathVariable String consentHandle,
                                   @PathVariable final String sessionId,
                                   @RequestParam(value = "outputFormat", required = false) String outputFormat) {
        return easyDataFlowService.fetchData(consentHandle, sessionId, decideOutputFormat(outputFormat));
    }

    @GetMapping({
            "/FI/data/{consentHandle}/{sessionId}/{outputFormat}",
            "/FI/data/{consentHandle}/{sessionId}"
    })
    public Mono<FIDataI> getData(@PathVariable String consentHandle,
                                 @PathVariable final String sessionId,
                                 @RequestParam(value = "outputFormat", required = false) String outputFormat) {
        return easyDataFlowService.getData(consentHandle, sessionId, decideOutputFormat(outputFormat));
    }

    private FIDataOutputFormat decideOutputFormat(String outputFormat) {
        return Optional.ofNullable(outputFormat)
                .map(FIDataOutputFormat::validateAndGetValue)
                .orElse(FIDataOutputFormat.json);
    }

    @DeleteMapping({
            "/FI/data/{consentHandle}",
            "/FI/data/{consentHandle}/{sessionId}"
    })
    public Mono<FIDataDeleteResponse> deleteData(@PathVariable Map<String, String> map) {
        final String sessionId = map.get("sessionId");
        if (sessionId != null)
            return easyDataFlowService.deleteData(map.get("consentHandle"), sessionId);
        return easyDataFlowService.deleteData(map.get("consentHandle"));
    }
}
