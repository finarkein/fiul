/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback;

import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
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
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.support.converter.MessageConverter;

import java.util.UUID;

import static io.finarkein.fiul.notification.ServiceConstants.IN_MEM;
import static io.finarkein.fiul.notification.ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY;
import static io.finarkein.fiul.notification.config.ConfigForInMem.FIUL_EVENT_FACTORY;

@Log4j2
@EnableJms
@Configuration
@ConditionalOnProperty(name = NOTIFICATION_Q_TYPE_PROPERTY, havingValue = IN_MEM, matchIfMissing = true)
public class InMemCallbackListenerConfigurer implements JmsListenerConfigurer {

    @Autowired
    @Qualifier(FIUL_EVENT_FACTORY)
    private JmsListenerContainerFactory<?> listenerContainerFactory;

    @Value("${fiul.notification.consent-queue-name}")
    private String consentNotificationTopicName;

    @Value("${fiul.notification.fi-queue-name}")
    private String fiNotificationTopicName;

    @Autowired
    private CallbackProcessor callbackProcessor;

    @Autowired
    @Qualifier("JacksonMessageConverter")
    public MessageConverter messageConverter;

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {

        var jmsListenerEndpoint = createCallbackProcessorForConsentNotification(messageConverter);
        registrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);

        log.info("JmsListenerEndpoint registered with id:{}, destination:{}",
                jmsListenerEndpoint.getId(),
                jmsListenerEndpoint.getDestination());

        jmsListenerEndpoint = createCallbackProcessorForFINotification(messageConverter);
        registrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);
        log.info("JmsListenerEndpoint registered with id:{}, destination:{}",
                jmsListenerEndpoint.getId(),
                jmsListenerEndpoint.getDestination());
    }

    private SimpleJmsListenerEndpoint createCallbackProcessorForFINotification(MessageConverter converter) {
        final String callbackProcessorForFiNotificationName = "Consumer.callback-processor." + fiNotificationTopicName;
        SimpleJmsListenerEndpoint fiNotificationEndpoint = new SimpleJmsListenerEndpoint();
        fiNotificationEndpoint.setId("callbackProcessorForFiNotification_" + System.currentTimeMillis());
        fiNotificationEndpoint.setDestination(callbackProcessorForFiNotificationName);
        fiNotificationEndpoint.setMessageListener(message -> {
            try {
                final var fiNotification = (FINotification) converter.fromMessage(message);
                callbackProcessor.handleFINotification(fiNotification);
            } catch (Exception e) {
                final String errorMessage = String.format("Error in FINotification-callback-processor:%s, error:%s"
                        , callbackProcessorForFiNotificationName, e.getMessage());
                log.error(errorMessage, e);
                throw Errors.InternalError.with(UUID.randomUUID().toString(), errorMessage, e);
            }
        });
        return fiNotificationEndpoint;
    }

    private SimpleJmsListenerEndpoint createCallbackProcessorForConsentNotification(MessageConverter converter) {
        final String callbackProcessorForConsentNotificationName = "Consumer.callback-processor." + consentNotificationTopicName;
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId("callbackProcessorForConsentNotification_" + System.currentTimeMillis());
        endpoint.setDestination(callbackProcessorForConsentNotificationName);
        endpoint.setMessageListener(message -> {
            try {
                final ConsentNotification consentNotification = (ConsentNotification) converter.fromMessage(message);
                callbackProcessor.handleConsentNotification(consentNotification);
            } catch (Exception e) {
                final String errorMessage = String.format("Error in ConsentNotification-callback-processor:%s, error:%s"
                        , callbackProcessorForConsentNotificationName, e.getMessage());
                log.error(errorMessage, e);
                throw Errors.InternalError.with(UUID.randomUUID().toString(), errorMessage, e);
            }
        });
        return endpoint;
    }
}
