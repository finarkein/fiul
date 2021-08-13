/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.api.aa.model;

import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.fiul.common.RequestUpdater;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

class RequestUpdaterTest {
    private final RequestUpdater requestUpdaterAuto = new RequestUpdater("generateAuto", "generateAuto");
    private final RequestUpdater requestUpdaterNull = new RequestUpdater("generateIfNull", "generateIfNull");
    private final RequestUpdater requestUpdaterNoop = new RequestUpdater("noop", "noop");

    @Test
    @DisplayName("TxnId generator test")
    void txnIdUpdaterTest() {
        ConsentNotification consentNotification = new ConsentNotification();

        Assert.isNull(consentNotification.getTxnid(), "TxnId not null");
        requestUpdaterNull.updateTxnIdIfNeeded(consentNotification);
        Assert.notNull(consentNotification.getTxnid(), "TxnId not generated");
        requestUpdaterNull.updateTxnIdIfNeeded(consentNotification);

        consentNotification.setTxnid(null);

        Assert.isNull(consentNotification.getTxnid(), "TxnId not null");
        requestUpdaterAuto.updateTxnIdIfNeeded(consentNotification);
        Assert.notNull(consentNotification.getTxnid(), "TxnId not generated");

        consentNotification.setTxnid(null);

        Assert.isNull(consentNotification.getTxnid(), "TxnId not null");
        requestUpdaterNoop.updateTxnIdIfNeeded(consentNotification);
        Assert.isNull(consentNotification.getTxnid(), "TxnId generated");
    }

    @Test
    @DisplayName("Timestamp generator test")
    void timestampUpdaterTest() {
        ConsentDetail consentDetail = new ConsentDetail();

        Assert.isNull(consentDetail.getTimestamp(), "Timestamp not null");
        requestUpdaterNull.updateTimestampIfNeeded(consentDetail);
        Assert.notNull(consentDetail.getTimestamp(), "Timestamp not generated");
        requestUpdaterNull.updateTimestampIfNeeded(consentDetail);

        consentDetail.setTimestamp(null);

        Assert.isNull(consentDetail.getTimestamp(), "Timestamp not null");
        requestUpdaterAuto.updateTimestampIfNeeded(consentDetail);
        Assert.notNull(consentDetail.getTimestamp(), "Timestamp not generated");

        consentDetail.setTimestamp(null);

        Assert.isNull(consentDetail.getTimestamp(), "Timestamp not null");
        requestUpdaterNoop.updateTimestampIfNeeded(consentDetail);
        Assert.isNull(consentDetail.getTimestamp(), "Timestamp generated");
    }
}
