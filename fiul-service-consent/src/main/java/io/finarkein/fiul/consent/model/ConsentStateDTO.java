/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.LastModifiedDate;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consent_state",
        indexes = {
                @Index(name = "cs_consentId_idx", columnList = "consentId"),
                @Index(name = "cs_consentStatus_idx", columnList = "consentStatus"),
                @Index(name = "cs_txnId_idx", columnList = "txnId")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ConsentStateDTO {

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
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp updatedOn;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp postConsentResponseTimestamp;

    @PrePersist
    protected void onCreate() {
        updatedOn = Timestamp.from(Instant.now());
    }
}
