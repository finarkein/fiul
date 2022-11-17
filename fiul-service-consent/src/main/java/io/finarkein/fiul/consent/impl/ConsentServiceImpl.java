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
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.consent.ConsentShortMeta;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.model.ConsentRequestDTO;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.consent.model.SignedConsentDTO;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.consent.service.ConsentStore;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import io.finarkein.fiul.notification.callback.model.ConsentCallback;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static io.finarkein.api.aa.util.Functions.*;
import static io.finarkein.fiul.Functions.UUIDSupplier;

@Log4j2
@Service
class ConsentServiceImpl implements ConsentService {

    protected final AAFIUClient aafiuClient;
    protected final ConsentStore consentStore;
    protected final CallbackRegistry callbackRegistry;
    protected final ObjectMapper mapper;

    @Autowired
    ConsentServiceImpl(AAFIUClient aafiuClient,
                       ConsentStore consentStore,
                       CallbackRegistry callbackRegistry,
                       ObjectMapper mapper) {
        this.aafiuClient = aafiuClient;
        this.consentStore = consentStore;
        this.callbackRegistry = callbackRegistry;
        this.mapper = mapper;
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
                            callback.setRunId(consentRequest.getCallback().getRunId());
                            callback.setAaId(consentRequest.getConsentDetail().getCustomer().getId());
                            callback.setAddOnParams(consentRequest.getCallback().getAddOnParams());
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
        Mono<ConsentHandleResponse> consentHandleResponseMono;
        if (aaNameOptional.isPresent()) {
            String aaName = aaNameOptional.get();
            consentHandleResponseMono = getConsentHandleResponseMono(consentHandle, aaName);
        } else {
            consentHandleResponseMono = consentRequestAANameByConsentHandle(consentHandle)
                    .flatMap(optionalAAName -> optionalAAName
                            .map(aaName -> getConsentHandleResponseMono(consentHandle, aaName))
                            .orElseThrow(() -> Errors.NoDataFound.with(UUIDSupplier.get(), "ConsentHandle not found, try with aaHandle"))
                    );
        }
        return consentHandleResponseMono.doOnSuccess(consentHandleResponse -> {
                    log.debug("GetConsentStatus: success: response:{}", consentHandleResponse);
                    consentStateUpdateHelper(consentHandleResponse.getTxnid(), consentHandleResponse.getConsentStatus().getId(),
                            consentHandleResponse.getConsentStatus().getStatus(), consentHandleResponse.getConsentHandle());
                })
                .doOnError(error -> log.error("GetConsentStatus: error:{}", error.getMessage(), error));
    }

    private Mono<ConsentHandleResponse> getConsentHandleResponseMono(String consentHandle, String aaName) {
        return aafiuClient.getConsentStatus(consentHandle, aaName)
                .flatMap(consentHandleResponse -> consentStore.consentStateByHandle(consentHandle)
                        .flatMap(optionalConsentState -> {
                            if (optionalConsentState.isPresent()
                                    && Objects.nonNull(optionalConsentState.get().getPostConsentResponseTimestamp())
                                    && strToTimeStamp.apply(consentHandleResponse.getTimestamp())
                                    .before(optionalConsentState.get().getPostConsentResponseTimestamp())) {
                                throw Errors.InvalidRequest.with(optionalConsentState.get().getTxnId(),
                                        "Invalid consent handle response timestamp : " + consentHandleResponse.getTimestamp());
                            }
                            return Mono.just(consentHandleResponse);
                        })
                );
    }

    private void consentStateUpdateHelper(String txnId, String consentId, String consentHandleStatus, String consentHandle) {
        ConsentStateDTO consentStateDTO = consentStore.getConsentStateByTxnId(txnId);
        if (consentStateDTO == null)
            consentStateDTO = consentStore.getConsentStateByHandle(consentHandle).orElse(null);
        if (consentStateDTO != null) {
            consentStateDTO.setConsentId(consentId);
            consentStateDTO.setConsentHandleStatus(consentHandleStatus);
            consentStore.updateConsentState(consentStateDTO);
        }
    }

