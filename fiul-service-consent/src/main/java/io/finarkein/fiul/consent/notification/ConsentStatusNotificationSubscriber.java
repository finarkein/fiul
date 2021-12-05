/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.notification;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.notification.ConsentNotificationSubscriber;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.finarkein.api.aa.util.Functions.strToTimeStamp;

@Log4j2
@Component
public class ConsentStatusNotificationSubscriber implements ConsentNotificationSubscriber {

    protected final ConsentService consentService;

    @Autowired
    public ConsentStatusNotificationSubscriber(ConsentService consentService) {
        this.consentService = consentService;
    }

    public void handleConsentNotification(ConsentNotification notification) {
        log.debug("notification received:{}", notification);
        final var consentStatusNotification = notification.getConsentStatusNotification();
        try {
            consentService.handleConsentNotification(ConsentNotificationLog.builder()
                    .version(notification.getVer())
                    .txnId(notification.getTxnid())
                    .notificationTimestamp(strToTimeStamp.apply(notification.getTimestamp()))
                    .notifierType(notification.getNotifier().getType())
                    .notifierId(notification.getNotifier().getId())
                    .consentHandle(consentStatusNotification.getConsentHandle())
                    .consentId(consentStatusNotification.getConsentId())
                    .consentState(consentStatusNotification.getConsentStatus())
                    .build());
            log.debug("ConsentNotification handling done, consentHandleId:{}, consentId:{}", consentStatusNotification.getConsentHandle(), consentStatusNotification.getConsentId());
        } catch (Exception e) {
            log.error("Error while persisting ConsentNotificationLog:{}", e.getMessage(), e);
        }
    }
}