/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.validators;

import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.fiul.consent.model.ConsentRequestInput;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConsentRequestInputValidator {

    private static void nullValidations(ConsentRequestInput consentRequestInput) {
        ArgsValidator.checkNotEmpty(consentRequestInput.getTxnId(), consentRequestInput.getConsentTemplateId(), "Template Id");
        ArgsValidator.checkNotEmpty(consentRequestInput.getTxnId(), consentRequestInput.getCustomerId(), "Customer Id");
    }

    private static void valueValidations(ConsentRequestInput consentRequestInput) {
        ArgsValidator.validateTxnId(consentRequestInput.getTxnId());
    }

    public static void validateConsentRequestInput(ConsentRequestInput consentRequestInput) {
        nullValidations(consentRequestInput);
        valueValidations(consentRequestInput);
    }
}
