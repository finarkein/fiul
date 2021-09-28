/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.validators;

import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.consent.DataFilter;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.consent.model.ConsentTemplateDataRange;
import io.finarkein.fiul.consent.model.ConsentTemplateDefinition;
import io.finarkein.fiul.consent.model.DataRangeType;
import io.finarkein.fiul.validation.ConsentValidatorImpl;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class ConsentTemplateDefinitionValidatorImpl implements ConsentTemplateDefinitionValidator {

    ConsentValidatorImpl consentValidatorImpl = new ConsentValidatorImpl();

    private void nullValidations(ConsentTemplateDefinition consentTemplateDefinition, String txnId) {
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getConsentStartOffset(), "Consent Start Offset");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getConsentExpiryDuration(), "Consent Expiry Duration");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getConsentMode(), "Consent Mode");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getConsentTypes(), "Consent Types");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getFiTypes(), "Consent FiTypes");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getPurposeCode(), "Consent PurposeCode");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getFetchType(), "Consent FetchType");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getFrequency(), "Consent Frequency");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getDataLife(), "Consent Data Life");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getConsentTemplateDataRange(), "Consent Template DataRange");
        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getConsentTemplateDataRange().getDataRangeType(), "Consent Template DataRangeType");
//        ArgsValidator.checkNotEmpty(txnId, consentTemplateDefinition.getDataFilter(), "Consent Data Filter");
    }

    private void valueValidations(ConsentTemplateDefinition consentTemplateDefinition, String txnId) {
        consentValidatorImpl.validateConsentMode(txnId, consentTemplateDefinition.getConsentMode().toString());
        consentValidatorImpl.validateConsentTypes(txnId, consentTemplateDefinition.getConsentTypes());
        consentValidatorImpl.validateFITypes(txnId, consentTemplateDefinition.getFiTypes());
        consentValidatorImpl.validateFetchType(txnId, consentTemplateDefinition.getFetchType());
        if(consentTemplateDefinition.getDataFilter() != null)
            validateDataFilters(txnId, consentTemplateDefinition.getDataFilter());
        validateConsentTemplateDataRange(consentTemplateDefinition.getConsentTemplateDataRange(), txnId);
    }

    private void validateConsentTemplateDataRange(ConsentTemplateDataRange consentTemplateDataRange, String txnId) {
        if (DataRangeType.get(consentTemplateDataRange.getDataRangeType().toString()) == null)
            throw Errors.InvalidRequest.with(txnId, "Invalid DataRange Type");
        if (consentTemplateDataRange.getDataRangeType().equals(DataRangeType.FIXED) || consentTemplateDataRange.getDataRangeType().equals(DataRangeType.CONSENT_START_RELATIVE)) {
            ArgsValidator.checkNotEmpty(txnId, consentTemplateDataRange.getFrom(), "DataRange From");
            ArgsValidator.checkNotEmpty(txnId, consentTemplateDataRange.getTo(), "DataRange To");
            if (consentTemplateDataRange.getDataRangeType().equals(DataRangeType.FIXED)) {
                ArgsValidator.checkTimestamp(txnId, consentTemplateDataRange.getFrom(), "Consent Template FI DataRange Start");
                ArgsValidator.checkTimestamp(txnId, consentTemplateDataRange.getTo(), "Consent Template FI DataRange Expiry");
            }
        }
        else if (consentTemplateDataRange.getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR)) {
            ArgsValidator.checkNotEmpty(txnId, consentTemplateDataRange.getYear(), "DataRange Year");
            isNumber(txnId, consentTemplateDataRange.getYear());
            ArgsValidator.requirePositive(txnId, Integer.parseInt(consentTemplateDataRange.getYear()), "Template DataRange Year");
        }
    }

    private void isNumber(String txnId, String number) {
        try {
            Integer.parseInt(number);
        }catch (Exception e) {
            throw Errors.InvalidRequest.with(txnId, "FI DataRange Value is not a number");
        }
    }

    private void validateDataFilters(String txnId, List<DataFilter> dataFilterList) {
        dataFilterList.forEach(e -> {
            consentValidatorImpl.validateDataFilterType(txnId, e.getType());
            consentValidatorImpl.validateDataFilterOperator(txnId, e.getOperator());
        });
    }

    @Override
    public void validateConsentTemplateDefinition(ConsentTemplateDefinition consentTemplateDefinition, String txnId) {
        nullValidations(consentTemplateDefinition, txnId);
        valueValidations(consentTemplateDefinition, txnId);
    }
}
