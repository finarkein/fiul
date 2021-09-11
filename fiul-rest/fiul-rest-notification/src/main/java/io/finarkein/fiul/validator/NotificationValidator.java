/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validator;

import io.finarkein.aa.validators.BasicResponseValidator;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;

public class NotificationValidator {

    public static void validateConsentNotification(ConsentNotification consentNotification) {
        BasicResponseValidator.basicValidation(consentNotification.getTxnid(), consentNotification.getVer(), consentNotification.getTimestamp());
        if (!consentNotification.getNotifier().getType().equals("AA")) {
            throw Errors.InvalidRequest.with(consentNotification.getTxnid(), "Invalid Notifier type");
        }
//        DONE -
//              - 15min variation in timestamp field error
//              - Invalid version
//              - Invalid timestamp
//              - FIP in notifier type

//        TODO: - Alternate AA id
//              - Invalid consentId
//              - Invalid consentHandle
//              - Schematic error
//              - consent details of alternate AA timestamp error
//              - Verify that on making valid POST /Consent/Notification with PAUSED status, user is not able to make FI request
//              - Verify that on making valid POST /Consent/Notification with EXPIRED status, user is not able to make FI request
//              - Verify that on making valid POST /Consent/Notification with REVOKED status, user is not able to make FI requestInvalid version
//              - Invalid API Key
//              - Alternate AA API Key
    }

    public static void validateFINotification(FINotification fiNotification) {

//        TODO: - Alternate AA id
//              - Invalid version
//              - Invalid timestamp
//              - Invalid sessionId
//              - FIP in notifier type
//              - Alternate AA Id
//              - Schematic error
//              - Invalid txnId
//              - selected details of alternate AA
//              - Verify that on making valid POST /FI/Notification with FIStatusNotification.sessionStatus as EXPIRED, FIU Spoc is not able to make FI/fetch
//              - Invalid API Key
//              - Alternate AA API Key

    }
}
