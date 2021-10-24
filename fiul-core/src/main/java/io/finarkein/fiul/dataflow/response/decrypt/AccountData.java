/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.response.decrypt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.finarkein.aa.fi.FIAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "linkRefNumber",
        "maskedAccNumber",
        "accountData"
})
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountData {
    @JsonProperty("linkRefNumber")
    protected String linkRefNumber;
    @JsonProperty("maskedAccNumber")
    protected String maskedAccNumber;
    @JsonProperty("accountData")
    protected FIAccount accountData;

    public AccountData(DecryptedDatum decryptedDatum){
        linkRefNumber = decryptedDatum.linkRefNumber;
        maskedAccNumber = decryptedDatum.maskedAccNumber;
    }
}
