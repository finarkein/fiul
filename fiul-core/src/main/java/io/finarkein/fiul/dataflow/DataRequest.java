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
import com.fasterxml.jackson.databind.JsonNode;
import io.finarkein.fiul.ext.Callback;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "customerAAId",
        "dataRangeFrom",
        "dataRangeTo",
        "consentHandle",
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

    @JsonProperty("dataRangeFrom")
    protected String dataRangeFrom;

    @JsonProperty("dataRangeTo")
    protected String dataRangeTo;

    @JsonProperty("consentHandle")
    protected String consentHandle;

    @JsonProperty("callback")
    protected Callback callback;

    protected DataRequest(DataRequest dataRequest){
        consentHandle = dataRequest.getConsentHandle();
        customerAAId = dataRequest.getCustomerAAId();
        dataRangeFrom = dataRequest.dataRangeFrom;
        dataRangeTo = dataRequest.dataRangeTo;
        consentHandle = dataRequest.getConsentHandle();
        callback = dataRequest.callback;
    }

    public static class Builder {
        private String customerAAId;
        private String dataRangeFrom;
        private String dataRangeTo;
        private String consentHandle;
        private Callback callback;

        Builder() {
        }

        @JsonProperty("customerAAId")
        public DataRequest.Builder customerAAId(@NonNull final String customerAAId) {
            this.customerAAId = customerAAId;
            return this;
        }

        @JsonProperty("dataRageFrom")
        public DataRequest.Builder dataRangeFrom(@NonNull final String dataRangeFrom) {
            this.dataRangeFrom = dataRangeFrom;
            return this;
        }

        @JsonProperty("dataRangeTo")
        public DataRequest.Builder dataRangeTo(@NonNull final String dataRangeTo) {
            this.dataRangeTo = dataRangeTo;
            return this;
        }

        @JsonProperty("consentHandle")
        public DataRequest.Builder consentHandle(@NonNull final String consentHandle) {
            this.consentHandle = consentHandle;
            return this;
        }

        @JsonProperty("callback")
        public DataRequest.Builder callback(final Callback callback) {
            this.callback = callback;
            return this;
        }

        @JsonProperty("callback")
        public DataRequest.Builder callbackURL(@NonNull final String url, final String runId, final JsonNode addOnParams) {
            this.callback = new Callback(url, runId, addOnParams);
            return this;
        }

        public DataRequest build() {
            return new DataRequest(this.customerAAId, this.dataRangeFrom, this.dataRangeTo, this.consentHandle, this.callback);
        }

        public String toString() {
            return "DataRequest.Builder(customerAAId=" + this.customerAAId + ", dataRangeFrom=" + this.dataRangeFrom + ", dataRangeTo=" + this.dataRangeTo + ", consentHandle=" + this.consentHandle + ", callback=" + this.callback + ")";
        }
    }
}
