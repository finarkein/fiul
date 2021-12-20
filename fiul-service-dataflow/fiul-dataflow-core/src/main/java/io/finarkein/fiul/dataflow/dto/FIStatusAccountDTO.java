/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.finarkein.api.aa.notification.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
@AllArgsConstructor
public class FIStatusAccountDTO {
    @JsonProperty("linkRefNumber")
    private String linkRefNumber;

    @EqualsAndHashCode.Exclude
    @JsonProperty("FIStatus")
    private String fiStatus;

    @EqualsAndHashCode.Exclude
    @JsonProperty("description")
    private String description;

    public static FIStatusAccountDTO from(Account account) {
        return new FIStatusAccountDTO(account.getLinkRefNumber(), account.getFiStatus(), account.getDescription());
    }
}
