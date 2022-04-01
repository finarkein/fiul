/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.heartbeat.HeartbeatResponse;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.FetchDataRequest;
import reactor.core.publisher.Mono;

/**
 * Represents the interface to interact with Account Aggregators FIU module.<br>
 * Its concrete implementations can be,<br>
 * <li>Webclient which directly connect to AA FIU module</li>
 * <ul>
 * Following properties used to configure the Client,<br><br>
 * <code>
 * #valid values = {generateIfNull,generateAuto,noop}<br>
 * aa-client.request-timestamp-setter=generateAuto<br>
 * aa-client.request-txn-id-setter=generateAuto<br>
 * aa-client.crypto-service=defaultCryptoService<br>
 * </code></ul>
 * <li>Java client which connects to FIU microservice(AAController) deployed in secured domain</li>
 * <ul>
 * Following properties used to configure the Client,<br><br>
 * <code>
 * aa-fiu-service.baseUrl=http://localhost:8082/aaAPI<br><br>
 * #valid values = {generateIfNull,generateAuto,noop}<br>
 * aa-client.request-timestamp-setter=generateAuto<br>
 * aa-client.request-txn-id-setter=generateAuto<br>
 * aa-client.crypto-service=defaultCryptoService<br><br>
 * </code></ul>
 */
public interface AAFIUClient {

    Mono<HeartbeatResponse> heartBeat(String aaName);

    Mono<ConsentResponse> createConsent(FIUConsentRequest consentRequest);

    Mono<ConsentHandleResponse> getConsentStatus(String consentHandleID, String aaName);

    Mono<ConsentArtefact> getConsentArtefact(String consentID, String aaName);

    Mono<FIRequestResponse> createFIRequest(FIUFIRequest fiRequest, String aaName);

    Mono<FIFetchResponse> fiFetch(String dataSessionId, String aaName);

    Mono<FIFetchResponse> fiFetch(String dataSessionId, String aaName, String fipId, String... linkRefNumber);

    /**
     * Short hand api which does following,<br>
     * 1. For given consentHandle checks for Consent-Artefact, if its ready then<br>
     * 2. Posts FI-Request i.e. raises fetch request with AA and receives data-session-id<br>
     * 3. Using data-session-id posts FI-Fetch-Request and returns the FIData<br>
     *
     * @param fetchDataRequest
     * @param aaName
     * @return
     */
    @Deprecated
    Mono<FIFetchResponse> fiFetchData(FetchDataRequest fetchDataRequest, String aaName);

    /**
     * Short hand api which does following,<br>
     * 1. For given consentHandle checks for Consent-Artefact status, if its ready then<br>
     * 2. Posts FI-Request i.e. raises fetch request with AA and receives data-session-id; provided {@link CryptoServiceAdapter} is configured<br>
     * 3. Using data-session-id posts FI-Fetch-Request and returns the decrypted FIData if crypto service is configured<br>
     * {@link CryptoServiceAdapterBuilder} can be implemented to plugin the CryptoService<br>
     *
     * @param fetchDataRequest
     * @param aaName
     * @return
     */
    Mono<io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse> fetchDecryptedData(FetchDataRequest fetchDataRequest, String aaName);

    /**
     * @return SerializedKeyPair; provided {@link CryptoServiceAdapter} is configured
     */
    Mono<SerializedKeyPair> generateKeyMaterial();

    SerializedKeyPair getOrCreateKeyMaterial();

    default Mono<String> generateJWS(String body) {
        return null;
    }

    Mono<io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse> decryptFIFetchResponse(FIFetchResponse response, SerializedKeyPair keyPair);
}
