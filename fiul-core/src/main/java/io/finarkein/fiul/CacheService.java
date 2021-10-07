/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import io.finarkein.api.aa.consent.artefact.ConsentArtefact;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * TODO refactor this interface
 */
public interface CacheService {

    ConsentArtefact getIfPresent(String consentId);

    CacheService putConsentArtefact(ConsentArtefact artefact);

    String getSignedConsentArtefact(String consentId, Callable<? extends String> callable) throws ExecutionException;

    void putSignedConsentArtefact(String consentId, String signature);

    String getIfSignedConsentArtefactPresent(String consentId);

}