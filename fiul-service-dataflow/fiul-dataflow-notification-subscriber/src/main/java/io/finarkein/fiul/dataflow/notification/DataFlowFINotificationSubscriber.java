/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.notification;

import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.api.aa.notification.FIStatusNotification;
import io.finarkein.fiul.dataflow.DataFlowNotificationHandler;
import io.finarkein.fiul.notification.FINotificationSubscriber;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DataFlowFINotificationSubscriber implements FINotificationSubscriber {
    protected final DataFlowNotificationHandler dataFlowNotificationHandler;

    @Autowired
    public DataFlowFINotificationSubscriber(DataFlowNotificationHandler dataFlowNotificationHandler) {
        this.dataFlowNotificationHandler = dataFlowNotificationHandler;
    }

    public void handleFINotification(FINotification notification) {
        log.debug("fi-notification received:{}", notification);
        final FIStatusNotification fiStatusNotification = notification.getFIStatusNotification();
        dataFlowNotificationHandler.handleFINotification(notification);
        log.debug("FINotification handling done, sessionId:{}, sessionStatus:{}", fiStatusNotification.getSessionId(), fiStatusNotification.getSessionStatus());
    }
}