/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.aa.registry.RegistryService;
import io.finarkein.aa.registry.RegistryServiceImpl;
import io.finarkein.aa.registry.models.EntityInfo;
import io.finarkein.aa.validators.BasicResponseValidator;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.webclient.helpers.AAInfoRegistry;
import io.finarkein.api.aa.webclient.jws.Signer;
import io.finarkein.fiul.common.Properties;
import io.finarkein.fiul.consent.model.ConsentState;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.regex.Pattern;

import static io.finarkein.aa.registry.RegistryService.REGISTRY_URL_PROPERTY_NAME;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationValidator {


    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        UUID.randomUUID();
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

    public static void validateConsentNotification(ConsentNotification consentNotification, ConsentState consentState, EntityInfo entityInfo) {
        BasicResponseValidator.basicValidation(consentNotification.getTxnid(), consentNotification.getVer(), consentNotification.getTimestamp(), "ConsentNotification");
        if (!consentState.isWasSuccessful())
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent creation was failed");
        if (!consentNotification.getNotifier().getType().equals("AA")) {
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Invalid Notifier type");
        }
        if (!isValidUUID(consentNotification.getConsentStatusNotification().getConsentId()))
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent Id is invalid");
        if (consentState == null)
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Consent data not found for given txnId");
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
        if (!fiNotification.getNotifier().getType().equals("AA")) {
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Invalid Notifier type");
        }
        if (!consentState.isPostFISuccessful())
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Post FI was not successful");
        if (!isValidUUID(fiNotification.getTxnid()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "Txn Id is invalid");
        if (consentState == null)
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "TxnId is invalid");
        if (!fiNotification.getNotifier().getId().equals(entityInfo.getId()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "FI notifier Id is invalid");
        if (!fiNotification.getFIStatusNotification().getSessionId().equals(consentState.getDataSessionId()))
            throw Errors.InvalidRequest.with(fiNotification.getTxnid(), "FI session Id is invalid");
        if (!fiNotification)

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
