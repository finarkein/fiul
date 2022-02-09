/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.aa.registry.RegistryService;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.dto.FIRequestState;
import io.finarkein.fiul.notification.NotificationPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "fiul.fi-notification-processor.session-based")
public class SessionIdBasedFINotificationProcessor extends DefaultFINotificationProcessor implements FINotificationProcessor {
    private final Set<String> applicableEntities;

    @Autowired
    SessionIdBasedFINotificationProcessor(NotificationPublisher publisher,
                                          final DataFlowService dataFlowService,
                                          RegistryService registryService, ObjectMapper objectMapper,
                                          @Value("${fiul.fi-notification-processor.session-based}") String applicableEntityIds) {
        super(publisher, dataFlowService, registryService, objectMapper);
        applicableEntities = Set.of(applicableEntityIds.split(","));
    }

    protected Optional<FIRequestState> retrieveFIRequestState(FINotification fiNotification) {
        return dataFlowService.getFIRequestStateBySessionId(fiNotification.getFIStatusNotification().getSessionId());
    }

    @Override
    public Set<String> applicableEntities() {
        return applicableEntities;
    }
}
