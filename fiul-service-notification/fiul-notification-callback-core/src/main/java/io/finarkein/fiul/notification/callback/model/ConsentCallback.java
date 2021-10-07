/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
public class ConsentCallback {

    @Id
    protected String consentHandleId;
    protected String callbackUrl;

    @Column(columnDefinition = "DATETIME(6)")
    protected Timestamp createdOn;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
    }

}
