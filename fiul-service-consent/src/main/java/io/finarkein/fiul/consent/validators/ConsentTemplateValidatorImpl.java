/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.validators;

import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import org.springframework.stereotype.Component;

@Component
class ConsentTemplateValidatorImpl implements ConsentTemplateValidator {

    private final ConsentTemplateDefinitionValidator consentTemplateDefinitionValidator = new ConsentTemplateDefinitionValidatorImpl();

    private void nullValidations(ConsentTemplate consentTemplate, String txnId) {
        ArgsValidator.checkNotEmpty(txnId, consentTemplate.getConsentVersion(), "Consent Template Version");
        ArgsValidator.checkNotEmpty(txnId, consentTemplate.getDescription(), "Consent Template Description");
        ArgsValidator.checkNotEmpty(txnId, consentTemplate.getTags(), "Consent Template Tags");
        ArgsValidator.checkNotEmpty(txnId, consentTemplate.getConsentTemplateDefinition(), "Consent Template Consent Detail Template");
    }

    private void valueValidations(ConsentTemplate consentTemplate, String txnId) {
        ArgsValidator.validateVersion(txnId, consentTemplate.getConsentVersion());
    }

    @Override
    public void validateConsentTemplate(ConsentTemplate consentTemplate, String txnId) {
        nullValidations(consentTemplate, txnId);
        valueValidations(consentTemplate, txnId);
        consentTemplateDefinitionValidator.validateConsentTemplateDefinition(consentTemplate.getConsentTemplateDefinition(), txnId);
    }
}
