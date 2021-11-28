/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy;

import io.finarkein.api.aa.consent.DataLife;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.sql.Timestamp;

@Builder(builderClassName = "Builder", builderMethodName = "with")
@AllArgsConstructor
@Getter
public class DataSaveRequest<T> {
    private final String aaName;
    private final String consentHandleId;
    private final String sessionId;
    private final DataLife dataLife;
    private final Timestamp dataLifeExpireOn;
    private final T fiFetchResponse;

    public static <T> DataSaveRequest.Builder<T> with(T fiFetchResponse) {
        return new DataSaveRequest.Builder(fiFetchResponse);
    }

    public static class Builder<T> {
        private String aaName;
        private String consentId;
        private String sessionId;
        private DataLife dataLife;
        private Timestamp dataLifeExpireOn;
        private T fiFetchResponse;

        public Builder(T fiFetchResponse) {
            this.fiFetchResponse = fiFetchResponse;
        }

        public DataSaveRequest.Builder<T> aaName(@NonNull final String aaName) {
            this.aaName = aaName;
            return this;
        }

        public DataSaveRequest.Builder<T> consentId(@NonNull final String consentId) {
            this.consentId = consentId;
            return this;
        }

        public DataSaveRequest.Builder<T> sessionId(@NonNull final String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public DataSaveRequest.Builder<T> dataLife(@NonNull final DataLife dataLife) {
            this.dataLife = dataLife;
            return this;
        }

        public DataSaveRequest.Builder<T> dataLifeExpireOn(@NonNull final Timestamp dataLifeExpireOn) {
            this.dataLifeExpireOn = dataLifeExpireOn;
            return this;
        }

        public DataSaveRequest<T> build() {
            return new DataSaveRequest(this.aaName, this.consentId, this.sessionId, this.dataLife, dataLifeExpireOn, this.fiFetchResponse);
        }

        public String toString() {
            return "DataSaveRequest.Builder(aaName=" + this.aaName + ", consentId=" + this.consentId + ", sessionId=" + this.sessionId + ", dataLife=" + this.dataLife + ", fiFetchResponse=" + this.fiFetchResponse + ")";
        }
    }
}
