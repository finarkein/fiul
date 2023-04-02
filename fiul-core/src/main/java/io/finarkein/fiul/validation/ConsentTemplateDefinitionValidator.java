/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validation;

import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.consent.DataFilter;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.dto.ConsentTemplateDataRange;
import io.finarkein.fiul.dto.ConsentTemplateDefinition;
import io.finarkein.fiul.dto.DataRangeType;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ConsentTemplateDefinitionValidator {

    private static void nullValidations(ConsentTemplateDefinition consentTemplateDefinition, String txnId) {
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

    private static void valueValidations(ConsentTemplateDefinition consentTemplateDefinition, String txnId) {
        ConsentValidator.validateConsentMode(txnId, consentTemplateDefinition.getConsentMode().toString());
        ConsentValidator.validateConsentTypes(txnId, consentTemplateDefinition.getConsentTypes());
        ConsentValidator.validateFITypes(txnId, consentTemplateDefinition.getFiTypes());
        ConsentValidator.validateFetchType(txnId, consentTemplateDefinition.getFetchType());
        if (consentTemplateDefinition.getDataFilter() != null)
            validateDataFilters(txnId, consentTemplateDefinition.getDataFilter());
        validateConsentTemplateDataRange(consentTemplateDefinition.getConsentTemplateDataRange(), txnId);
    }

    private static void validateConsentTemplateDataRange(ConsentTemplateDataRange consentTemplateDataRange, String txnId) {
        if (DataRangeType.get(consentTemplateDataRange.getDataRangeType().toString()) == null)
            throw Errors.InvalidRequest.with(txnId, "Invalid DataRange Type");
        if (consentTemplateDataRange.getDataRangeType().equals(DataRangeType.FIXED) || consentTemplateDataRange.getDataRangeType().equals(DataRangeType.CONSENT_START_RELATIVE)) {
            ArgsValidator.checkNotEmpty(txnId, consentTemplateDataRange.getFrom(), "DataRange From");
            ArgsValidator.checkNotEmpty(txnId, consentTemplateDataRange.getTo(), "DataRange To");
            if (consentTemplateDataRange.getDataRangeType().equals(DataRangeType.FIXED)) {
                ArgsValidator.checkTimestamp(txnId, consentTemplateDataRange.getFrom(), "Consent Template FI DataRange Start");
                ArgsValidator.checkTimestamp(txnId, consentTemplateDataRange.getTo(), "Consent Template FI DataRange Expiry");
            }
        } else if (consentTemplateDataRange.getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR)) {
            ArgsValidator.checkNotEmpty(txnId, consentTemplateDataRange.getYear(), "DataRange Year");
            isNumber(txnId, consentTemplateDataRange.getYear());
            ArgsValidator.requirePositive(txnId, Integer.parseInt(consentTemplateDataRange.getYear()), "Template DataRange Year");
        }
    }

    private static void isNumber(String txnId, String number) {
        try {
            Integer.parseInt(number);
        } catch (Exception e) {
            throw Errors.InvalidRequest.with(txnId, "FI DataRange Value is not a number");
        }
    }

    private static void validateDataFilters(String txnId, List<DataFilter> dataFilterList) {
        dataFilterList.forEach(e -> {
            ConsentValidator.validateDataFilterType(txnId, e.getType());
            ConsentValidator.validateDataFilterOperator(txnId, e.getOperator());
        });
    }


    public static void validateConsentTemplateDefinition(ConsentTemplateDefinition consentTemplateDefinition, String txnId) {
        nullValidations(consentTemplateDefinition, txnId);
        valueValidations(consentTemplateDefinition, txnId);
    }
}