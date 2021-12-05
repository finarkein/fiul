/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback.impl;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.notification.callback.CallbackProcessor;
import io.finarkein.fiul.notification.callback.CallbackRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import static io.finarkein.fiul.notification.callback.CallbackWebClientConfig.WEBHOOK_QUALIFIER_SUPPLIER_METHOD;

@Log4j2
@Component
class CallbackProcessorImpl extends AbstractCallbackProcessor implements CallbackProcessor {

    @Autowired
    public CallbackProcessorImpl(@Qualifier(WEBHOOK_QUALIFIER_SUPPLIER_METHOD) WebClient webClient,
                                 CallbackRegistry registry) {
        super(webClient, registry);
    }

    public void handleConsentNotification(ConsentNotification notification) {
        doHandleConsentNotification(notification);
    }

    public void handleFINotification(FINotification notification) {
        doHandleFINotification(notification);
    }
}