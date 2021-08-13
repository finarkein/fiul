/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.service.cache;

import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.fiul.CacheService;
import io.finarkein.fiul.RequestCacheServiceBuilder;
import lombok.NoArgsConstructor;
import org.kohsuke.MetaInfServices;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


class NoOpReqCacheService implements CacheService {
    public static final String SERVICE_NAME = "no-op";

    @Override
    public ConsentArtefact getIfPresent(String consentId) {
        return null;
    }

    @Override
    public CacheService putConsentArtefact(ConsentArtefact artefact) {
        return null;
    }

    @Override
    public String getSignedConsentArtefact(String consentId, Callable<? extends String> callable) throws ExecutionException {
        return null;
    }

    @Override
    public String getIfSignedConsentArtefactPresent(String consentId) {
        return null;
    }

    @MetaInfServices
    @NoArgsConstructor
    public static class NoOpReqCacheServiceBuilder implements RequestCacheServiceBuilder {

        @Override
        public String name() {
            return SERVICE_NAME;
        }

        @Override
        public NoOpReqCacheService build(Properties properties) {
            return new NoOpReqCacheService();
        }
    }
}
