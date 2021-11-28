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

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@IdClass(FIDataRecordDataKey.Key.class)
@Table(name = "FI_DATA_RECORD_KEY",
        indexes = {
                @Index(name = "FIDataRecordDK_Idx1", columnList = "dataLifeExpireOn")
        })
@EntityListeners(FIDataKeyEntityListener.class)
public final class FIDataRecordDataKey {
    @Id
    private String consentHandleId;
    @Id
    private String sessionId;
    @Id
    private String fipId;

    @Column(nullable = false, updatable = false)
    private byte[] encryptedDataKey;

    @Column(columnDefinition = "TIMESTAMP(6)", nullable = false, updatable = false)
    private Timestamp dataLifeExpireOn;

    @Column(columnDefinition = "TIMESTAMP(6)", nullable = false, updatable = false)
    private Timestamp createdOn;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Key implements Serializable {
        @Id
        private String consentHandleId;
        @Id
        private String sessionId;
        @Id
        private String fipId;
    }
}
