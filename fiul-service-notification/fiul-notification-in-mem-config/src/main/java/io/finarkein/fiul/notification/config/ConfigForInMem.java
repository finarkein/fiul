/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.config;

import io.finarkein.fiul.notification.ServiceConstants;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

@Configuration
@ConditionalOnProperty(name = ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY, havingValue = ServiceConstants.IN_MEM, matchIfMissing = true)
public class ConfigForInMem {
    public static final String FIUL_EVENT_FACTORY = "fiul-events-factory";

    @Bean(FIUL_EVENT_FACTORY)
    public JmsListenerContainerFactory<?> fiuEventFactory(@Qualifier("notificationConnection") ConnectionFactory connectionFactory,
                                                          DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter());
        return factory;
    }

    @Bean
    @Qualifier("JacksonMessageConverter")
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Primary
    @Bean
    @Qualifier("notification")
    public JmsTemplate inMemJmsTemplate(@Qualifier("notificationConnection") ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        return jmsTemplate;
    }

    @Bean
    @Qualifier("notificationConnection")
    public ConnectionFactory connectionFactoryNotification() {
        return new ActiveMQConnectionFactory("vm://localhost");
    }
}
