/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.aa.registry.RegistryService;
import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.notification.NotificationResponse;
import io.finarkein.fiul.config.model.AaApiKeyBody;
import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.dto.FIRequestState;
import io.finarkein.fiul.notification.NotificationPublisher;
import io.finarkein.fiul.validator.NotificationValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Service
public class DefaultFINotificationProcessor implements FINotificationProcessor {
    public static final String DEFAULT_PROCESSOR_NAME = "4225e94c-89bd-11ec-a8a3-0242ac120002";
    private static final Set<String> applicableEntities = Set.of(DEFAULT_PROCESSOR_NAME);

    protected final NotificationPublisher publisher;
    protected final DataFlowService dataFlowService;
    protected final RegistryService registryService;
    protected final Base64.Decoder decoder = Base64.getDecoder();
    protected final ObjectMapper objectMapper;

    @Autowired
    protected DefaultFINotificationProcessor(NotificationPublisher publisher,
                                   final DataFlowService dataFlowService,
                                   RegistryService registryService,
                                   ObjectMapper objectMapper) {
        this.publisher = publisher;
        this.dataFlowService = dataFlowService;
        this.registryService = registryService;
        this.objectMapper = objectMapper;
    }

    protected Optional<FIRequestState> retrieveFIRequestState(FINotification fiNotification) {
        return dataFlowService.getFIRequestStateByTxnId(fiNotification.getTxnid());
    }

    @Override
    public ResponseEntity<Mono<NotificationResponse>> process(FINotification fiNotification, String aaApiKey) {
        log.debug("FINotification received:{}", fiNotification);
        final String txnId = fiNotification.getTxnid();
        AaApiKeyBody aaApiKeyBody = null;
        try {
            String[] chunks = aaApiKey.split("\\.");
            String payload = new String(decoder.decode(chunks[1]));
            aaApiKeyBody = objectMapper.readValue(payload, AaApiKeyBody.class);
        } catch (Exception e) {
            log.error("Error while publishing fiNotification for handling:{}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(txnId,
                    Timestamp.from(Instant.now()), e.getMessage())));
        }
        ArgsValidator.isValidUUID(txnId, txnId, "TxnId");

        final String sessionId = fiNotification.getFIStatusNotification().getSessionId();
        Optional<FIRequestState> optionalFIRequestState = retrieveFIRequestState(fiNotification);
        if (optionalFIRequestState.isPresent()) {
            try {
                log.debug("{}: Validating FINotification", sessionId);
                NotificationValidator.validateFINotification(fiNotification, optionalFIRequestState.get(),
                        registryService.getEntityInfoByAAName(optionalFIRequestState.get().getAaId()),
                        aaApiKeyBody);
                log.debug("{}: FINotification.publishing (fiNotification)", sessionId);
                publisher.publishFINotification(fiNotification);
                log.debug("{}: FINotification.publish(fiNotification) done", sessionId);
                return ResponseEntity.ok(Mono.just(NotificationResponse.okResponse(txnId, Timestamp.from(Instant.now()))));
            } catch (SystemException e) {
                log.error("Error while publishing fiNotification for handling:{}", e.getMessage(), e);
                if (e.errorCode().httpStatusCode() == 404)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(NotificationResponse.notFoundResponse(txnId, Timestamp.from(Instant.now()), e.getMessage())));
                return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(txnId, Timestamp.from(Instant.now()), e.getMessage())));
            } catch (Exception e) {
                log.error("Error while publishing fiNotification for handling:{}", e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        }
        log.debug("{}: For FINotification FIRequestState not found returning invalid request", sessionId);
        return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(txnId,
                Timestamp.from(Instant.now()), "Invalid Request")));
    }

    @Override
    public Set<String> applicableEntities() {
        return applicableEntities;
    }
}
