/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy.dto;

import lombok.extern.log4j.Log4j2;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

@Log4j2
public class FIDataKeyEntityListener {

    @PostLoad
    public void onLoad(FIDataRecordDataKey dataKey) {
        log.info("DataKey accessed for consentId:{}, sessionId:{}, fip:{}",
                dataKey.getConsentId(), dataKey.getSessionId(), dataKey.getFipId());
    }

    @PostPersist
    public void onPersist(FIDataRecordDataKey dataKey) {
        log.info("DataKey stored for consentId:{}, sessionId:{}, fip:{}",
                dataKey.getConsentId(), dataKey.getSessionId(), dataKey.getFipId());
    }

    @PostRemove
    public void onRemove(FIDataRecordDataKey dataKey) {
        log.info("DataKey removed for consentId:{}, sessionId:{}, fip:{}",
                dataKey.getConsentId(), dataKey.getSessionId(), dataKey.getFipId());
    }

    @PostUpdate
    public void onUpdate(FIDataRecordDataKey dataKey) {
        log.info("DataKey updated for consentId:{}, sessionId:{}, fip:{}",
                dataKey.getConsentId(), dataKey.getSessionId(), dataKey.getFipId());
    }
}
