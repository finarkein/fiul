/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.jms.pub;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.notification.NotificationPublisher;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static io.finarkein.fiul.notification.ServiceConstants.IN_MEM;
import static io.finarkein.fiul.notification.ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY;

@Log4j2
@Service
@ConditionalOnProperty(name = NOTIFICATION_Q_TYPE_PROPERTY, havingValue = IN_MEM, matchIfMissing = true)
@RefreshScope
class InMemNotificationPublisher implements NotificationPublisher {

    protected final JmsTemplate jms;
    protected final String consentNotificationTopicName;
    protected final String fiNotificationTopicName;

    @Autowired
    protected InMemNotificationPublisher(@Qualifier("notification") JmsTemplate jms,
                                         @Value("${fiul.notification.consent-queue-name}") String consentNotificationTopicName,
                                         @Value("${fiul.notification.fi-queue-name}") String fiNotificationTopicName) {
        this.jms = jms;
        this.consentNotificationTopicName = consentNotificationTopicName;
        this.fiNotificationTopicName = fiNotificationTopicName;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("InMemNotificationPublisher:{} for topics:[{},{}] initialized",
                getClass().getSimpleName(), consentNotificationTopicName, fiNotificationTopicName);
    }

    public void publishConsentNotification(ConsentNotification consentNotification) {
        jms.convertAndSend(new ActiveMQTopic(consentNotificationTopicName), consentNotification);
    }

    public void publishFINotification(FINotification fiNotification) {
        jms.convertAndSend(new ActiveMQTopic(fiNotificationTopicName), fiNotification);
    }
}
