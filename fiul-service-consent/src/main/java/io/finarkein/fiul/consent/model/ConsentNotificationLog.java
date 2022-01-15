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

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "cs_notifn_log_idx2", columnList = "consentHandle")
})
public class ConsentNotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CONSENT_NOTIFICATION_LOG")
    @SequenceGenerator(name = "SEQ_CONSENT_NOTIFICATION_LOG", sequenceName = "SEQ_CONSENT_NOTIFICATION_LOG", allocationSize = 1)
    private long id;
    private String version;
    private String txnId;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp notificationTimestamp;

    private String notifierType;
    private String notifierId;
    private String consentHandle;
    private String consentId;
    private String consentState;
    private String aaId;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    protected Timestamp createdOn;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
    }
}
