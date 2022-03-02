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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@AllArgsConstructor
@Entity
@Builder
@NoArgsConstructor
@IdClass(FIDataHeader.Key.class)
@Table(name = "FI_DATA_HEADER",
        indexes = {
                @Index(name = "FIDataHeader_Idx1", columnList = "consentId, sessionId, aaName"),
                @Index(name = "FIDataHeader_Idx2", columnList = "aaName"),
                @Index(name = "FIDataHeader_Idx3", columnList = "dataLifeExpireOn"),
                @Index(name = "FIDataHeader_Idx4", columnList = "consentHandleId, sessionId, aaName"),
                @Index(name = "FIDataHeader_Idx5", columnList = "consentHandleId, sessionId"),
                @Index(name = "FIDataHeader_Idx6", columnList = "consentHandleId")
        }
)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class FIDataHeader {
    @Id
    @Column(length = 36)
    private String consentId;

    @Id
    @Column(length = 36)
    private String sessionId;

    @Column(length = 36)
    private String consentHandleId;

    @Column(nullable = false, updatable = false)
    private String aaName;

    private String version;
    private String txnId;

    @Column(nullable = false, updatable = false)
    private String dataLifeUnit;

    @Column(nullable = false, updatable = false)
    private Integer dataLifeValue;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false, updatable = false)
    private Timestamp dataLifeExpireOn;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
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
        private String consentId;
        @Id
        private String sessionId;
    }
}
