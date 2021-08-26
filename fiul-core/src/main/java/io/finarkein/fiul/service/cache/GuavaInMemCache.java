/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.service.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.fiul.CacheService;
import io.finarkein.fiul.RequestCacheServiceBuilder;
import lombok.NoArgsConstructor;
import org.kohsuke.MetaInfServices;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
class GuavaInMemCache implements CacheService {
    static final String GUAVA_CACHE_NAME = "in-mem";
    private final Cache<String, String> signedConsents;
    private final Cache<String, ConsentArtefact> consentArtefactCache;

    GuavaInMemCache(Properties properties) {

        final String concurrencyLevelStr = properties.getProperty("concurrencyLevel", "8");
        int concurrencyLevel = Integer.parseInt(concurrencyLevelStr);

        signedConsents = CacheBuilder.newBuilder()
                .concurrencyLevel(concurrencyLevel)
                .initialCapacity(16)
                .maximumSize(1024)
                .build();

        consentArtefactCache = CacheBuilder.newBuilder()
                .concurrencyLevel(concurrencyLevel)
                .initialCapacity(16)
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .maximumSize(1024)
                .build();
    }

    @Override
    public ConsentArtefact getIfPresent(String consentId) {
        return consentArtefactCache.getIfPresent(consentId);
    }

    @Override
    public CacheService putConsentArtefact(ConsentArtefact artefact) {
        consentArtefactCache.put(artefact.getConsentId(), artefact);
        return this;
    }

    @Override
    public String getSignedConsentArtefact(String consentId, Callable<? extends String> callable) throws ExecutionException {
        return signedConsents.get(consentId, callable);
    }

    @Override
    public String getIfSignedConsentArtefactPresent(String consentId) {
        return signedConsents.getIfPresent(consentId);
    }

    @MetaInfServices
    @NoArgsConstructor
    public static class GuavaRequestCacheBuilder implements RequestCacheServiceBuilder {

        @Override
        public String name() {
            return GUAVA_CACHE_NAME;
        }

        @Override
        public CacheService build(Properties properties) {
            return new GuavaInMemCache(properties);
        }
    }
}
