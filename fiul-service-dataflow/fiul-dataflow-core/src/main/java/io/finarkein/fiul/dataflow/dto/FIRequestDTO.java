/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.dto;

import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.fiul.common.JSONAttrConverter;
import io.finarkein.fiul.ext.Callback;
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
@IdClass(FIRequestDTO.Key.class)
@Table(name = "FI_REQUEST",
        indexes = {
                @Index(name = "FIReq_Idx1", columnList = "sessionId, aaName")
        })
public class FIRequestDTO {
    @Id
    protected String consentId;

    @Id
    protected String sessionId;

    protected String consentHandleId;

    protected String version;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp timestamp;

    protected String txnId;

    protected String aaName;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp fiDataRangeFrom;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp fiDataRangeTo;

    @Column(columnDefinition="text")
    @Convert(converter = JSONAttrConverter.OfKeyMaterial.class)
    protected KeyMaterial keyMaterial;

    @Column(columnDefinition="text")
    @Convert(converter = JSONAttrConverter.OfCallback.class)
    protected Callback callback;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp createdOn;

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
