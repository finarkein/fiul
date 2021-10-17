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
@Table( name = "FI_REQUEST_STATE",
        indexes = {
                @Index(name = "FIReqState_Idx1", columnList = "sessionId, sessionStatus"),
                @Index(name = "FIReqState_Idx2", columnList = "sessionStatus")
        })
public class FIRequestState {

    @Id
    protected String sessionId;

    protected String notifierId;
    protected boolean fiRequestSuccessful;
    protected String sessionStatus;
    protected String txnId;
    protected String aaId;

    @Column(columnDefinition = "TIMESTAMP(6)")
    protected Timestamp notificationTimestamp;

    @Column(columnDefinition = "text")
    @Convert(converter = Converter.OfFIStatusResponseList.class)
    protected List<FIStatusResponse> fiStatusResponse;

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
}
