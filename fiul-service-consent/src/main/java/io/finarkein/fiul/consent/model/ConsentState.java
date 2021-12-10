/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "cs_consentId_idx", columnList = "consentId"),
        @Index(name = "cs_consentStatus_idx", columnList = "consentStatus"),
        @Index(name = "cs_txnId_idx", columnList = "txnId")
})
public class ConsentState {

    @Id
    private String consentHandle;
    private String consentId;
    private String consentStatus;
    private String txnId;
    private Boolean isPostConsentSuccessful;
    private String aaId;
    private String customerAAId;
    private String notifierId;

    @LastModifiedDate
    @Column(columnDefinition = "TIMESTAMP(6)")
    private Timestamp updatedOn;

    @Column(columnDefinition = "TIMESTAMP(6)")
    private Timestamp postConsentResponseTimestamp;

    @PrePersist
    protected void onCreate() {
        updatedOn = Timestamp.from(Instant.now());
    }
}
