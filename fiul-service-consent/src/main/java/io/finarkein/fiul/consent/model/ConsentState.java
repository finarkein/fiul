/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsentState {
    private String consentHandle;
    private String consentId;
    private String consentStatus;
    private Boolean isPostConsentSuccessful;
    private String aaHandle;
    private String notifierId;
    private Timestamp updatedOn;
    private Timestamp postConsentResponseTimestamp;

    public static ConsentState from(ConsentStateDTO consentStateDTO){
        return new ConsentState(consentStateDTO.getConsentHandle(),
                consentStateDTO.getConsentId(),
                consentStateDTO.getConsentStatus(),
                consentStateDTO.getIsPostConsentSuccessful(),
                consentStateDTO.getAaId(),
                consentStateDTO.getNotifierId(),
                consentStateDTO.getUpdatedOn(),
                consentStateDTO.getPostConsentResponseTimestamp());
    }
}
