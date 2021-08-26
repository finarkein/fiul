/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.aa;

import io.finarkein.api.aa.AAClient;
import io.finarkein.api.aa.AAClientBuilder;
import io.finarkein.api.aa.common.FIDataRange;
import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.api.aa.consent.handle.ConsentStatus;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.api.aa.dataflow.Consent;
import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.heartbeat.HeartbeatResponse;
import io.finarkein.api.aa.jws.JWSSigner;
import io.finarkein.fiul.*;
import io.finarkein.fiul.common.RequestUpdater;
import io.finarkein.fiul.common.UnSupportedCryptoService;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.FetchDataRequest;
import io.finarkein.fiul.validation.ConsentValidator;
import io.finarkein.fiul.validation.ConsentValidatorImpl;
import io.finarkein.fiul.validation.FIRequestValidator;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static io.finarkein.fiul.Functions.fiFetchResponseDecoder;

@Log4j2
class AAClientService implements AAFIUClient {

    private final AAClient aaClient;
    private final CacheService cache;
    private final RequestUpdater requestUpdater;
    private final CryptoServiceAdapter crypto;
    private final JWSSigner util;
    private final ConsentValidator consentValidator = new ConsentValidatorImpl();
    private final FIRequestValidator fiRequestValidator = new FIRequestValidator();

    public AAClientService(FiulWebClientConfig config, JWSSigner util) {
        this.util = util;
        aaClient = AAClientBuilder.Registry.builderFor("Default").build(config.getProperties(), util);
        requestUpdater = new RequestUpdater(config.getRequestTimestampSetter(), config.getRequestTxnIdSetter());

        cache = RequestCacheServiceBuilder.Registry.builderFor(config.getFiRequestCacheServiceName()).build(config.getProperties());

        log.info("Using RequestCacheService:{}", config.getFiRequestCacheServiceName());

        crypto = buildCryptoService(config);
        setDigitalSignatureUpdater(config);
    }

    private void setDigitalSignatureUpdater(FiulWebClientConfig config) {
        final String propertyName = "aa-client.firequest-digital-sign-auto-update";
        String value = config.getProperties().getPropertyIgnoreCase(propertyName, "true");
        final boolean isValueTrue = Boolean.parseBoolean(value);
        String message = "Server will not update FIRequest.Consent.DigitalSignature value automatically";
        if (isValueTrue) {
            requestUpdater.setDigitalSigUpdater(new SignedConsentArtefactFetcher(this, cache));
            message = "Server will update FIRequest.Consent.DigitalSignature value automatically";
        }
        log.info("property {}={} : {}", propertyName, isValueTrue, message);
    }

    private CryptoServiceAdapter buildCryptoService(FiulWebClientConfig config) {
        CryptoServiceAdapterBuilder builder = CryptoServiceAdapterBuilder.Registry.builderFor(config.getCryptoServiceName());
        if (builder != null) {
            log.info("CryptoService:{}", builder.adapterName());
            return builder.build(config.getProperties());
        }
        log.info("CryptoService is not configured");
        return UnSupportedCryptoService.INSTANCE;
    }

    @Override
    public Mono<HeartbeatResponse> heartBeat(String aaName) {
        return aaClient.heartBeat(aaName);
    }

    @Override
    public Mono<ConsentResponse> createConsent(FIUConsentRequest consentRequest) {
        requestUpdater.updateTxnIdIfNeeded(consentRequest);
        requestUpdater.updateTimestampIfNeeded(consentRequest);

        consentValidator.validateCreateConsent(consentRequest);
        String aaName = io.finarkein.api.aa.util.Functions.aaNameExtractor.apply(consentRequest.getConsentDetail().getCustomer().getId());
        return aaClient.createConsentRequest(consentRequest.toAAConsentRequest(), aaName);
    }

    @Override
    public Mono<ConsentHandleResponse> getConsentStatus(String consentHandleID, String aaName) {
        return aaClient.getConsentStatus(consentHandleID, aaName);
    }

    @Override
    public Mono<ConsentArtefact> getConsentArtefact(String consentID, String aaName) {
        final var cachedConsentArtefact = cache.getIfPresent(consentID);
        if (cachedConsentArtefact != null)
            return Mono.just(cachedConsentArtefact);

        return aaClient.getConsentArtefact(consentID, aaName)
                .doOnSuccess(cache::putConsentArtefact);
    }

    @Override
    public Mono<FIRequestResponse> createFIRequest(FIUFIRequest fiRequest, String aaName) {
        requestUpdater.updateTxnIdIfNeeded(fiRequest);
        requestUpdater.updateTimestampIfNeeded(fiRequest);
        requestUpdater.getDigitalSigUpdater().updateIfNeededAndCache(fiRequest.getConsent(), aaName);

        fiRequestValidator.validateFIRequestBody(fiRequest);

        return aaClient.createFIRequest(fiRequest.toAAFIRequest(), aaName);
    }

