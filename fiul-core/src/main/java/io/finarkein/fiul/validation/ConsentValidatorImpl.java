/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validation;

import io.finarkein.aa.fi.common.FITypeMeta;
import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.consent.*;
import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentRequest;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

import static io.finarkein.api.aa.exception.Errors.InvalidRequest;
import static io.finarkein.fiul.Functions.toTimestamp;

@NoArgsConstructor
public class ConsentValidatorImpl implements ConsentValidator {

    public void validateCreateConsent(ConsentRequest consentRequest) {
        nullValidation(consentRequest);
        validateCommonParameters(consentRequest);
        validateConsentDetail(consentRequest.getTxnid(), consentRequest.getConsentDetail());
    }

    public void validateStatus(String txnId, String status){
        ArgsValidator.validateStatus(txnId,status);
    }

    protected void nullValidation(ConsentRequest consent) {
        final String txnId = consent.getTxnid();
        ArgsValidator.checkNotEmpty(txnId, consent.getVer(), "ver");
        ArgsValidator.checkNotEmpty(txnId, txnId, "txnId");
        ArgsValidator.checkNotEmpty(txnId, consent.getTimestamp(), "timestamp");
        nullValidateConsentDetail(txnId, consent.getConsentDetail());
    }

    protected void nullValidateConsentDetail(String txnId, ConsentDetail consentDetail) {
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getConsentStart(), "Consent Start");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getConsentExpiry(), "Consent Expiry");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getConsentMode(), "Consent Mode");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getFetchType(), "Fetch type");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getConsentTypes(), "consentTypes");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getFiTypes(), "fiTypes");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getDataConsumer(), "DataConsumer");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getDataConsumer().getId(), "DataConsumerId");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getCustomer(), "Customer");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getCustomer().getId(), "CustomerId");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getPurpose(), "Purpose");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getPurpose().getCode(), "Purpose Code");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getPurpose().getRefUri(), "Purpose refUri");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getPurpose().getText(), "Purpose Text");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getPurpose().getCategory(), "Category");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getPurpose().getCategory().getType(), "Category Type");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getFIDataRange(), "FIDataRange");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getFIDataRange().getFrom(), "FIDataRange Start");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getFIDataRange().getTo(), "DataRange End");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getDataLife(), "DataLife");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getDataLife().getUnit(), "DataLife Unit");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getDataLife().getValue(), "DataLife Value");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getFrequency(), "Frequency");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getFrequency().getUnit(), "Frequency Unit");
        ArgsValidator.checkNotEmpty(txnId, consentDetail.getFrequency().getValue(), "Frequency Value");
    }

    protected void validateCommonParameters(ConsentRequest consent) {
        ArgsValidator.validateTxnId(consent.getTxnid());
        ArgsValidator.validateVersion(consent.getTxnid(), consent.getVer());
        ArgsValidator.requirePastTimestamp(consent.getTxnid(), consent.getTimestamp());
    }

    protected void validateConsentDetail(String txnId, ConsentDetail consentDetail) {
        validateConsentDate(txnId, consentDetail.getConsentStart(), consentDetail.getConsentExpiry());
        validateConsentMode(txnId, consentDetail.getConsentMode());
        validateFetchType(txnId, consentDetail.getFetchType());
        validateConsentTypes(txnId, consentDetail.getConsentTypes());
        validateFITypes(txnId, consentDetail.getFiTypes());
        validateDataLifeUnit(txnId, consentDetail.getDataLife().getUnit());
        validateFrequencyUnit(txnId, consentDetail.getFrequency().getUnit());
        if(consentDetail.getDataFilter() != null) {
            consentDetail.getDataFilter().forEach(e -> validateDataFilterType(txnId, e.getType()));
            consentDetail.getDataFilter().forEach(e -> validateDataFilterOperator(txnId, e.getOperator()));
        }
        ArgsValidator.validateDateRange(txnId, consentDetail.getFIDataRange().getFrom(), consentDetail.getFIDataRange().getTo());
        notNegative(txnId,consentDetail.getDataLife().getValue(),"DataLife Value");
        notNegative(txnId,consentDetail.getFrequency().getValue(),"Frequency Value");
        if (consentDetail.getDataFilter() != null) {
            for (DataFilter dataFilter : consentDetail.getDataFilter()) {
                notNegative(txnId, Integer.parseInt(dataFilter.getValue()), "Data Filter Value");
            }
        }
    }

    public void validateConsentMode(String txnId, String consentMode) {
        if (ConsentMode.get(consentMode) == null)
            throw InvalidRequest.with(txnId, "Invalid consent mode");
    }

    public void validateFetchType(String txnId, String fetchType) {
        if (FetchType.get(fetchType) == null)
            throw InvalidRequest.with(txnId, "Invalid Fetch type");
    }

    public void validateConsentTypes(String txnId, List<String> consentTypes) {
        if (consentTypes.stream().anyMatch(e -> ConsentType.get(e) == null))
            throw InvalidRequest.with(txnId, "Invalid consent types");
    }

    public void validateFITypes(String txnId, List<String> fiTypes) {
        if (fiTypes.stream().anyMatch(fiType -> !FITypeMeta.isFIType(fiType)))
            throw InvalidRequest.with(txnId, "Invalid FI type, supported types : " + FITypeMeta.getNames());
    }

    protected void validateDataConsumer(String txnId, String dataConsumer) {
        if (!dataConsumer.equals("AA"))
            throw InvalidRequest.with(txnId, "Invalid dataConsumer type");
    }

    public void validateDataFilterType(String txnId, String dataFilterType) {
        if (DataFilterType.get(dataFilterType) == null)
            throw InvalidRequest.with(txnId, "Invalid data filter type");
    }

    public void validateDataFilterOperator(String txnId, String operator) {
        if (!DataFilterOperator.operatorSet.contains(operator))
            throw InvalidRequest.with(txnId, "Invalid data filter operator");
    }

    public void validateDataLifeUnit(String txnId, String dataLifeUnit) {
        if ( DataLifeUnit.get(dataLifeUnit) == null)
            throw InvalidRequest.with(txnId, "Invalid DataLife Unit");
    }

    protected void validateFrequencyUnit(String txnId, String frequencyUnit) {
        if (FrequencyUnit.get(frequencyUnit) == null)
            throw InvalidRequest.with(txnId, "Invalid Frequency Unit");
    }

    protected void validateConsentDate(String txnId, String consentStart, String consentExpiry) {
        // TODO More research in date ranges allowed
        try {
            Timestamp startTimestamp = toTimestamp.apply(consentStart);
            Timestamp endTimestamp = toTimestamp.apply(consentExpiry);
            if (!startTimestamp.before(endTimestamp))
                throw InvalidRequest.with(txnId, "Invalid Consent Data Range");
        }catch (Exception e){
            throw InvalidRequest.with(txnId, "Invalid Consent Data Range", e);
        }
    }

    protected void notNegative(String txnId, int integer, String parameter) {
        if (integer < 0)
            throw InvalidRequest.with(txnId, parameter + " cannot be negative");
    }

}
