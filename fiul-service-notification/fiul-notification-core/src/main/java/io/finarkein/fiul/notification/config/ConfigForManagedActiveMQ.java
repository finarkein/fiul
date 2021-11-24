/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.config;

import io.finarkein.fiul.notification.ServiceConstants;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@EnableJms
@Configuration
@Log4j2
@ConditionalOnProperty(name = ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY, havingValue = ConfigForManagedActiveMQ.MANAGED_ACTIVE_MQ)
public class ConfigForManagedActiveMQ extends CommonConfig {

    public static final String MANAGED_ACTIVE_MQ = "managed-active-mq";

    @Value("${fiul.notification.queue.broker-url}")
    private String brokerURL;

    @Bean
    @Qualifier("notification")
    public JmsTemplate managedJmsTemplate(@Qualifier("notificationConnection") ActiveMQConnectionFactory connectionFactoryNotification) {
        JmsTemplate template = new JmsTemplate(createPooledConnectionFactory(connectionFactoryNotification));
        template.setMessageConverter(jacksonJmsMessageConverter());
        return template;
    }

    protected PooledConnectionFactory createPooledConnectionFactory(@Qualifier("notificationConnection") ActiveMQConnectionFactory connectionFactoryNotification) {
        // Create a pooled connection factory.
        final PooledConnectionFactory pooledConnectionFactory =
                new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(connectionFactoryNotification);
        pooledConnectionFactory.setMaxConnections(10);
        return pooledConnectionFactory;
    }

    @Bean
    @Qualifier("notificationConnection")
    public ActiveMQConnectionFactory connectionFactoryNotification(
            @Value("${fiul.notification.queue.username}") String userName,
            @Value("${fiul.notification.queue.password}") String password
    ) {
        // Create a connection factory.
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        if(userName != null) {
            // Pass the username and password
            connectionFactory.setUserName(userName);
            connectionFactory.setPassword(password);
        }
        log.info("CallbackService config: BrokerUrl='{}'",brokerURL);
        return connectionFactory;
    }
}