    @Override
    public Mono<FIFetchResponse> fiFetch(String dataSessionId, String aaName) {
        return aaClient.fiFetch(dataSessionId, aaName);
    }

    @Override
    public Mono<FIFetchResponse> fiFetch(String dataSessionId, String aaName, String fipId, String... linkRefNumber) {
        return aaClient.fiFetch(dataSessionId, aaName, fipId, linkRefNumber);
    }

    @Override
    @Deprecated
    public Mono<FIFetchResponse> fiFetchData(FetchDataRequest fetchDataRequest, String aaName) {
        log.debug("FetchDataRequest received for ConsentHandle:{}={}, checking status", fetchDataRequest.getConsentHandleId(), fetchDataRequest);

        final ConsentHandleResponse handleResponse = getConsentStatus(fetchDataRequest.getConsentHandleId(), aaName).toProcessor().block();
        Objects.requireNonNull(handleResponse, "ConsentHandle status not found");
        ConsentStatus consentStatus = handleResponse.getConsentStatus();
        if (!consentStatus.getStatus().equalsIgnoreCase("READY"))
            throw new IllegalStateException("ConsentHandle status is not READY, current state=" + consentStatus.getStatus());
        log.debug("ConsentHandle status is {}, ConsentArtefactId is:{}", consentStatus.getStatus(), consentStatus.getId());

        FIUFIRequest fiRequest = createFIRequest(fetchDataRequest, consentStatus);
        FIRequestResponse requestDataResponse = createFIRequest(fiRequest, aaName).doOnSuccess(response -> await()).toProcessor().block();
        Objects.requireNonNull(requestDataResponse, "FIRequestResponse cannot be null");
        return fiFetch(requestDataResponse.getSessionId(), aaName);
    }

    private static void await() {
        try {
            //It seems AA needs some time to prepare after FIFetch request is raised successfully,
            //because if we raise FIFetch request immediately after FIRequest it is observed that AA's response is 404
            //hence adding sleep
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private FIUFIRequest createFIRequest(FetchDataRequest fetchDataRequest, ConsentStatus consentStatus) {
        return new FIUFIRequest(fetchDataRequest.getVer(), RequestUpdater.TimestampUpdaters.currentTimestamp(),
                null, new FIDataRange(fetchDataRequest.getFrom(), fetchDataRequest.getTo()),
                new Consent(consentStatus.getId(), "NA"), fetchDataRequest.getKeyMaterial(), null);
    }

    @Override
    public Mono<io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse> fetchDecryptedData(FetchDataRequest fetchDataRequest, String aaName) {
        if (crypto == UnSupportedCryptoService.INSTANCE) {
            log.error("CryptoService not configured i.e. FIUClient is not configured for KeyMaterial generation");
            throw new IllegalArgumentException("CryptoService not configured i.e. FIU Client is not configured for KeyMaterial generation");
        }
        log.debug("fetchDecryptedData: generating KeyPair");
        final SerializedKeyPair generatedKeyPair = crypto.generateKey();
        fetchDataRequest.setKeyMaterial(generatedKeyPair.getKeyMaterial());
        log.debug("fetchDecryptedData: calling fiFetchData");

        return fiFetchData(fetchDataRequest, aaName)
                .checkpoint("fetchDecryptedData: aaFIFetchResponse received now decrypting FI data")
                .map(aaFIFetchResponse -> fiFetchResponseDecoder.decode(aaFIFetchResponse, crypto, generatedKeyPair))
                .checkpoint("fetchDecryptedData: aaFIFetchResponse -> FIFetchResponse done");
    }

    @Override
    public Mono<SerializedKeyPair> generateKeyMaterial() {
        if (crypto == UnSupportedCryptoService.INSTANCE) {
            log.error("CryptoService not configured i.e. FIUClient is not configured for KeyMaterial generation");
            throw new IllegalArgumentException("CryptoService not configured i.e. FIU Client is not configured for KeyMaterial generation");
        }
        return Mono.just(crypto.generateKey());
    }

    @Override
    public String name() {
        return FiulWebClientBuilder.NAME;
    }

    @Override
    public Mono<String> generateJWS(String body) {
        try {
            log.debug("Generating jws-signature for:{}", body);
            String jwsSig = util.sign(body);
            log.debug("jws-signature:{}", jwsSig);
            return Mono.just(jwsSig);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Mono<io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse> decryptFIFetchResponse(FIFetchResponse response,
                                                                                                    SerializedKeyPair keyPair) {
        if (crypto == UnSupportedCryptoService.INSTANCE) {
            var msg = "CryptoService not configured";
            log.error(msg);
            throw Errors.InternalError.with(response.getTxnid(), msg);
        }
        return Mono.just(fiFetchResponseDecoder.decode(response, crypto, keyPair));
    }
}
