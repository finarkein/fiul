/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ConsentTemplate {

    @Id
    @GeneratedValue(generator = "UUIDConsentTemplate")
    @GenericGenerator(name = "UUIDConsentTemplate", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String description;
    private String tags;
    private String consentVersion;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = ConsentJsonAttrConverter.OfTypeConsentTemplateDefinition.class)
    private ConsentTemplateDefinition consentTemplateDefinition;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp createdOn;

    @LastModifiedDate
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp updatedOn;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
        updatedOn = createdOn;
    }
}
