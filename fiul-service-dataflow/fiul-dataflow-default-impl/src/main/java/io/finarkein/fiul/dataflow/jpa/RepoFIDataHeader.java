/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa;

import io.finarkein.fiul.dataflow.dto.FIDataHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;

@Repository
public interface RepoFIDataHeader extends JpaRepository<FIDataHeader, FIDataHeader.Key> {

    @Modifying(flushAutomatically = true)
    @Transactional
    int deleteByDataLifeExpireOnBefore(Timestamp expirationTime);

    @Modifying(flushAutomatically = true)
    @Transactional
    int deleteByConsentId(String consentId);

    @Modifying(flushAutomatically = true)
    @Transactional
    int deleteByConsentIdAndSessionId(String consentId, String sessionId);

    @Modifying(flushAutomatically = true)
    @Transactional
    int deleteBySessionId(String sessionId);
}
