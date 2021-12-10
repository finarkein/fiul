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
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.Message;
import java.util.Objects;
import java.util.UUID;

import static io.finarkein.fiul.notification.ServiceConstants.IN_MEM;
import static io.finarkein.fiul.notification.ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY;
import static io.finarkein.fiul.notification.config.ConfigForInMem.FIUL_EVENT_FACTORY;

@Log4j2
@EnableJms
@Configuration
@ConditionalOnProperty(name = NOTIFICATION_Q_TYPE_PROPERTY, havingValue = IN_MEM, matchIfMissing = true)
public class InMemCallbackListenerConfigurer implements JmsListenerConfigurer {

    private final JmsListenerContainerFactory<?> listenerContainerFactory;
    private final String consentNotificationTopicName;
    private final String fiNotificationTopicName;
    private final CallbackProcessor callbackProcessor;
    private final MessageConverter messageConverter;

    @Autowired
    public InMemCallbackListenerConfigurer(@Value("${fiul.notification.consent-queue-name}") String consentNotificationTopicName,
                                           @Value("${fiul.notification.fi-queue-name}") String fiNotificationTopicName,
                                           @Qualifier(FIUL_EVENT_FACTORY) JmsListenerContainerFactory<?> listenerContainerFactory,
                                           @Qualifier("JacksonMessageConverter") MessageConverter messageConverter,
                                           CallbackProcessor callbackProcessor) {
        this.consentNotificationTopicName = consentNotificationTopicName;
        this.fiNotificationTopicName = fiNotificationTopicName;
        this.callbackProcessor = callbackProcessor;
        this.listenerContainerFactory = listenerContainerFactory;
        this.messageConverter = messageConverter;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        SimpleJmsListenerEndpoint jmsListenerEndpoint =  JmsUtil.createSimpleEndpoint(
                "Consumer.callback-processor." + consentNotificationTopicName,
                message -> handleConsentNotification(messageConverter, message)
        );
        registrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);

        log.info("ConsentNotification JmsListenerEndpoint registered:{}", jmsListenerEndpoint);

        jmsListenerEndpoint = JmsUtil.createSimpleEndpoint(
                "Consumer.callback-processor." + fiNotificationTopicName,
                message -> handleFINotification(messageConverter, message)
        );
        registrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);

        log.info("FINotification JmsListenerEndpoint registered:{}", jmsListenerEndpoint);
    }

    private void handleFINotification(MessageConverter converter, Message message) {
        FINotification fiNotification = null;
        try {
            fiNotification = (FINotification) converter.fromMessage(message);
            callbackProcessor.handleFINotification(fiNotification);
        } catch (Exception e) {
            final String errorMessage = String.format("Error in FINotification-callback-processor error:%s", e.getMessage());
            log.error(errorMessage, e);
            throw Errors.InternalError.with(Objects.isNull(fiNotification) ?
                            UUID.randomUUID().toString() : fiNotification.getTxnid(),
                    errorMessage, e);
        }
    }

    private void handleConsentNotification(MessageConverter converter, Message message) {
        ConsentNotification consentNotification = null;
        try {
            consentNotification = (ConsentNotification) converter.fromMessage(message);
            callbackProcessor.handleConsentNotification(consentNotification);
        } catch (Exception e) {
            final String errorMessage = String.format("Error in ConsentNotification-callback-processor error:%s",
                    e.getMessage());
            log.error(errorMessage, e);
            throw Errors.InternalError.with(Objects.isNull(consentNotification) ?
                            UUID.randomUUID().toString() : consentNotification.getTxnid()
                    , errorMessage, e);
        }
    }
}
