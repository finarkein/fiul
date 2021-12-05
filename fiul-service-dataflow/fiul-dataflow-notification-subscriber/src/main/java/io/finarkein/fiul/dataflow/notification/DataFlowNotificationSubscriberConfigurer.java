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

@Log4j2
@EnableJms
@Configuration
@ConditionalOnProperty(name = NOTIFICATION_Q_TYPE_PROPERTY, havingValue = IN_MEM, matchIfMissing = true)
public class DataFlowNotificationSubscriberConfigurer implements JmsListenerConfigurer {

    @Value("${fiul.notification.consent-queue-name}")
    private String consentNotificationTopicName;

    @Value("${fiul.notification.fi-queue-name}")
    private String fiNotificationTopicName;

    @Autowired
    private DataFlowConsentNotificationSubscriber dataFlowConsentNotificationSubscriber;

    @Autowired
    private DataFlowFINotificationSubscriber dataFlowFINotificationSubscriber;

    @Autowired
    @Qualifier("fiul-events-factory")
    private JmsListenerContainerFactory<?> listenerContainerFactory;

    @Autowired
    @Qualifier("JacksonMessageConverter")
    public MessageConverter jacksonJmsMessageConverter;

    @Value("${" + NOTIFICATION_Q_TYPE_PROPERTY + "}")
    private String queueType;

    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar jmsListenerEndpointRegistrar) {
        //consentNotification subscriber
        var jmsListenerEndpoint = dataflowServiceEndPointForConsentNotification(jacksonJmsMessageConverter);
        jmsListenerEndpointRegistrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);

        log.info("JmsListenerEndpoint registered with id:{}, destination:{}, queueType:{}",
                jmsListenerEndpoint.getId(), jmsListenerEndpoint.getDestination(), queueType);

        //fiNotification subscriber
        jmsListenerEndpoint = dataflowServiceEndPointForFINotification(jacksonJmsMessageConverter);
        jmsListenerEndpointRegistrar.registerEndpoint(jmsListenerEndpoint, listenerContainerFactory);

        log.info("JmsListenerEndpoint registered with id:{}, destination:{}, queueType:{}",
                jmsListenerEndpoint.getId(), jmsListenerEndpoint.getDestination(), queueType);
    }

    private SimpleJmsListenerEndpoint dataflowServiceEndPointForConsentNotification(MessageConverter converter) {
        final String dataflowServiceConsentNotificationListenerName = "Consumer.dataflow-service." + consentNotificationTopicName;
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId("dataflowServiceConsentNotificationListener_" + System.currentTimeMillis());
        endpoint.setDestination(dataflowServiceConsentNotificationListenerName);
        endpoint.setMessageListener(message -> {
            try {
                final ConsentNotification consentNotification = (ConsentNotification) converter.fromMessage(message);
                dataFlowConsentNotificationSubscriber.handleConsentNotification(consentNotification);
            } catch (Exception e) {
                final String errorMessage = String
                        .format("Error in ConsentNotification-dataflow-service-handler:%s, error:%s"
                                , dataflowServiceConsentNotificationListenerName, e.getMessage());
                log.error(errorMessage, e);
                throw Errors.InternalError.with(UUID.randomUUID().toString(), errorMessage, e);
            }
        });
        return endpoint;
    }

    private SimpleJmsListenerEndpoint dataflowServiceEndPointForFINotification(MessageConverter converter) {
        String dataflowServiceFINotificationListenerName = "Consumer.dataflow-service." + fiNotificationTopicName;
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId("dataflowServiceFINotificationListener_" + System.currentTimeMillis());
        endpoint.setDestination(dataflowServiceFINotificationListenerName);
        endpoint.setMessageListener(message -> {
            try {
                final FINotification consentNotification = (FINotification) converter.fromMessage(message);
                dataFlowFINotificationSubscriber.handleFINotification(consentNotification);
            } catch (Exception e) {
                final String errorMessage = String
                        .format("Error in FINotification-dataflow-service-handler:%s, error:%s"
                                , dataflowServiceFINotificationListenerName, e.getMessage());
                log.error(errorMessage, e);
                throw Errors.InternalError.with(UUID.randomUUID().toString(), errorMessage, e);
            }
        });
        return endpoint;
    }
}
