/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.config;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Configuration
@Getter
@Log4j2
public class DBCallHandlerSchedulerConfig {
    @ToString.Exclude
    private final Scheduler scheduler;

    static final String PROPERTY_PREFIX = "fiul.db-io-handler";

    public DBCallHandlerSchedulerConfig() {
        final int threadCap = Optional
                .ofNullable(System.getProperty(PROPERTY_PREFIX + ".threadCap"))
                .map(Integer::parseInt)
                .orElseGet(() -> 10 * Runtime.getRuntime().availableProcessors());
        final int queueSize = Optional
                .ofNullable(System.getProperty(PROPERTY_PREFIX + ".queue-size"))
                .map(Integer::parseInt)
                .orElse(100000);
        scheduler = Schedulers.newBoundedElastic(threadCap, queueSize, "db-io-handler");
        log.info("name:db-io-handler, scheduler-type:boundedElastic, threadCap:{}, queueSize:{}"
                , threadCap, queueSize);
    }
}
