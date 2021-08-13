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
public final class CipheredFIData {
    private final List<CipheredDataRecord> records;

    public static class Builder {
        private List<CipheredDataRecord> records;

        Builder() {
            records = new ArrayList<>();
        }

        public CipheredFIData.Builder addRecord(@NonNull final String fipId,
                                                @NonNull final String linkRefNumber,
                                                @NonNull final String maskedAccNumber,
                                                final byte[] fiData,
                                                @NonNull final byte[] encryptedDataKey) {
            this.records.add(CipheredDataRecord.builder()
                    .fipId(fipId)
                    .linkRefNumber(linkRefNumber)
                    .maskedAccNumber(maskedAccNumber)
                    .fiData(fiData)
                    .encryptedDataKey(encryptedDataKey)
                    .build());
            return this;
        }

        public CipheredFIData.Builder addRecord(@NonNull final CipheredDataRecord cipheredDataRecord) {
            this.records.add(cipheredDataRecord);
            return this;
        }

        public CipheredFIData build() {
            return new CipheredFIData(this.records);
        }

        public String toString() {
            return "CipheredFIData.Builder(records=" + this.records + ")";
        }
    }
}
