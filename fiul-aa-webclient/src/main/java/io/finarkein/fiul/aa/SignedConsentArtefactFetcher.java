/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.aa;

import io.finarkein.api.aa.common.DigitalSigConsumer;
import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.fiul.AAFIUClient;
import io.finarkein.fiul.CacheService;
import io.finarkein.fiul.Functions;
import io.finarkein.fiul.common.RequestUpdater;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static io.finarkein.fiul.Functions.doGet;

/**
 * Fetches consentArtefact from fiu client if not found in cache and sets consentArtefact signature on DigitalSigConsumer i.e. on Consent
 */
@Log4j2
public class SignedConsentArtefactFetcher implements RequestUpdater.DigitalSignUpdater {
    private final AAFIUClient client;
    private final CacheService cache;

    public SignedConsentArtefactFetcher(AAFIUClient client, CacheService cache) {
        this.client = client;
        this.cache = cache;
    }

    private Callable<String> createConsentFetcher(String consentId, String aaName) {
        return () -> {
            log.debug("Fetching ConsentArtefact for consentId:{}, from:{}", consentId, aaName);
            final Mono<ConsentArtefact> consentArtefact = client.getConsentArtefact(consentId, aaName);
            return doGet(consentArtefact).getSignedConsent();
        };
    }

    @Override
    public boolean updateIfNeededAndCache(DigitalSigConsumer consumer, String aaName) {
        if(!aaName.equalsIgnoreCase("finvu"))
            return false;
        String signedConsentArtefact = cache.getIfSignedConsentArtefactPresent(consumer.getId());
        if (signedConsentArtefact == null) {
            try {
                signedConsentArtefact = cache.getSignedConsentArtefact(consumer.getId(), this.createConsentFetcher(consumer.getId(), aaName));
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        } else {
            log.debug("Using SignedConsentArtefact from cache for consentId:{}", consumer.getId());
        }
        final String signature = Functions.getSignature(signedConsentArtefact);
        if (signature != null) {
            consumer.setDigitalSignature(signature);
            return true;
        }
        return false;
    }
}
