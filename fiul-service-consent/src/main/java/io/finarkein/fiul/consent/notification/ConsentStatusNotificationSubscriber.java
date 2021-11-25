/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.notification;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.service.ConsentService;
import io.finarkein.fiul.notification.ConsentNotificationSubscriber;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static io.finarkein.api.aa.util.Functions.strToTimeStamp;
import static io.finarkein.fiul.notification.ServiceConstants.CHANNEL_NAME_CONSENT;
import static io.finarkein.fiul.notification.ServiceConstants.NOTIFICATION_Q_TYPE_PROPERTY;
import static io.finarkein.fiul.notification.config.CommonConfig.FIUL_EVENT_FACTORY;

@Log4j2
@Component
public class ConsentStatusNotificationSubscriber implements ConsentNotificationSubscriber {
    public static final String CONSENT_NOTIFICATION_CONSENT_SERVICE_SUBSCRIBER = "Consumer.consent-service." + CHANNEL_NAME_CONSENT;

    @Value("${" + NOTIFICATION_Q_TYPE_PROPERTY + "}")
    private String queueType;

    protected final JmsTemplate jms;
    protected final ConsentService consentService;

    @Autowired
    public ConsentStatusNotificationSubscriber(@Qualifier("notification") JmsTemplate jms, ConsentService consentService) {
        this.jms = jms;
        this.consentService = consentService;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("ConsentNotificationSubscriber:{}, topic:{}, queue-type:{} initialized",
                getClass().getSimpleName(), CONSENT_NOTIFICATION_CONSENT_SERVICE_SUBSCRIBER, queueType);
    }

    @JmsListener(destination = CONSENT_NOTIFICATION_CONSENT_SERVICE_SUBSCRIBER, containerFactory = FIUL_EVENT_FACTORY)
    public void handleConsentNotification(ConsentNotification notification) {
        log.debug("notification received:{}", notification);
        final var consentStatusNotification = notification.getConsentStatusNotification();
        try {
            consentService.handleConsentNotification(ConsentNotificationLog.builder()
                    .version(notification.getVer())
                    .txnId(notification.getTxnid())
                    .notificationTimestamp(strToTimeStamp.apply(notification.getTimestamp()))
                    .notifierType(notification.getNotifier().getType())
                    .notifierId(notification.getNotifier().getId())
                    .consentHandle(consentStatusNotification.getConsentHandle())
                    .consentId(consentStatusNotification.getConsentId())
                    .consentState(consentStatusNotification.getConsentStatus())
                    .build());
            log.debug("ConsentNotification handling done, consentHandleId:{}, consentId:{}", consentStatusNotification.getConsentHandle(), consentStatusNotification.getConsentId());
        } catch (Exception e) {
            log.error("Error while persisting ConsentNotificationLog:{}", e.getMessage(), e);
        }
    }

}