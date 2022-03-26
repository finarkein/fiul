/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import io.finarkein.api.aa.consent.ConsentMode;
import io.finarkein.api.aa.consent.DataFilter;
import io.finarkein.api.aa.consent.FetchType;
import io.finarkein.api.aa.consent.Purpose;
import io.finarkein.fiul.common.JSONAttrConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(indexes = {
        @Index(name = "crd_consentId_idx", columnList = "consentId"),
        @Index(name = "crd_aaName_idx", columnList = "aaName"),
        @Index(name = "crd_customerId_idx", columnList = "customerId"),
        @Index(name = "crd_consentStartDate_idx", columnList = "consentStartDate"),
        @Index(name = "crd_consentEndDate_idx", columnList = "consentEndDate")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequestDTO {

    @Id
    private String consentHandle;
    private String consentId;
    private String txnId;
    private String version;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp timestamp;
    
    private String aaName;
    private String customerId;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp consentStartDate;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp consentEndDate;

    private ConsentMode consentMode;
    private String consentTypes;
    private String fiTypes;
    private String dataConsumerId;

    @Convert(converter = JSONAttrConverter.OfTypePurpose.class)
    private Purpose purposeJson;

    private String dataLifeUnit;
    private int dataLifeValue;
    @Convert(converter = JSONAttrConverter.OfTypeDataFilterList.class)
    private List<DataFilter> dataFilterJson;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp fiDateRangeFrom;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp fiDateRangeTo;

    private FetchType fetchType;
    private String frequencyUnit;
    private int frequencyValue;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    protected Timestamp createdOn;

    @PrePersist
    protected void onCreate() {
        createdOn = Timestamp.from(Instant.now());
    }

}
