/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.notification;

import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.notification.config.JmsUtil;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@EnableJms
@Configuration
@ConditionalOnProperty(name = NOTIFICATION_Q_TYPE_PROPERTY, havingValue = IN_MEM, matchIfMissing = true)
public class DataFlowNotificationSubscriberConfigurer implements JmsListenerConfigurer {

    protected final String consentNotificationTopicName;
    protected final String fiNotificationTopicName;
    protected final DataFlowConsentNotificationSubscriber dataFlowConsentNotificationSubscriber;
    protected final DataFlowFINotificationSubscriber dataFlowFINotificationSubscriber;
    protected final JmsListenerContainerFactory<?> listenerContainerFactory;
    protected final MessageConverter jacksonJmsMessageConverter;
    protected final String queueType;

    protected DataFlowNotificationSubscriberConfigurer(@Value("${fiul.notification.consent-queue-name}") String consentNotificationTopicName,
                                                       @Value("${fiul.notification.fi-queue-name}") String fiNotificationTopicName,
                                                       DataFlowConsentNotificationSubscriber dataFlowConsentNotificationSubscriber,
                                                       DataFlowFINotificationSubscriber dataFlowFINotificationSubscriber,
                                                       @Qualifier("fiul-events-factory") JmsListenerContainerFactory<?> listenerContainerFactory,
                                                       @Qualifier("JacksonMessageConverter") MessageConverter jacksonJmsMessageConverter,
                                                       @Value("${" + NOTIFICATION_Q_TYPE_PROPERTY + "}") String queueType) {
        this.consentNotificationTopicName = consentNotificationTopicName;
        this.fiNotificationTopicName = fiNotificationTopicName;
        this.dataFlowConsentNotificationSubscriber = dataFlowConsentNotificationSubscriber;
        this.dataFlowFINotificationSubscriber = dataFlowFINotificationSubscriber;
        this.listenerContainerFactory = listenerContainerFactory;
        this.jacksonJmsMessageConverter = jacksonJmsMessageConverter;
        this.queueType = queueType;
    }

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar jmsListenerEndpointRegistrar) {
        //consentNotification subscriber
        SimpleJmsListenerEndpoint jmsListenerEndpoint = JmsUtil.createSimpleEndpoint(
                "Consumer.dataflow-service." + consentNotificationTopicName,
                message -> processConsentNotification(jacksonJmsMessageConverter, message)
        );
        jmsListenerEndpointRegistrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);

        log.info("ConsentNotification JmsListenerEndpoint registered:{}, queueType:{}", jmsListenerEndpoint, queueType);

        //fiNotification subscriber
        jmsListenerEndpoint = JmsUtil.createSimpleEndpoint(
                "Consumer.dataflow-service." + fiNotificationTopicName,
                message -> processFINotification(jacksonJmsMessageConverter, message)
        );
        jmsListenerEndpointRegistrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);

        log.info("FINotification JmsListenerEndpoint registered:{}, queueType:{}", jmsListenerEndpoint, queueType);
    }

    protected void processConsentNotification(MessageConverter converter, Message message) {
        ConsentNotification consentNotification = null;
        try {
            consentNotification = (ConsentNotification) converter.fromMessage(message);
            dataFlowConsentNotificationSubscriber.handleConsentNotification(consentNotification);
        } catch (Exception e) {
            final String errorMessage = String
                    .format("Error in ConsentNotification-dataflow-service-handler error:%s", e.getMessage());
            log.error(errorMessage, e);
            throw Errors.InternalError.with(Objects.isNull(consentNotification) ?
                    UUID.randomUUID().toString() : consentNotification.getTxnid(), errorMessage, e);
        }
    }

    protected void processFINotification(MessageConverter converter, Message message) {
        FINotification fiNotification = null;
        try {
            fiNotification = (FINotification) converter.fromMessage(message);
            dataFlowFINotificationSubscriber.handleFINotification(fiNotification);
        } catch (Exception e) {
            final String errorMessage = String
                    .format("Error in FINotification-dataflow-service-handler error:%s", e.getMessage());
            log.error(errorMessage, e);
            throw Errors.InternalError.with(Objects.isNull(fiNotification) ?
                    UUID.randomUUID().toString() : fiNotification.getTxnid(), errorMessage, e);
        }
    }
}
