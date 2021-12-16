/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.finarkein.api.aa.notification.FIStatusResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FIStatusResponseDTO {
    @JsonProperty("fipID")
    private String fipID;

    @EqualsAndHashCode.Exclude
    @JsonProperty("Accounts")
    private Set<FIStatusAccountDTO> accounts;

    public static FIStatusResponseDTO from(FIStatusResponse fiStatusResponse) {
        Set<FIStatusAccountDTO> accountDTOSet = null;

        if (fiStatusResponse.getAccounts() != null) {
            accountDTOSet = fiStatusResponse
                    .getAccounts()
                    .stream()
                    .map(FIStatusAccountDTO::from)
                    .collect(Collectors.toSet());
        }
        return new FIStatusResponseDTO(fiStatusResponse.getFipID(), accountDTOSet);
    }
}
