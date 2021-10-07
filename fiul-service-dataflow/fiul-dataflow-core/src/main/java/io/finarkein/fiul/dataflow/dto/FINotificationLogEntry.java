/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.dto;

import io.finarkein.api.aa.notification.FIStatusResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "FI_NOTIFICATION_LOG")
public class FINotificationLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FI_NOTIFICATION_LOG")
    @SequenceGenerator(name = "SEQ_FI_NOTIFICATION_LOG", sequenceName = "SEQ_FI_NOTIFICATION_LOG", allocationSize = 1)
    protected long id;

    protected String version;
    @Column(columnDefinition = "DATETIME(6)")
    protected Timestamp notificationTimestamp;

    protected String txnId;
    protected String sessionId;
    protected String sessionStatus;
    protected String notifierType;
    protected String notifierId;

    @Column(columnDefinition = "text")
    @Convert(converter = Converter.OfFIStatusResponseList.class)
    protected List<FIStatusResponse> fiStatusNotification;

    @Column(columnDefinition = "DATETIME(6)")
    protected Timestamp createdOn;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
    }
}
