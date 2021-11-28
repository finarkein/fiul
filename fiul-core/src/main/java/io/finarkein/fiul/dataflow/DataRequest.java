/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.finarkein.fiul.ext.Callback;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "customerAAId",
        "dataRageFrom",
        "dataRageTo",
        "consentHandleId",
        "callback"
})
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class DataRequest {

    @JsonProperty("customerAAId")
    protected String customerAAId;

    @JsonProperty("dataRageFrom")
    protected String dataRageFrom;

    @JsonProperty("dataRageTo")
    protected String dataRageTo;

    @JsonProperty("consentHandleId")
    protected String consentHandleId;

    @JsonProperty("callback")
    protected Callback callback;

    protected DataRequest(DataRequest dataRequest){
        consentHandleId = dataRequest.getConsentHandleId();
        customerAAId = dataRequest.getCustomerAAId();
        dataRageFrom = dataRequest.dataRageFrom;
        dataRageTo = dataRequest.dataRageTo;
        consentHandleId = dataRequest.getConsentHandleId();
        callback = dataRequest.callback;
    }

    public static class Builder {
        private String customerAAId;
        private String dataRageFrom;
        private String dataRageTo;
        private String consentHandleId;
        private Callback callback;

        Builder() {
        }

        @JsonProperty("customerAAId")
        public DataRequest.Builder customerAAId(@NonNull final String customerAAId) {
            this.customerAAId = customerAAId;
            return this;
        }

        @JsonProperty("dataRageFrom")
        public DataRequest.Builder dataRageFrom(@NonNull final String dataRageFrom) {
            this.dataRageFrom = dataRageFrom;
            return this;
        }

        @JsonProperty("dataRageTo")
        public DataRequest.Builder dataRageTo(@NonNull final String dataRageTo) {
            this.dataRageTo = dataRageTo;
            return this;
        }

        @JsonProperty("consentHandleId")
        public DataRequest.Builder consentHandleId(@NonNull final String consentHandleId) {
            this.consentHandleId = consentHandleId;
            return this;
        }

        @JsonProperty("callback")
        public DataRequest.Builder callback(final Callback callback) {
            this.callback = callback;
            return this;
        }

        @JsonProperty("callback")
        public DataRequest.Builder callbackURL(@NonNull final String url) {
            this.callback = new Callback(url);
            return this;
        }

        public DataRequest build() {
            return new DataRequest(this.customerAAId, this.dataRageFrom, this.dataRageTo, this.consentHandleId, this.callback);
        }

        public String toString() {
            return "DataRequest.Builder(customerAAId=" + this.customerAAId + ", dataRageFrom=" + this.dataRageFrom + ", dataRageTo=" + this.dataRageTo + ", consentHandleId=" + this.consentHandleId + ", callback=" + this.callback + ")";
        }
    }
}
