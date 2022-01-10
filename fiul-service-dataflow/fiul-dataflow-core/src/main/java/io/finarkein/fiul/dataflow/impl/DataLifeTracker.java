/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.impl;

import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.EasyDataFlowService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;

@Log4j2
@Component
@RefreshScope
public class DataLifeTracker {

    @Autowired
    private EasyDataFlowService easyDataFlowService;

    @Autowired
    private DataFlowService dataFlowService;

    @Value("${fiul.dataflow.data-life-tracker-fixed-delay}")
    private int fixedDelay;

    @Scheduled(initialDelay = 5000, fixedDelayString = "${fiul.dataflow.data-life-tracker-fixed-delay}")
    public void deleteExpiredFIData() {
        final var triggerTimestamp = Timestamp.from(Instant.now());

        try {
            easyDataFlowService.deleteByDataLifeExpireOnBefore(triggerTimestamp);
        } catch (Exception e) {
            log.error("Error while executing easyDataFlowService.deleteByDataLifeExpireOnBefore, error:{}", e.getMessage());
        }

        try {
            dataFlowService.deleteByDataLifeExpireOnBefore(triggerTimestamp);
        } catch (Exception e) {
            log.error("Error while executing dataFlowService.deleteByDataLifeExpireOnBefore, error:{}", e.getMessage());
        }

        log.debug("DataLifeTracker triggered with expiration-on-or-before:{}, next-fire-time: after {} minutes",
                triggerTimestamp,
                fixedDelay / 1000 / 60);
    }

}
