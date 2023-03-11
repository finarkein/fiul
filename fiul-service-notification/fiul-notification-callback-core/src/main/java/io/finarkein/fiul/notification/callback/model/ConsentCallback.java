/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
@TypeDef(name = "json", typeClass = JsonType.class)
@Table(indexes = {
        @Index(name = "cs_cb_idx1", columnList = "fiuId")
})
public class ConsentCallback {

    @Id
    protected String consentHandleId;
    protected String callbackUrl;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    protected Timestamp createdOn;

    @Type(type = "json")
    @Column(name = "add_on_params", columnDefinition = "jsonb")
    private JsonNode addOnParams;

    private String aaId;
    private String runId;

    protected String fiuId;

    @Column(columnDefinition = "boolean default false")
    private Boolean encrypt;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
    }
}
