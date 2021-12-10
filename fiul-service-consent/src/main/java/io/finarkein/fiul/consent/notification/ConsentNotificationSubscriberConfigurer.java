/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.notification;

import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.fiul.notification.config.JmsUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.Message;
import java.util.UUID;

import static io.finarkein.fiul.notification.ServiceConstants.IN_MEM;
import static io.finarkein.fiul.notification.ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY;

@Log4j2
@EnableJms
@Configuration
@ConditionalOnProperty(name = NOTIFICATION_Q_TYPE_PROPERTY, havingValue = IN_MEM, matchIfMissing = true)
public class ConsentNotificationSubscriberConfigurer implements JmsListenerConfigurer {

    protected final String consentNotificationTopicName;
    protected final ConsentStatusNotificationSubscriber subscriber;
    protected final JmsListenerContainerFactory<?> listenerContainerFactory;
    protected final MessageConverter jacksonJmsMessageConverter;
    protected final String queueType;

    @Autowired
    ConsentNotificationSubscriberConfigurer(@Value("${fiul.notification.consent-queue-name}") String consentNotificationTopicName,
                                            ConsentStatusNotificationSubscriber subscriber,
                                            @Qualifier("fiul-events-factory") JmsListenerContainerFactory<?> listenerContainerFactory,
                                            @Qualifier("JacksonMessageConverter") MessageConverter jacksonJmsMessageConverter,
                                            @Value("${" + NOTIFICATION_Q_TYPE_PROPERTY + "}") String queueType){

        this.consentNotificationTopicName = consentNotificationTopicName;
        this.subscriber = subscriber;
        this.listenerContainerFactory = listenerContainerFactory;
        this.jacksonJmsMessageConverter = jacksonJmsMessageConverter;
        this.queueType = queueType;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar jmsListenerEndpointRegistrar) {
        var jmsListenerEndpoint  = JmsUtil.createSimpleEndpoint(
                "Consumer.consent-service." + consentNotificationTopicName,
                message -> handleConsentNotification(message, jacksonJmsMessageConverter)
                );
        jmsListenerEndpointRegistrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);

        log.info("JmsListenerEndpoint registered:{}, queueType:{}", jmsListenerEndpoint, queueType);
    }

    protected void handleConsentNotification(Message message, MessageConverter converter) {
        try {
            final ConsentNotification consentNotification = (ConsentNotification) converter.fromMessage(message);
            subscriber.handleConsentNotification(consentNotification);
        } catch (Exception e) {
            final String errorMessage = String
                    .format("Error in ConsentNotification-consent-service-handler error:%s", e.getMessage());
            log.error(errorMessage, e);
            throw Errors.InternalError.with(UUID.randomUUID().toString(), errorMessage, e);
        }
    }
}
