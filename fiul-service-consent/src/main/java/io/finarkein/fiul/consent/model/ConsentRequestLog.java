/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.fiul.common.JSONAttrConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "crl_aaId_idx", columnList = "aaId"),
        @Index(name = "crl_customerAAId_idx", columnList = "customerAAId"),
        @Index(name = "crl_errorOrigin_idx", columnList = "errorOrigin")
})
public class ConsentRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    private String version;
    private String txnId;

    @Column(columnDefinition = "DATETIME(6)")
    private Timestamp timestamp;
    private String aaId;
    private String customerAAId;

    @Column(columnDefinition = "LONGTEXT")
    @Convert(converter = JSONAttrConverter.OfTypeConsentDetail.class)
    private ConsentDetail consentDetail;

    @Column(columnDefinition = "DATETIME(6)")
    protected Timestamp createdOn;
    private String errorDetails;
    private ErrorOrigin errorOrigin;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
    }
}
