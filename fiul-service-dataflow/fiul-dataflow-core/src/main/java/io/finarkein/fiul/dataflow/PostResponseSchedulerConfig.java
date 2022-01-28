/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Configuration
@Getter
@Log4j2
public class PostResponseSchedulerConfig {

    @ToString.Include
    private final String schedulerType;

    @ToString.Exclude
    private final Scheduler scheduler;

    static final String BOUNDED_ELASTIC = "boundedElastic";

    public PostResponseSchedulerConfig(@Value("${fiul.response-processor.scheduler-type:immediate}") String schedulerType,
                                       @Value("${fiul.response-processor.scheduler-name:response-processor}") String responseProcessor) {
        this.schedulerType = schedulerType;
        if (BOUNDED_ELASTIC.equalsIgnoreCase(schedulerType)) {
            final int threadCap = Optional
                    .ofNullable(System.getProperty("fiul.post-response.scheduler.size"))
                    .map(Integer::parseInt)
                    .orElseGet(() -> 10 * Runtime.getRuntime().availableProcessors());
            final int queueSize = Optional
                    .ofNullable(System.getProperty("fiul.post-response.scheduler.queue-size"))
                    .map(Integer::parseInt)
                    .orElse(100000);
            scheduler = Schedulers.newBoundedElastic(threadCap, queueSize, responseProcessor);
            log.info("PostResponseSchedulerConfig = name:{}, schedulerType:{}, threadCap:{}, queueSize:{}",
                    responseProcessor, schedulerType, threadCap, queueSize);
        } else {
            scheduler = Schedulers.immediate();
        }
    }
}
