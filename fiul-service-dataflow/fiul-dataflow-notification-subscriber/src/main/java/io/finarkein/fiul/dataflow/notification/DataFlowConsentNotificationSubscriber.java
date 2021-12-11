/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.notification;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.fiul.dataflow.DataFlowNotificationHandler;
import io.finarkein.fiul.notification.ConsentNotificationSubscriber;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DataFlowConsentNotificationSubscriber implements ConsentNotificationSubscriber {

    protected final DataFlowNotificationHandler dataFlowNotificationHandler;

    @Autowired
    public DataFlowConsentNotificationSubscriber(DataFlowNotificationHandler dataFlowNotificationHandler) {
        this.dataFlowNotificationHandler = dataFlowNotificationHandler;
    }

    public void handleConsentNotification(ConsentNotification notification) {
        log.debug("notification received:{}", notification);

        final var consentStatusNotification = notification.getConsentStatusNotification();
        dataFlowNotificationHandler.handleConsentNotification(notification);
        log.debug("ConsentNotification handling done, consentHandleId:{}, consentId:{}", consentStatusNotification.getConsentHandle(), consentStatusNotification.getConsentId());
    }

}