/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ConsentTemplateDataRange implements Serializable {
    @JsonProperty("type")
    private DataRangeType dataRangeType;
    private String year;
    private String from;
    private String to;

    public ConsentTemplateDataRange(ConsentTemplateDataRange other) {

        this.dataRangeType = other.getDataRangeType();
        this.year = other.getYear();
        this.from = other.getFrom();
        this.to = other.getTo();
    }
}
