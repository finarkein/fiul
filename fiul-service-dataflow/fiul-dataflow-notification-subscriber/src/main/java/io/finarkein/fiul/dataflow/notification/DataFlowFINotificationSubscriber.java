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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static io.finarkein.fiul.notification.ServiceConstants.CHANNEL_NAME_FI;
import static io.finarkein.fiul.notification.ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY;
import static io.finarkein.fiul.notification.config.CommonConfig.FIUL_EVENT_FACTORY;

@Log4j2
@Component
public class DataFlowFINotificationSubscriber implements FINotificationSubscriber {
    public static final String FI_NOTIFICATION_DATAFLOW_SUBSCRIBER = "Consumer.dataflow-service." + CHANNEL_NAME_FI;

    @Value("${" + NOTIFICATION_Q_TYPE_PROPERTY + "}")
    private String queueType;

    protected final DataFlowNotificationHandler dataFlowNotificationHandler;

    @Autowired
    public DataFlowFINotificationSubscriber(JmsTemplate jms, DataFlowNotificationHandler dataFlowNotificationHandler) {
        this.dataFlowNotificationHandler = dataFlowNotificationHandler;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("FINotificationSubscriber:{}, topic:{}, queue-type:{} initialized",
                getClass().getSimpleName(), FI_NOTIFICATION_DATAFLOW_SUBSCRIBER, queueType);
    }

    @Override
    @JmsListener(destination = FI_NOTIFICATION_DATAFLOW_SUBSCRIBER, containerFactory = FIUL_EVENT_FACTORY)
    public void handleFINotification(FINotification notification) {
        log.debug("fi-notification received:{}", notification);
        try {
            final FIStatusNotification fiStatusNotification = notification.getFIStatusNotification();
            dataFlowNotificationHandler.handleFINotification(notification);
            log.debug("FINotification handling done, sessionId:{}, sessionStatus:{}", fiStatusNotification.getSessionId(), fiStatusNotification.getSessionStatus());
        } catch (Exception e) {
            log.error("Error while processing dataFlowNotificationHandler.handleFINotification:{}", e.getMessage(), e);
        }
    }
}