/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.aa.registry.RegistryService;
import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.notification.NotificationResponse;
import io.finarkein.fiul.config.model.AaApiKeyBody;
import io.finarkein.fiul.consent.model.ConsentState;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.dto.FIRequestState;
import io.finarkein.fiul.notification.NotificationPublisher;
import io.finarkein.fiul.validator.NotificationValidator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/")
@Log4j2
public class NotificationController {

    private final NotificationPublisher publisher;

    private final ConsentService consentService;

    private final DataFlowService dataFlowService;

    private final RegistryService registryService;

    private final Base64.Decoder decoder = Base64.getDecoder();

    private final ObjectMapper objectMapper;

    @Value("${test.scenario:false}")
    private boolean testScenario;


    @Autowired
    public NotificationController(NotificationPublisher publisher, ConsentService consentService,
                                  RegistryService registryService, DataFlowService dataFlowService) {
        this.publisher = publisher;
        this.consentService = consentService;
        this.registryService = registryService;
        this.dataFlowService = dataFlowService;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/Consent/Notification")
    public ResponseEntity<Mono<NotificationResponse>> consentResponseMono(@RequestBody ConsentNotification consentNotification,
                                                                          @RequestHeader("x-jws-signature") String jwsSignature,
                                                                          @RequestHeader("aa_api_key") String aaApiKey) {
        log.debug("ConsentNotification received:{}", consentNotification);
        validateJWS(consentNotification.getTxnid(), jwsSignature);

        String[] chunks = aaApiKey.split("\\.");

        String payload = new String(decoder.decode(chunks[1]));
        AaApiKeyBody aaApiKeyBody = null;
        try {
            aaApiKeyBody = objectMapper.readValue(payload, AaApiKeyBody.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
        }

        ArgsValidator.isValidUUID(consentNotification.getTxnid(), consentNotification.getTxnid(), "TxnId");

        ConsentState consentState = consentService.getConsentStateByConsentHandle(consentNotification.getConsentStatusNotification().getConsentHandle());
        if (consentState == null)
            consentState = consentService.getConsentStateByTxnId(consentNotification.getTxnid());

        if (consentState != null) {
            try {
                NotificationValidator.validateConsentNotification(consentNotification, consentState,
                        registryService.getEntityInfoByAAName(consentState.getAaId()), testScenario, aaApiKeyBody);

                publisher.publishConsentNotification(consentNotification);
                log.debug("NotificationPublisher.publish(consentNotification) done");
                return ResponseEntity.ok().body(Mono.just(NotificationResponse.okResponse(consentNotification.getTxnid(),
                        Timestamp.from(Instant.now()))));
            } catch (SystemException e) {
                if (e.errorCode().httpStatusCode() == 404)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(NotificationResponse.notFoundResponse(consentNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
                return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(consentNotification.getTxnid(),
                        Timestamp.from(Instant.now()), e.getMessage())));
            } catch (Exception e) {
                log.error("Error while publishing ConsentNotification for handling:{}", e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        }
        return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(consentNotification.getTxnid(),
                Timestamp.from(Instant.now()), "Invalid Request")));
    }

    @PostMapping("/FI/Notification")
    public ResponseEntity<Mono<NotificationResponse>> fiNotification(@RequestBody FINotification fiNotification,
                                                                     @RequestHeader("x-jws-signature") String jwsSignature,
                                                                     @RequestHeader("aa_api_key") String aaApiKey) {
        log.debug("FINotification received:{}", fiNotification);
        validateJWS(fiNotification.getTxnid(), jwsSignature);
        String[] chunks = aaApiKey.split("\\.");
        String payload = new String(decoder.decode(chunks[1]));
        AaApiKeyBody aaApiKeyBody = null;
        try {
            aaApiKeyBody = objectMapper.readValue(payload, AaApiKeyBody.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(fiNotification.getTxnid(),
                    Timestamp.from(Instant.now()), e.getMessage())));
        }
        ArgsValidator.isValidUUID(fiNotification.getTxnid(), fiNotification.getTxnid(), "TxnId");

        Optional<FIRequestState> optionalFIRequestState = dataFlowService.getFIRequestStateByTxnId(fiNotification.getTxnid());
        if (optionalFIRequestState.isPresent()) {
            try {
                NotificationValidator.validateFINotification(fiNotification, optionalFIRequestState.get(),
                        registryService.getEntityInfoByAAName(optionalFIRequestState.get().getAaId()),
                        testScenario, aaApiKeyBody);
            } catch (SystemException e) {
                if (e.errorCode().httpStatusCode() == 404)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(NotificationResponse.notFoundResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
                return ResponseEntity.badRequest().body(Mono.just(NotificationResponse.invalidResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now()), e.getMessage())));
            }
        }
        try {
            publisher.publishFINotification(fiNotification);
            log.debug("FINotification.publish(fiNotification) done");
        } catch (Exception e) {
            log.error("Error while publishing fiNotification for handling:{}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        return ResponseEntity.ok(Mono.just(NotificationResponse.okResponse(fiNotification.getTxnid(), Timestamp.from(Instant.now()))));
    }

    private static void validateJWS(String txnId, String jwsSignature) {
        if (jwsSignature == null)
            throw Errors.InvalidRequest.with(txnId, "Null JWS Signature");
        if (jwsSignature.split("\\.").length != 3)
            throw Errors.InvalidRequest.with(txnId, "Invalid JWS Signature");
    }
}
