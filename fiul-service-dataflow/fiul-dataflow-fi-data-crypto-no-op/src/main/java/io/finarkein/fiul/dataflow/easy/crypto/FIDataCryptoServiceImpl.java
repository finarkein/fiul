/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy.crypto;

import io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static io.finarkein.fiul.dataflow.easy.crypto.FIDataCryptoService.SERVICE_NAME_PROPERTY;

/**
 * dummy implementation
 */
@Component
@ConditionalOnProperty(name = SERVICE_NAME_PROPERTY, havingValue = "no-op")
class FIDataCryptoServiceImpl implements FIDataCryptoService {

    @Override
    public CipheredFIData encrypt(FIFetchResponse fiFetchResponse) {
        final var builder = CipheredFIData.builder();

        fiFetchResponse.getDecryptedFI()
                .forEach(decryptedFI ->
                        decryptedFI
                                .getDecryptedDatum()
                                .stream()
                                .map(decryptedDatum -> CipheredDataRecord.builder()
                                        .fipId(decryptedFI.getFipID())
                                        .linkRefNumber(decryptedDatum.getLinkRefNumber())
                                        .maskedAccNumber(decryptedDatum.getMaskedAccNumber())
                                        .fiData(decryptedDatum.getDecryptedFI().getBytes(StandardCharsets.UTF_8))
                                        .encryptedDataKey("dummy".getBytes()).build())
                                .forEach(builder::addRecord)
                );
        return builder.build();
    }

    @Override
    public DecipheredFIData decrypt(CipheredFIData cipheredFIData) {
        final var builder = DecipheredFIData.builder();
        cipheredFIData
                .getRecords()
                .forEach(cipheredDataRecord -> builder.addRecord(DecipheredDataRecord
                        .builder()
                        .fipId(cipheredDataRecord.getFipId())
                        .linkRefNumber(cipheredDataRecord.getLinkRefNumber())
                        .maskedAccNumber(cipheredDataRecord.getMaskedAccNumber())
                        .data(new String(cipheredDataRecord.getFiData(), StandardCharsets.UTF_8))
                        .build()));
        return builder.build();
    }
}
