/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa.easy;

import io.finarkein.fiul.dataflow.easy.dto.FIDataRecordDataKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;

@Repository
public interface RepoFIDataRecordDataKey extends JpaRepository<FIDataRecordDataKey, FIDataRecordDataKey.Key> {

    @Modifying(flushAutomatically = true)
    @Transactional
    int deleteByDataLifeExpireOnBefore(Timestamp expirationTime);

    @Modifying(flushAutomatically = true)
    @Transactional
    int deleteByConsentId(String consentId);

    @Modifying(flushAutomatically = true)
    @Transactional
    int deleteByConsentIdAndSessionId(String consentId, String sessionId);
}
