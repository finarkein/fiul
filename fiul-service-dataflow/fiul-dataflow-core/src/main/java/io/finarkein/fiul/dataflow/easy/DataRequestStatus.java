/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.finarkein.fiul.dataflow.dto.FIStatusResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class DataRequestStatus {
    protected String aaHandle;
    protected String consentHandle;
    protected String sessionId;
    protected SessionStatus sessionStatus;
    protected Set<FIStatusResponseDTO> fiStatus;
    protected Timestamp requestSubmittedOn;
    protected Timestamp updatedOn;
}
