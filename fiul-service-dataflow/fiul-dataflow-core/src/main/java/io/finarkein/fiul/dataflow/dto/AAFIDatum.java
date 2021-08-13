/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.dto;

import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.fiul.common.ZippedBlobAttrConverter;
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
@IdClass(AAFIDatum.Key.class)
@Table(name = "FI_DATUM",
        indexes = {
                @Index(name = "AAFIDatum_Idx1", columnList = "dataLifeExpireOn")
        })
public class AAFIDatum {
    @Id
    private String consentId;
    @Id
    private String sessionId;
    @Id
    private String aaName;
    @Id
    private String fipId;
    @Id
    private String linkRefNumber;
    @Id
    private String maskedAccNumber;

    @Lob
    @Column(columnDefinition = "LONGBLOB", nullable = false, updatable = false)
    @Convert(converter = ZippedBlobAttrConverter.OfString.class)
    private String fiData;

    @Lob
    @Column(columnDefinition = "BLOB", nullable = false, updatable = false)
    @Convert(converter = ZippedBlobAttrConverter.OfKeyMaterial.class)
    private KeyMaterial keyMaterial;

    @Column(columnDefinition = "DATETIME(6)", nullable = false, updatable = false)
    private Timestamp dataLifeExpireOn;

    @Column(columnDefinition = "DATETIME(6)", nullable = false, updatable = false)
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
        @Id
        private String aaName;
        @Id
        private String fipId;
        @Id
        private String linkRefNumber;
        @Id
        private String maskedAccNumber;
    }
}
