/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder(builderClassName = "Builder")
@Table( name = "FI_FETCH_METADATA",
        indexes = {
                @Index(name = "FIFetchMD_Idx1", columnList = "sessionId, fiDataRangeFrom, fiDataRangeTo, fipId, " +
                        "linkRefNumbers, fiFetchSubmittedOn"),
                @Index(name = "FIFetchMD_Idx2", columnList = "sessionId, fiDataRangeFrom, fiDataRangeTo"),
                @Index(name = "FIFetchMD_Idx3", columnList = "sessionId, aaName"),
                @Index(name = "FIFetchMD_Idx4", columnList = "sessionId"),
                @Index(name = "FIFetchMD_Idx6", columnList = "consentId"),
                @Index(name = "FIFetchMD_Idx5", columnList = "fiFetchCompletedOn, consentHandleId, fiDataRangeFrom, fiDataRangeTo, easyDataFlow"),
        })
public class FIFetchMetadata {

    @Id
    @Column(length = 36)
    protected String sessionId;

    @Column(length = 36)
    protected String consentId;

    @Column(length = 36)
    protected String consentHandleId;

    @Column(length = 36)
    protected String txnId;
    @Column(length = 20)
    protected String aaName;

    @Column(columnDefinition = "boolean default false")
    protected boolean easyDataFlow;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp fiDataRangeFrom;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp fiDataRangeTo;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp fiRequestSubmittedOn;

    @Column(length = 36)
    protected String fipId;

    @Column(length = 36)
    protected String linkRefNumbers;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp fiFetchSubmittedOn;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp fiFetchCompletedOn;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp updatedOn;

    @PrePersist
    protected void onCreate() {
        updatedOn = Timestamp.from(Instant.now());
    }

    @PreUpdate
    protected void onUpdate() {
        updatedOn = Timestamp.from(Instant.now());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        @Id
        @Column(length = 36)
        String consentHandleId;
        @Id
        @Column(length = 36)
        String sessionId;
    }
}
