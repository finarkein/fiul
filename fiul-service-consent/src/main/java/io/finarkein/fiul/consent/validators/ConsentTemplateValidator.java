/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.validators;

import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import io.finarkein.fiul.validation.ConsentTemplateDefinitionValidator;
import io.finarkein.fiul.validation.ValidatorCommons;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConsentTemplateValidator {
    public static void validateConsentTemplate(ConsentTemplate consentTemplate, String txnId) {
        nullValidations(consentTemplate, txnId);
        valueValidations(consentTemplate, txnId);
        ConsentTemplateDefinitionValidator.validateConsentTemplateDefinition(consentTemplate.getConsentTemplateDefinition(), txnId);
    }

    private static void nullValidations(ConsentTemplate consentTemplate, String txnId) {
        ArgsValidator.checkNotEmpty(txnId, consentTemplate.getConsentVersion(), "Consent Template Version");
        ArgsValidator.checkNotEmpty(txnId, consentTemplate.getDescription(), "Consent Template Description");
        if (consentTemplate.getDescription().length() > 100)
            throw Errors.InvalidRequest.with(txnId, "The description length must not exceed 100 characters");
        ValidatorCommons.requireNonXssCharsInString(txnId, consentTemplate.getDescription(), "description");

        ArgsValidator.checkNotEmpty(txnId, consentTemplate.getTags(), "Consent Template Tags");
        if (consentTemplate.getTags().length() > 100)
            throw Errors.InvalidRequest.with(txnId, "The tags length must not exceed 100 characters");
        ValidatorCommons.requireNonXssCharsInString(txnId, consentTemplate.getTags(), "tags");

        ArgsValidator.checkNotEmpty(txnId, consentTemplate.getConsentTemplateDefinition(), "Consent Template Consent Detail Template");
    }

    private static void valueValidations(ConsentTemplate consentTemplate, String txnId) {
        ArgsValidator.validateVersion(txnId, consentTemplate.getConsentVersion());
    }

}
