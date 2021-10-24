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
        "fipId",
        "accounts"
})
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FIPData {
    @JsonProperty("fipId")
    private String fipId;
    @JsonProperty("accounts")
    private List<AccountData> accountData;

    public FIPData(DecryptedFI decryptedFIData) {
        fipId = decryptedFIData.getFipId();
    }
}
