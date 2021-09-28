/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.heartbeat.HeartbeatResponse;
import io.finarkein.fiul.AAFIUClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@Log4j2
public class FiulController {

    private final AAFIUClient fiuClient;

    @Autowired
    FiulController(AAFIUClient fiuClient) {
        this.fiuClient = fiuClient;
    }

    @GetMapping("/{aaName}/Heartbeat")
    public Mono<HeartbeatResponse> getHeartbeat(@PathVariable String aaName) {
        return fiuClient.heartBeat(aaName);
    }
}