    private Mono<Optional<String>> consentRequestAANameByConsentHandle(final String consentHandle) {
        return consentStore.findRequestByConsentHandle(consentHandle)
                .map(optionalConsentRequestDTO -> optionalConsentRequestDTO.map(ConsentRequestDTO::getAaName))
                ;
    }

    private Supplier<Optional<String>> consentRequestAANameByConsentId(final String consentId) {
        return () -> consentStore.findRequestByConsentId(consentId).map(ConsentRequestDTO::getAaName);
    }

    @Override
    public Mono<ConsentArtefact> getConsentArtefact(String consentId, Optional<String> aaNameOptional) {
        log.debug("GetConsentArtefact: start: consentId:{}, aaHandle:{}", consentId, aaNameOptional);
        return aaNameOptional
                .or(consentRequestAANameByConsentId(consentId))
                .map(aaName -> aafiuClient.getConsentArtefact(consentId, aaName)
                        .publishOn(Schedulers.boundedElastic())
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
                            if (consentStateDTO != null) {
                                log.debug("Updating setGetConsentArtefactSuccessful to null for " +
                                                "consent handle : {}, consentId : {}",
                                        consentStateDTO.getConsentHandle(), consentId);
                                consentStateDTO.setGetConsentArtefactSuccessful(null);
                                consentStore.saveConsentState(consentStateDTO);
                            }
                            return Mono.just(consentArtefact);
                        })
                        .doOnError(throwable -> {
                            final ConsentStateDTO consentStateDTO = consentStore.getConsentStateById(consentId);
                            if (consentStateDTO != null) {
                                log.error("Updating setGetConsentArtefactSuccessful to false for " +
                                                "consent handle : {}, consentId : {}",
                                        consentStateDTO.getConsentHandle(), consentId);
                                consentStateDTO.setGetConsentArtefactSuccessful(false);
                                consentStore.saveConsentState(consentStateDTO);
                            }

                        })
                )
                .orElseThrow(() -> Errors
                        .NoDataFound
                        .with(UUIDSupplier.get(), "ConsentArtefact not found, try with aaHandle"))
                .doOnSuccess(artefact -> log.debug("GetConsentArtefact: success: consentId:{}, aaHandle:{}", consentId, aaNameOptional))
                .doOnError(error -> log.error("GetConsentArtefact: error:{}", error.getMessage(), error));
    }

    @Override
    public Mono<SignedConsentDTO> getSignedConsent(final String consentId, final Optional<String> aaHandleOptional) {
        final var consentDTOOptional = consentStore.findSignedConsent(consentId);
        return consentDTOOptional
                .map(Mono::just)
                .orElseGet(() -> fetchAndSaveSignedConsent(consentId, aaHandleOptional))
                ;
    }

    protected Mono<SignedConsentDTO> fetchAndSaveSignedConsent(final String consentId, @NonNull Optional<String> aaHandleOptional) {
        final String aaHandle = aaHandleOptional
                .or(() -> {
                    var consentState = getConsentStateByConsentId(consentId);
                    return (consentState != null) ?
                            Optional.ofNullable(consentState.getAaId()) : Optional.empty();
                })
                .orElseThrow(() -> Errors.NoDataFound.with(UUIDSupplier.get(), "SignedConsent cannot be found, try with aaHandle")
                        .params(Map.of("consentId", consentId)));
        return aafiuClient.getConsentArtefact(consentId, aaHandle)
                .flatMap(consentArtefact -> {
                    SignedConsentDTO signedConsentDTO = new SignedConsentDTO();
                    signedConsentDTO.setConsentId(consentId);
                    signedConsentDTO.setCreateTimestamp(strToTimeStamp.apply(consentArtefact.getCreateTimestamp()));

                    final String[] tokens = decodeJWTToken.apply(consentArtefact.getSignedConsent());
                    signedConsentDTO.setHeader(tokens[0]);
                    signedConsentDTO.setPayload(tokens[1]);
                    signedConsentDTO.setSignature(tokens[2]);

                    if (signedConsentDTO.getPayload() != null) {
                        try {
                            final SignedConsent signedConsent = mapper.readValue(signedConsentDTO.getPayload(), SignedConsent.class);
                            signedConsentDTO.setConsentMode(signedConsent.getConsentMode());
                            signedConsentDTO.setDataLifeUnit(signedConsent.getDataLife().getUnit());
                            signedConsentDTO.setDataLifeValue(signedConsent.getDataLife().getValue());
                        } catch (Exception e) {
                            return Mono.error(e);
                        }
                    }

                    return Mono.just(signedConsentDTO);
                })
                .doOnSuccess(consentStore::saveSignedConsent);
    }

    @Override
    public Optional<ConsentRequestDTO> getConsentRequestByConsentId(String consentId) {
        return consentStore.findRequestByConsentId(consentId);
    }

    @Override
    public Mono<SignedConsent> getSignedConsentDetail(String consentId, String aaName) {
        log.debug("GetSignedConsentDetail: start: consentId:{}, aaName:{}", consentId, aaName);

        return getSignedConsent(consentId, Optional.ofNullable(aaName))
                .map(SignedConsentDTO::getPayload)
                .flatMap(payload -> {
                    SignedConsent signedConsent = deserializeSingedConsent(payload);
                    if (signedConsent == null)
                        return Mono.empty();
                    return Mono.just(signedConsent);
                }).doOnSuccess(details -> log.debug("GetSignedConsentDetail: success: consentId:{}, aaName:{}", consentId, aaName));
    }

    SignedConsent deserializeSingedConsent(String json) {
        if (json != null) {
            try {
                return mapper.readValue(json, SignedConsent.class);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return null;
    }

    @Override
    public Mono<ConsentShortMeta> getConsentMeta(String consentId, String aaName) {
        return getSignedConsent(consentId, Optional.ofNullable(aaName))
                .map(signedConsentDTO -> {
                            if (signedConsentDTO.getConsentMode() == null) {
                                final SignedConsent signedConsent = deserializeSingedConsent(signedConsentDTO.getPayload());
                                if (signedConsent != null) {
                                    signedConsentDTO.setConsentMode(signedConsent.getConsentMode());
                                    signedConsentDTO.setDataLifeUnit(signedConsent.getDataLife().getUnit());
                                    signedConsentDTO.setDataLifeValue(signedConsent.getDataLife().getValue());
                                    consentStore.saveSignedConsent(signedConsentDTO);
                                }
                            }
                            return new ConsentShortMeta(signedConsentDTO.getConsentMode(),
                                    signedConsentDTO.getDataLifeUnit(),
                                    signedConsentDTO.getDataLifeValue());
                        }
                );
    }

    @Override
    public void handleConsentNotification(ConsentNotificationLog consentNotificationLog) {
        consentStore.logConsentNotification(consentNotificationLog);
    }

    @Override
    public Mono<ConsentStateDTO> getConsentState(String consentHandle, Optional<String> aaHandle) {
        log.debug("GetConsentState: start: consentHandle:{}, aaHandle:{}", consentHandle, aaHandle);
        return consentStore.consentStateByHandle(consentHandle)
                .flatMap(optionalConsentStateDTO -> {
                    if (optionalConsentStateDTO.isEmpty())
                        return fetchConsentStatus(consentHandle, aaHandle);
                    return Mono.just(optionalConsentStateDTO.get());
                })
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

    private Mono<ConsentStateDTO> fetchConsentStatus(String consentHandle, Optional<String> optionalAaHandle) {
        if (optionalAaHandle.isPresent()) {
            String aaName = optionalAaHandle.get();
            return getConsentStateDTOMono(consentHandle, aaName);
        }
        return consentStore
                .findRequestByConsentHandle(consentHandle)
                .flatMap(optionalConsentRequest ->
                        optionalConsentRequest
                                .map(ConsentRequestDTO::getAaName)
                                .map(aaName -> getConsentStateDTOMono(consentHandle, aaName))
                                .orElseThrow(() -> Errors.InvalidRequest.with(UUIDSupplier.get(),
                                        "Unable to get status for given consentHandle:" + consentHandle + ", try with aaHandle"))
                );
    }

    private Mono<ConsentStateDTO> getConsentStateDTOMono(String consentHandle, String aaName) {
        return aafiuClient
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
                        throwable.getMessage(), throwable));
    }
}
