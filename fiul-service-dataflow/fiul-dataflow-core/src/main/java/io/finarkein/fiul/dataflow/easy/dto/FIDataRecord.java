/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder(builderClassName = "Builder")
@IdClass(FIDataRecord.Key.class)
@Table(name = "FI_DATA_RECORD",
        indexes = {
                @Index(name = "FIDataRecord_Idx1", columnList = "dataLifeExpireOn"),
                @Index(name = "FIDataRecord_Idx2", columnList = "consentHandleId, sessionId"),
                @Index(name = "FIDataRecord_Idx3", columnList = "consentHandleId")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public final class FIDataRecord implements Serializable {
    @Id
    @Column(length = 36)
    private String consentId;
    @Id
    @Column(length = 36)
    private String sessionId;
    @Id
    @Column(length = 20)
    private String aaName;
    @Id
    @Column(length = 36)
    private String fipId;
    @Id
    @Column(length = 36)
    private String linkRefNumber;
    @Id
    @Column(length = 36)
    private String maskedAccNumber;

    @Column(length = 36, nullable = false, updatable = false)
    private String consentHandleId;

    @Column(columnDefinition = "BYTEA", nullable = false, updatable = false)
    private byte[] fiData;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false, updatable = false)
    private Timestamp dataLifeExpireOn;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false, updatable = false)
    private Timestamp createdOn;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        @Id
        @Column(length = 36)
        private String consentId;
        @Id
        @Column(length = 36)
        private String sessionId;
        @Id
        @Column(length = 20)
        private String aaName;
        @Id
        @Column(length = 36)
        private String fipId;
        @Id
        @Column(length = 36)
        private String linkRefNumber;
        @Id
        @Column(length = 36)
        private String maskedAccNumber;
    }
}
