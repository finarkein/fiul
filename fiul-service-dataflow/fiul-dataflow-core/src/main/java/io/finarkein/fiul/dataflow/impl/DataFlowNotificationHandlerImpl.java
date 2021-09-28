/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.impl;

import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import io.finarkein.fiul.consent.ConsentState;
import io.finarkein.fiul.dataflow.DataFlowNotificationHandler;
import io.finarkein.fiul.dataflow.DataFlowService;
import io.finarkein.fiul.dataflow.EasyDataFlowService;
import io.finarkein.fiul.dataflow.store.FIRequestStore;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class DataFlowNotificationHandlerImpl implements DataFlowNotificationHandler {

    @Autowired
    DataFlowService dataFlowService;

    @Autowired
    EasyDataFlowService easyDataFlowService;

    @Autowired
    FIRequestStore fiRequestStore;

    @Override
    public void handleFINotification(FINotification fiNotification) {
        fiRequestStore.logNotificationAndUpdateState(fiNotification);
    }

    @Override
    public void handleConsentNotification(ConsentNotification consentNotification) {
        final var statusNotification = consentNotification.getConsentStatusNotification();
        final var consentStatus = statusNotification.getConsentStatus();
        final var consentState = ConsentState.get(consentStatus.toUpperCase());

        if (!consentState.isCleanupStatus())
            return;

        log.debug("Handling consentNotification:{}", consentNotification);
        if (consentState == ConsentState.REVOKED || consentState == ConsentState.EXPIRED) {
            easyDataFlowService.deleteData(statusNotification.getConsentId());
            dataFlowService.deleteDataByConsentId(statusNotification.getConsentId());
        }
    }
}
