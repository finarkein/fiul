/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validator;

import io.finarkein.aa.registry.models.EntityInfo;
import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.aa.validators.BasicResponseValidator;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.config.model.AaApiKeyBody;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.dataflow.dto.FIRequestState;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationValidator {

    private static final String REQUIRED_NOTIFIER_TYPE = "AA";

    public static void validateConsentNotification(ConsentNotification consentNotification, ConsentStateDTO consentStateDTO,
                                                   EntityInfo entityInfo, AaApiKeyBody aaApiKeyBody) {
        BasicResponseValidator.basicValidation(consentNotification.getTxnid(), consentNotification.getVer(),
                consentNotification.getTimestamp(), "ConsentNotification");
        if (!consentNotification.getNotifier().getId().equals(aaApiKeyBody.getClientId()))
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Api key of alternate AA sent");
        if (consentStateDTO == null)
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent data not found for given txnId");
        if (consentStateDTO.getConsentStatus() != null && consentStateDTO.getConsentStatus().equalsIgnoreCase("FAILED"))
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent Status is failed");
        if (consentStateDTO.getIsPostConsentSuccessful() != null && !consentStateDTO.getIsPostConsentSuccessful())
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent creation was failed");
        if (!consentNotification.getNotifier().getType().equals(REQUIRED_NOTIFIER_TYPE)) {
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Invalid Notifier type");
        }
        ArgsValidator.isValidUUID(consentNotification.getTxnid(), consentNotification.getConsentStatusNotification().getConsentId(),
                "ConsentId");

        if (!consentNotification.getConsentStatusNotification().getConsentHandle().equals(consentStateDTO.getConsentHandle()))
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "ConsentHandle Id is invalid");
//        if (!consentNotification.getConsentStatusNotification().getConsentId().equals(consentState.getConsentId()))
//            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent Id is invalid");
        if (!consentNotification.getNotifier().getId().equals(entityInfo.getId()))
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent notifier Id is invalid");
    }

    public static void validateFINotification(FINotification fiNotification, FIRequestState fiRequestState,
                                              EntityInfo entityInfo, AaApiKeyBody aaApiKeyBody) {
        BasicResponseValidator.basicValidation(fiNotification.getTxnid(), fiNotification.getVer(), fiNotification.getTimestamp(),
                "FINotification");
        if (!fiNotification.getNotifier().getId().equals(aaApiKeyBody.getClientId()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Api key of alternate AA sent");
        if (!fiNotification.getNotifier().getType().equals(REQUIRED_NOTIFIER_TYPE)) {
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Invalid Notifier type");
        }
        if (fiRequestState == null)
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "TxnId is invalid");
        if (!fiRequestState.isFiRequestSuccessful())
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Post FI was not successful");
        if (!fiNotification.getNotifier().getId().equals(entityInfo.getId()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "FI notifier Id is invalid");
        if (!fiNotification.getFIStatusNotification().getSessionId().equals(fiRequestState.getSessionId()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "FI session Id is invalid");
        fiNotification.getFIStatusNotification().getFiStatusResponse().forEach(
                fiStatusResponse -> {
                    fiStatusResponse.getAccounts().forEach(
                            account -> {
                                if (account.getLinkRefNumber() == null)
                                    throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "LinkRefNumber is null");
                                if (account.getFiStatus() == null)
                                    throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "FI Status is null");
                                if (account.getDescription() == null || account.getDescription().trim().length() == 0)
                                    throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Description is null");
                            }
                    );
                }
        );
    }
}
