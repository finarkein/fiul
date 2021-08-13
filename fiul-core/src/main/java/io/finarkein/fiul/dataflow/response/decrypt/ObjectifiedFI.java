/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.response.decrypt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@JsonPropertyOrder({
        "fipID",
        "objectifiedDatumList"
})
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectifiedFI {
    @JsonProperty("fipID")
    private String fipID;
    @JsonProperty("objectifiedDatumList")
    private List<ObjectifiedDatum> objectifiedDatumList;

    public ObjectifiedFI(DecryptedFI decryptedFIData) {
        fipID = decryptedFIData.getFipID();
    }
}
