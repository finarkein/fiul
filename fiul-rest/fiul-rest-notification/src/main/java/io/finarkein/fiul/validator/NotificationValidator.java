/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validator;

import io.finarkein.aa.registry.models.EntityInfo;
import io.finarkein.aa.validators.BasicResponseValidator;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.consent.model.ConsentState;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationValidator {

    private static final String REQUIRED_NOTIFIER_TYPE = "AA";
    private static final Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static boolean isValidUUID(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

    public static void validateConsentNotification(ConsentNotification consentNotification, ConsentState consentState, EntityInfo entityInfo) {
        BasicResponseValidator.basicValidation(consentNotification.getTxnid(), consentNotification.getVer(), consentNotification.getTimestamp(), "ConsentNotification");
        if (consentState == null)
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent data not found for given txnId");
        if (consentState.getConsentStatus() != null && consentState.getConsentStatus().equalsIgnoreCase("FAILED"))
                throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent Status is failed");
        if (!consentState.isWasSuccessful())
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent creation was failed");
        if (!consentNotification.getNotifier().getType().equals(REQUIRED_NOTIFIER_TYPE)) {
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Invalid Notifier type");
        }
        if (!isValidUUID(consentNotification.getConsentStatusNotification().getConsentId()))
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent Id is invalid");

        if (!consentNotification.getConsentStatusNotification().getConsentHandle().equals(consentState.getConsentHandle()))
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "ConsentHandle Id is invalid");
//        if (!consentNotification.getConsentStatusNotification().getConsentId().equals(consentState.getConsentId()))
//            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent Id is invalid");
        if (!consentNotification.getNotifier().getId().equals(entityInfo.getId()))
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent notifier Id is invalid");

        //DONE -
        // - 15min variation in timestamp field error
        // - Invalid version
        // - Invalid timestamp
        // - FIP in notifier type
        // - Alternate AA id
        // - Invalid consentId
        // - Invalid consentHandle
        // - Verify that on making valid POST /Consent/Notification with PAUSED status, user is not able to make FI request
        // - Verify that on making valid POST /Consent/Notification with EXPIRED status, user is not able to make FI request
        // - Verify that on making valid POST /Consent/Notification with REVOKED status, user is not able to make FI requestInvalid version
        // - Invalid API Key
        // - Alternate AA API Key

        //TODO:
        // - Schematic error
        // - consent details of alternate AA timestamp error
    }

    public static void validateFINotification(FINotification fiNotification, ConsentState consentState, EntityInfo entityInfo) {
        BasicResponseValidator.basicValidation(fiNotification.getTxnid(), fiNotification.getVer(), fiNotification.getTimestamp(), "FINotification");
        if (!fiNotification.getNotifier().getType().equals(REQUIRED_NOTIFIER_TYPE)) {
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Invalid Notifier type");
        }

        if (consentState == null)
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "TxnId is invalid");

        if (!consentState.isPostFISuccessful())
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Post FI was not successful");

        if (!isValidUUID(fiNotification.getTxnid()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Txn Id is invalid");

        if (!fiNotification.getNotifier().getId().equals(entityInfo.getId()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "FI notifier Id is invalid");
        if (!fiNotification.getFIStatusNotification().getSessionId().equals(consentState.getDataSessionId()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "FI session Id is invalid");

        //DONE -
        // - 15min variation in timestamp field error
        // - Invalid version
        // - Invalid timestamp
        // - FIP in notifier type
        // - Invalid sessionId
        // - Alternate AA Id
        // - Invalid txnId

        //TODO:
        // - Schematic error
        // - selected details of alternate AA
        // - Verify that on making valid POST /FI/Notification with FIStatusNotification.sessionStatus as EXPIRED,
        //   FIU Spec is not able to make FI/fetch
        // - Invalid API Key
        // - Alternate AA API Key

    }
}
