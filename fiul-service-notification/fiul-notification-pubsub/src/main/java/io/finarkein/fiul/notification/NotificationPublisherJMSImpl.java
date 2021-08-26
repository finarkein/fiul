/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static io.finarkein.fiul.notification.ServiceConstants.*;

@Log4j2
@Service
class NotificationPublisherJMSImpl implements NotificationPublisher {

    protected final JmsTemplate jms;

    @Value("${" + NOTIFICATION_Q_TYPE_PROPERTY + "}")
    private String queueType;

    @Autowired
    protected NotificationPublisherJMSImpl(JmsTemplate jms) {
        this.jms = jms;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("NotificationPublisher:{} for topics:[{},{}], queue-type:{} initialized",
                getClass().getSimpleName(), CHANNEL_NAME_CONSENT, CHANNEL_NAME_FI, queueType);
    }

    public void publishConsentNotification(ConsentNotification consentNotification) {
        jms.convertAndSend(new ActiveMQTopic(CHANNEL_NAME_CONSENT), consentNotification);
    }

    public void publishFINotification(FINotification fiNotification) {
        jms.convertAndSend(new ActiveMQTopic(CHANNEL_NAME_FI), fiNotification);
    }
}
