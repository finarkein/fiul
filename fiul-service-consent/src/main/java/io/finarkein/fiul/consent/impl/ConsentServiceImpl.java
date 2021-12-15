/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.api.aa.consent.artefact.SignedConsent;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.api.aa.consent.handle.ConsentStatus;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.api.aa.util.Functions;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.model.ConsentRequestDTO;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.consent.service.ConsentStore;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import io.finarkein.fiul.notification.callback.model.ConsentCallback;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static io.finarkein.api.aa.util.Functions.*;
import static io.finarkein.fiul.Functions.UUIDSupplier;
import static io.finarkein.fiul.Functions.currentTimestampSupplier;

@Log4j2
@Service
class ConsentServiceImpl implements ConsentService {

    private final AAFIUClient aafiuClient;
    private final ConsentStore consentStore;
    private final CallbackRegistry callbackRegistry;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    ConsentServiceImpl(AAFIUClient aafiuClient, ConsentStore consentStore, CallbackRegistry callbackRegistry) {
        this.aafiuClient = aafiuClient;
        this.consentStore = consentStore;
        this.callbackRegistry = callbackRegistry;
    }

    @Override
    public Mono<ConsentResponse> createConsent(FIUConsentRequest consentRequest) {
        return Mono.just(consentRequest)
                .doOnNext(request -> log.debug("CreateConsent: start: request:{}", request))
                .flatMap(this::doCreateConsent)
                .doOnSuccess(response -> log.debug("CreateConsent: success: response:{}", response))
                .doOnError(error -> log.error("CreateConsent: error:{}", error.getMessage(), error));
    }

    protected Mono<ConsentResponse> doCreateConsent(FIUConsentRequest consentRequest) {
        return aafiuClient.createConsent(consentRequest)
                .doOnSuccess(response -> {
                            if (Objects.isNull(consentRequest.getCallback()) || Objects.isNull(consentRequest.getCallback().getUrl()))
                                return;

                            var callback = new ConsentCallback();
                            callback.setConsentHandleId(response.getConsentHandle());
                            callback.setCallbackUrl(consentRequest.getCallback().getUrl());
                            callbackRegistry.registerConsentCallback(callback);
                        }
                ).doOnSuccess(response -> {
                    consentStore.saveConsentRequest(response.getConsentHandle(), consentRequest);
                    consentStore.saveConsentState(ConsentStateDTO.builder()
                            .txnId(response.getTxnid())
                            .isPostConsentSuccessful(true)
                            .aaId(aaNameExtractor.apply(consentRequest.getConsentDetail().getCustomer().getId()))
                            .customerAAId(consentRequest.getConsentDetail().getCustomer().getId())
                            .consentHandle(response.getConsentHandle())
                            .postConsentResponseTimestamp(strToTimeStamp.apply(response.getTimestamp()))
                            .build());
                }).doOnError(SystemException.class, e -> {
                            if (e.getParamValue("consentHandle") != null)
                                consentStore.saveConsentState(ConsentStateDTO.builder()
                                        .consentHandle(e.getParamValue("consentHandle"))
                                        .txnId(consentRequest.getTxnid())
                                        .isPostConsentSuccessful(false)
                                        .aaId(aaNameExtractor.apply(consentRequest.getConsentDetail().getCustomer().getId()))
                                        .build());
                        }
                );
    }

    @Override
    public Mono<ConsentHandleResponse> getConsentStatus(String consentHandle, Optional<String> aaNameOptional) {
        log.debug("GetConsentStatus: start: consentHandle:{}, aaHandle:{}", consentHandle, aaNameOptional);
        return buildFromConsentState(consentHandle)
                .get()
                .map(Mono::just)
                .orElseGet(() ->
                        aaNameOptional
                                .or(consentRequestAANameByConsentHandle(consentHandle))
                                .map(aaName ->
                                        aafiuClient
                                                .getConsentStatus(consentHandle, aaName)
                                                .flatMap(consentHandleResponse -> {
                                                    final Optional<ConsentStateDTO> optionalConsentState = consentStore
                                                            .getConsentStateByHandle(consentHandle);
                                                    if (optionalConsentState.isPresent() && strToTimeStamp.apply(consentHandleResponse.getTimestamp())
                                                            .before(optionalConsentState.get().getPostConsentResponseTimestamp())) {
                                                        throw Errors.InvalidRequest.with(optionalConsentState.get().getTxnId(),
                                                                "Invalid consent handle response timestamp : " + consentHandleResponse.getTimestamp());
                                                    }
                                                    return Mono.just(consentHandleResponse);
                                                })
                                )
                                .orElseThrow(() -> Errors
                                        .NoDataFound
                                        .with(UUIDSupplier.get(), "ConsentHandle not found, try with aaHandle"))
                ).doOnSuccess(consentHandleResponse -> {
                    log.debug("GetConsentStatus: success: response:{}", consentHandleResponse);
                    consentStateUpdateHelper(consentHandleResponse.getTxnid(), consentHandleResponse.getConsentStatus().getId(),
                            consentHandleResponse.getConsentStatus().getStatus());
                })
                .doOnError(error -> log.error("GetConsentStatus: error:{}", error.getMessage(), error));
    }

