/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy.crypto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Builder(builderClassName = "Builder")
@Getter
public final class DecipheredFIData {
    private final List<DecipheredDataRecord> records;

    public static class Builder {
        private List<DecipheredDataRecord> records;

        Builder() {
            records = new ArrayList<>();
        }

        public DecipheredFIData.Builder addRecord(@NonNull final String fipId,
                                                @NonNull final String linkRefNumber,
                                                @NonNull final String maskedAccNumber,
                                                final String decryptedFIData) {
            this.records.add(DecipheredDataRecord.builder()
                    .fipId(fipId)
                    .linkRefNumber(linkRefNumber)
                    .maskedAccNumber(maskedAccNumber)
                    .data(decryptedFIData)
                    .build());
            return this;
        }

        public DecipheredFIData.Builder addRecord(@NonNull final DecipheredDataRecord encryptedDataRecord) {
            this.records.add(encryptedDataRecord);
            return this;
        }

        public DecipheredFIData build() {
            return new DecipheredFIData(this.records);
        }

        public String toString() {
            return "DecipheredFIData.Builder(records=" + this.records + ")";
        }
    }
}
