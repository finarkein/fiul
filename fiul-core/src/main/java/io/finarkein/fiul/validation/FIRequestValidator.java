/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validation;

import io.finarkein.aa.validators.ArgsValidator;
import io.finarkein.api.aa.dataflow.FIRequest;

import static io.finarkein.api.aa.exception.Errors.InvalidRequest;
import static io.finarkein.fiul.Functions.UUIDSupplier;

public class FIRequestValidator extends CommonDataFlowValidator {

    public void validateFIRequestBody(FIRequest fiRequest) {
        final String txnId = fiRequest.getTxnid();
        if (txnId == null || txnId.isEmpty())
            throw InvalidRequest.with(UUIDSupplier.get(), "txnId cannot be null/empty");

        validateVersion(txnId, fiRequest.getVer());
        validateForNullsAndEmpty(fiRequest);
        validateTimestamp(txnId, fiRequest.getTimestamp());
        ArgsValidator.validateDateRange(txnId, fiRequest.getFIDataRange().getFrom(), fiRequest.getFIDataRange().getTo());

        ArgsValidator.checkTimestamp(txnId, fiRequest.getKeyMaterial().getDhPublicKey().getExpiry(), "KeyMaterial Expiry Date");
        validateAfterTimeStamp(txnId, fiRequest.getKeyMaterial().getDhPublicKey().getExpiry());
    }

    protected void validateForNullsAndEmpty(FIRequest fiRequest) {
        final String txnId = fiRequest.getTxnid();
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getTimestamp(), "T]timestamp");
        ArgsValidator.checkNotNull(txnId, fiRequest.getFIDataRange(), "FIDataRange");
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getFIDataRange().getFrom(), "FIDataRange start date");
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getFIDataRange().getTo(), "FIDataRange end date");

        ArgsValidator.checkNotNull(txnId, fiRequest.getConsent(), "Consent");
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getConsent().getId(), "Consent ID");
//        ArgsValidator.checkNotEmpty(fiRequest.getConsent().getDigitalSignature(), "DigitalSignature");

        ArgsValidator.checkNotNull(txnId, fiRequest.getKeyMaterial(), "KeyMaterial");
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getKeyMaterial().getCryptoAlg(), "KeyMaterial Crypto Alg");
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getKeyMaterial().getCurve(), "KeyMaterial Curve");
//        ArgsValidator.checkNotEmpty(fiRequest.getKeyMaterial().getParams(), "KeyMaterial Params");
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getKeyMaterial().getNonce(), "KeyMaterial Nonce");

        ArgsValidator.checkNotNull(txnId, fiRequest.getKeyMaterial().getDhPublicKey(), "DH Public Key");
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getKeyMaterial().getDhPublicKey().getExpiry(), "DH Public Key Expiry");
        ArgsValidator.checkNotEmpty(txnId, fiRequest.getKeyMaterial().getDhPublicKey().getKeyValue(), "DH Public Key Key Value");
//        ArgsValidator.checkNotEmpty(fiRequest.getKeyMaterial().getDhPublicKey().getParameters(), "DH Public Key Parameters");
    }
}
