/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.finarkein.api.aa.consent.ConsentMode;
import io.finarkein.api.aa.consent.DataFilter;
import io.finarkein.fiul.ext.Callback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentTemplateDefinition implements Serializable {
    private String consentStartOffset;
    private String consentExpiryDuration;
    private ConsentMode consentMode;
    private List<String> consentTypes;
    private List<String> fiTypes;
    private String purposeCode;
    private String fetchType;
    private String frequency;
    private String dataLife;
    private ConsentTemplateDataRange consentTemplateDataRange;
    private List<DataFilter> dataFilter;
    private Callback callback;
}
