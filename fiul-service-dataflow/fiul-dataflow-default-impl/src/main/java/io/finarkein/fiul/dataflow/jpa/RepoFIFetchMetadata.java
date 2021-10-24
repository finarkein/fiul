/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa;

import io.finarkein.fiul.dataflow.dto.FIFetchMetadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepoFIFetchMetadata extends JpaRepository<FIFetchMetadata, String> {

    @Query("Select t from FIFetchMetadata t where" +
            " t.fiFetchCompletedOn IS NOT NULL" +
            " and t.sessionId = :sessionIdValue " +
            " and (t.fiDataRangeFrom >= :fiDataRangeFromValue and t.fiDataRangeTo <= :fiDataRangeToValue)")
    Optional<FIFetchMetadata> getCompletedFetchMetaData(@Param("sessionIdValue") String sessionIdValue,
                                                        @Param("fiDataRangeFromValue") Timestamp fiDataFromValue,
                                                        @Param("fiDataRangeToValue") Timestamp fiDataToValue);

    @Query("Select t from FIFetchMetadata t where" +
            " t.fiFetchCompletedOn IS NOT NULL" +
            " and t.sessionId = :sessionIdValue " +
            " and t.fipId = :fipIdValue " +
            " and t.linkRefNumbers like '%:linkRefNumbersValue%' " +
            " and (t.fiDataRangeFrom >= :fiDataRangeFromValue and t.fiDataRangeTo <= :fiDataRangeToValue)")
    Optional<FIFetchMetadata> getCompletedFetchMetaDataForFIP(@Param("sessionIdValue") String sessionIdValue,
                                                              @Param("fipIdValue") String fipIdValue,
                                                              @Param("linkRefNumbersValue") String linkRefNumbersValue,
                                                              @Param("fiDataRangeFromValue") Timestamp fiDataFromValue,
                                                              @Param("fiDataRangeToValue") Timestamp fiDataToValue);

    @Query("Select t from FIFetchMetadata t where t.sessionId = :sessionIdValue " +
            " and (t.fiDataRangeFrom >= :fiDataRangeFromValue and t.fiDataRangeTo <= :fiDataRangeToValue)")
    Optional<FIFetchMetadata> getFetchMetaData(@Param("sessionIdValue") String sessionIdValue,
                                               @Param("fiDataRangeFromValue") Timestamp fiDataFromValue,
                                               @Param("fiDataRangeToValue") Timestamp fiDataToValue);

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("Update FIFetchMetadata set fiFetchSubmittedOn = :fiFetchSubmittedOnValue, " +
            "fiFetchCompletedOn = :fiFetchCompletedOnValue, updatedOn = :updatedOnValue," +
            "fipId = :fipIdValue, linkRefNumbers = :linkRefNumberValue " +
            "where sessionId = :sessionIdValue")
    int updateStatus(@Param("sessionIdValue") String sessionIdValue,
                     @Param("fiFetchSubmittedOnValue") Timestamp fiFetchSubmittedOnValue,
                     @Param("fiFetchCompletedOnValue") Timestamp fiFetchCompletedOnValue,
                     @Param("updatedOnValue") Timestamp updatedOnValue,
                     @Param("fipIdValue") String fipIdValue,
                     @Param("linkRefNumberValue") String linkRefNumberValue);

    @Query("select t from FIFetchMetadata t where " +
            "t.fiFetchCompletedOn IS NOT NULL and " +
            "t.consentId = :consentIdValue and " +
            "t.fiDataRangeFrom <= :fiDataRangeFromValue and " +
            "t.fiDataRangeTo >= :fiDataRangeToValue and " +
            "t.easyDataFlow = :easyDataFlowValue " +
            "order by t.fiFetchCompletedOn desc")
    List<FIFetchMetadata> getMetadataForGivenWindow(@Param("consentIdValue") String consentIdValue,
                                                    @Param("fiDataRangeFromValue") Timestamp fiDataRangeFromValue,
                                                    @Param("fiDataRangeToValue") Timestamp fiDataRangeToValue,
                                                    @Param("easyDataFlowValue") boolean easyDataFlowValue,
                                                    Pageable pageable);

}
