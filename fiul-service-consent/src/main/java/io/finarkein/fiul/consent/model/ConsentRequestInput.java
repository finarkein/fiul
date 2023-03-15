/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import io.finarkein.fiul.dto.ConsentTemplateDefinition;
import io.finarkein.fiul.ext.Callback;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ConsentRequestInput {

    private String txnId;
    private String consentTemplateId;
    private String customerId;
    private Callback callback;

    protected List<Callback> webhooks;

    private String tenant;
    private String workspace;
    private String keyIdentifier;
    private ConsentTemplateDefinition consentTemplateDefinition;
}