    private void consentStateUpdateHelper(String txnId, String consentId, String consentStatus) {
        ConsentStateDTO consentStateDTO = consentStore.getConsentStateByTxnId(txnId);
        if (consentStateDTO != null) {
            consentStateDTO.setConsentId(consentId);
            consentStateDTO.setConsentStatus(consentStatus);
            consentStore.updateConsentState(consentStateDTO);
        }
    }

    private Supplier<Optional<String>> consentRequestAANameByConsentHandle(final String consentHandle) {
        return () -> consentStore.findRequestByConsentHandle(consentHandle).map(ConsentRequestDTO::getAaName);
    }

    private Supplier<Optional<String>> consentRequestAANameByConsentId(final String consentId) {
        return () -> consentStore.findRequestByConsentId(consentId).map(ConsentRequestDTO::getAaName);
    }

    private Supplier<Optional<ConsentHandleResponse>> buildFromConsentState(String consentHandle) {
        return () -> {
            final Optional<ConsentStateDTO> consentStateOptional = consentStore.getConsentStateByHandle(consentHandle);
            return consentStateOptional
                    .map(consentState -> {
                                if (consentState.getConsentStatus() == null || consentState.getConsentId() == null)
                                    return null;

                                return ConsentHandleResponse.builder()
                                        .txnid(UUIDSupplier.get())
                                        .consentHandle(consentHandle)
                                        .ver(ConsentHandleResponse.VERSION)
                                        .timestamp(currentTimestampSupplier.get())
                                        .consentStatus(new ConsentStatus(consentState.getConsentId(), consentState.getConsentStatus()))
                                        .build();
                            }
                    );
        };
    }

    @Override
    public Mono<ConsentArtefact> getConsentArtefact(String consentId, Optional<String> aaNameOptional) {
        log.debug("GetConsentArtefact: start: consentId:{}, aaHandle:{}", consentId, aaNameOptional);
        return aaNameOptional
                .or(consentRequestAANameByConsentId(consentId))
                .map(aaName -> aafiuClient.getConsentArtefact(consentId, aaName)
                        .flatMap(consentArtefact -> {
                            final Timestamp artefactCreateTimestamp = strToTimeStamp.apply(consentArtefact.getCreateTimestamp());
                            final ConsentStateDTO consentStateDTO = consentStore.getConsentStateById(consentArtefact.getConsentId());
                            if (consentStateDTO != null && consentStateDTO.getPostConsentResponseTimestamp() != null
                                    && artefactCreateTimestamp.before(consentStateDTO.getPostConsentResponseTimestamp())) {
                                throw Errors.InvalidRequest.with(consentStateDTO.getTxnId(),
                                        "Invalid consent artefact timestamp : " + consentArtefact.getCreateTimestamp());
                            }
                            if (artefactCreateTimestamp.after(strToTimeStamp.apply(currentTimestampSupplierMinusDuration.apply(-5, ChronoUnit.SECONDS)))) {
                                throw Errors.InvalidRequest.with(consentStateDTO.getTxnId(),
                                        "Invalid consent artefact timestamp : " + consentArtefact.getCreateTimestamp());
                            }
                            return Mono.just(consentArtefact);
                        })
                )
                .orElseThrow(() -> Errors
                        .NoDataFound
                        .with(UUIDSupplier.get(), "ConsentArtefact not found, try with aaHandle"))
                .doOnSuccess(artefact -> log.debug("GetConsentArtefact: success: consentId:{}, aaHandle:{}", consentId, aaNameOptional))
                .doOnError(error -> log.error("GetConsentArtefact: error:{}", error.getMessage(), error));
    }

