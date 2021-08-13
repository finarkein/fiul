/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa.std;

import io.finarkein.fiul.dataflow.dto.AAFIDatum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface RepoAAFIDataStore extends JpaRepository<AAFIDatum, AAFIDatum.Key> {

    @Query("Select t from AAFIDatum t where" +
            " t.consentId = :consentIdValue" +
            " and t.sessionId = :sessionIdValue " +
            " and t.aaName = :aaNameValue " +
            " and t.fipId = :fipIdValue " +
            " and t.linkRefNumber IN :linkRefNumbers")
    List<AAFIDatum> getByLinkRefNumbers(
            @Param("consentIdValue") String consentIdValue,
            @Param("sessionIdValue") String sessionIdValue,
            @Param("aaNameValue") String aaName,
            @Param("fipIdValue") String fipId,
            @Param("linkRefNumbers") List<String> linkRefNumbers
    );

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
