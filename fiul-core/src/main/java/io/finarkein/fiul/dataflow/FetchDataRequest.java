/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.finarkein.api.aa.common.KeyMaterialConsumer;
import io.finarkein.api.aa.crypto.KeyMaterial;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ver",
        "from",
        "to",
        "consentHandleId",
        "KeyMaterial"
})
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchDataRequest implements KeyMaterialConsumer {
    @JsonProperty("ver")
    protected String ver;

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("consentHandleId")
    private String consentHandleId;

    @JsonProperty("KeyMaterial")
    private KeyMaterial keyMaterial;

}