/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import io.finarkein.fiul.common.ZippedBlobAttrConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "signed_consent")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class SignedConsentDTO implements Serializable {
    @Id
    protected String consentId;

    protected String consentMode;

    protected String dataLifeUnit;

    protected Integer dataLifeValue;

    protected Timestamp createTimestamp;

    @Convert(converter = ZippedBlobAttrConverter.OfString.class)
    @Column(columnDefinition = "BYTEA", nullable = false, updatable = false)
    protected String header;

    @Convert(converter = ZippedBlobAttrConverter.OfString.class)
    @Column(columnDefinition = "BYTEA", nullable = false, updatable = false)
    protected String payload;

    @Column(columnDefinition = "TEXT", nullable = false, updatable = false)
    protected String signature;
}