    @Override
    public Optional<ConsentRequestDTO> getConsentRequestByConsentId(String consentId) {
        return consentStore.findRequestByConsentId(consentId);
    }

    @Override
    public Mono<SignedConsent> getSignedConsentDetail(String consentId, String aaName) {
        log.debug("GetConsentDetail: start: consentId:{}, aaName:{}", consentId, aaName);
        return aafiuClient.getConsentArtefact(consentId, aaName)
                .map(ConsentArtefact::getSignedConsent)
                .flatMap(signedConsent -> {
                    final var tokens = Functions.decodeJWTToken.apply(signedConsent);
                    if (tokens[1] != null) {
                        try {
                            return Mono.just(mapper.readValue(tokens[1], SignedConsent.class));
                        } catch (Exception e) {
                            return Mono.error(e);
                        }
                    }
                    return Mono.empty();
                }).doOnSuccess(details -> log.debug("GetConsentDetail: success: consentId:{}, aaName:{}", consentId, aaName));
    }

    @Override
    public void handleConsentNotification(ConsentNotificationLog consentNotificationLog) {
        consentStore.logConsentNotification(consentNotificationLog);
    }

    @Override
    public Mono<ConsentStateDTO> getConsentState(String consentHandle, Optional<String> aaHandle) {
        log.debug("GetConsentState: start: consentHandle:{}, aaHandle:{}", consentHandle, aaHandle);
        return consentStore.getConsentStateByHandle(consentHandle)
                .map(Mono::just)
                .orElseGet(() -> fetchConsentStatus(consentHandle, aaHandle))
                .doOnSuccess(artefact -> log.debug("GetConsentState: success: consentHandle:{}, aaHandle:{}", consentHandle, aaHandle))
                .doOnError(error -> log.error("GetConsentState: error:{}", error.getMessage(), error));
    }

    @Override
    public ConsentStateDTO getConsentStateByConsentId(String consentId) {
        return consentStore.getConsentStateById(consentId);
    }

    @Override
    public ConsentStateDTO getConsentStateByTxnId(String txnId) {
        return consentStore.getConsentStateByTxnId(txnId);
    }

    @Override
    public ConsentStateDTO getConsentStateByConsentHandle(String consentHandle) {
        return consentStore.getConsentStateByHandle(consentHandle).orElse(null);
    }

    @Override
    public void updateConsentStateNotifier(String txnId, String notifierId) {
        ConsentStateDTO consentStateDTO = consentStore.getConsentStateByTxnId(txnId);
        if (consentStateDTO != null) {
            consentStateDTO.setNotifierId(notifierId);
            consentStore.updateConsentState(consentStateDTO);
        }
    }

    @Override
    public void updateConsentState(ConsentStateDTO consentStateDTO) {
        consentStore.updateConsentState(consentStateDTO);
    }

    private Mono<ConsentStateDTO> fetchConsentStatus(String consentHandle, Optional<String> aaHandle) {
        return aaHandle
                .or(() -> consentStore.findRequestByConsentHandle(consentHandle).map(ConsentRequestDTO::getAaName))
                .map(aaName ->
                        aafiuClient
                                .getConsentStatus(consentHandle, aaName)
                                .flatMap(consentStatusResponse -> {
                                    final var state = new ConsentStateDTO();
                                    state.setConsentHandle(consentStatusResponse.getConsentHandle());
                                    state.setConsentId(consentStatusResponse.getConsentStatus().getId());
                                    state.setConsentStatus(consentStatusResponse.getConsentStatus().getStatus());
                                    state.setTxnId(consentStatusResponse.getTxnid());
                                    state.setAaId(aaName);

                                    //saving consentState
                                    consentStore.saveConsentState(state);
                                    return Mono.just(state);
                                })
                                .onErrorMap(throwable -> Errors.InvalidRequest.with(UUIDSupplier.get(),
                                        throwable.getMessage(), throwable))
                )
                .orElseThrow(() -> Errors.InvalidRequest.with(UUIDSupplier.get(),
                        "Unable to get status for given consentHandle:" + consentHandle + ", try with aaHandle"));
    }
}
