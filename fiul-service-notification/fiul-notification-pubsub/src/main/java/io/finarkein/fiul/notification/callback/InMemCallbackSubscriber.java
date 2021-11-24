/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.notification.ConsentNotificationSubscriber;
import io.finarkein.fiul.notification.FINotificationSubscriber;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;

import static io.finarkein.fiul.notification.ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY;
import static io.finarkein.fiul.notification.config.CallbackWebClientConfig.WEBHOOK_QUALIFIER_SUPPLIER_METHOD;
import static io.finarkein.fiul.notification.config.CommonConfig.FIUL_EVENT_FACTORY;
import static io.finarkein.fiul.notification.config.ConfigForInMem.IN_MEM;

@Log4j2
@Component
@ConditionalOnProperty(name = NOTIFICATION_Q_TYPE_PROPERTY, havingValue = IN_MEM, matchIfMissing = true)
class InMemCallbackSubscriber extends AbstractCallbackSubscriber
        implements FINotificationSubscriber, ConsentNotificationSubscriber {

    @Autowired
    public InMemCallbackSubscriber(@Qualifier("notification") JmsTemplate jms,
                                   @Qualifier(WEBHOOK_QUALIFIER_SUPPLIER_METHOD)
                                           WebClient webClient,
                                   CallbackRegistry registry) {
        super(jms, webClient, registry);
    }

    @PostConstruct
    public void postConstruct() {
        log.info("(Consent+FI)NotificationSubscriber:{}, topics:[{},{}], queue-type:{} initialized",
                getClass().getSimpleName(), CONSENT_NOTIFICATION_CALLBACK_SUBSCRIBER, FI_NOTIFICATION_CALLBACK_SUBSCRIBER, IN_MEM);
    }

    @JmsListener(destination = CONSENT_NOTIFICATION_CALLBACK_SUBSCRIBER, containerFactory = FIUL_EVENT_FACTORY)
    public void handleConsentNotification(ConsentNotification notification) {
        doHandleConsentNotification(notification);
    }

    @JmsListener(destination = FI_NOTIFICATION_CALLBACK_SUBSCRIBER, containerFactory = FIUL_EVENT_FACTORY)
    public void handleFINotification(FINotification notification) {
        doHandleFINotification(notification);
    }
}