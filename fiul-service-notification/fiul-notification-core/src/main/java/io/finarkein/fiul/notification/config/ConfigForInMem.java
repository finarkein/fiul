/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

import static io.finarkein.fiul.notification.ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY;
import static io.finarkein.fiul.notification.config.ConfigForInMem.IN_MEM;

@EnableJms
@Configuration
@ConditionalOnProperty(name = NOTIFICATION_Q_TYPE_PROPERTY, havingValue = IN_MEM, matchIfMissing = true)
public class ConfigForInMem extends CommonConfig{
    public static final String IN_MEM = "in-mem";

    @Bean
    @Qualifier("notification")
    public JmsTemplate managedJmsTemplate(@Qualifier("notificationConnection") ConnectionFactory connectionFactoryNotification) {
        return new JmsTemplate(connectionFactoryNotification);
    }

    @Bean
    @Qualifier("notificationConnection")
    public ConnectionFactory connectionFactoryNotification() {
        return new ActiveMQConnectionFactory("vm://localhost");
    }

}
