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
import io.finarkein.api.aa.common.FIDataRange;
import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.api.aa.dataflow.Consent;
import io.finarkein.api.aa.dataflow.FIRequest;
import io.finarkein.fiul.ext.Callback;
import lombok.*;

/**
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ver",
        "timestamp",
        "txnid",
        "FIDataRange",
        "Consent",
        "KeyMaterial",
        "callback"
})
@ToString
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FIUFIRequest extends FIRequest {

    @JsonProperty("callback")
    protected Callback callback;

    @JsonProperty("aaHandle")
    protected String aaHandle;

    FIUFIRequest(final String ver, final String timestamp, final String txnid, final FIDataRange fIDataRange,
                        final Consent consent, final KeyMaterial keyMaterial, Callback callback, String aaHandle) {
        super(ver, timestamp, txnid, fIDataRange, consent, keyMaterial);
        this.callback = callback;
        this.aaHandle = aaHandle;
    }

    public static Builder builder(){
        return new Builder();
    }

    public FIRequest toAAFIRequest(){
        return new FIRequest(ver, timestamp, txnid, fIDataRange, consent, keyMaterial);
    }

    public static class Builder {
        private String ver;
        private String timestamp;
        private String txnid;
        private FIDataRange fIDataRange;
        private Consent consent;
        private KeyMaterial keyMaterial;
        private Callback callback;
        private String aaHandle;

        Builder() {
        }

        public FIUFIRequest.Builder ver(final String ver) {
            this.ver = ver;
            return this;
        }

        public FIUFIRequest.Builder timestamp(final String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public FIUFIRequest.Builder txnid(final String txnid) {
            this.txnid = txnid;
            return this;
        }

        public FIUFIRequest.Builder fIDataRange(final FIDataRange fIDataRange) {
            this.fIDataRange = fIDataRange;
            return this;
        }

        public FIUFIRequest.Builder consent(final Consent consent) {
            this.consent = consent;
            return this;
        }

        public FIUFIRequest.Builder keyMaterial(final KeyMaterial keyMaterial) {
            this.keyMaterial = keyMaterial;
            return this;
        }

        public FIUFIRequest.Builder callback(final Callback callback) {
            this.callback = callback;
            return this;
        }

        public FIUFIRequest.Builder aaHandle(final String aaHandle) {
            this.aaHandle = aaHandle;
            return this;
        }

        public FIUFIRequest build() {
            return new FIUFIRequest(this.ver, this.timestamp, this.txnid, this.fIDataRange, this.consent,
                    this.keyMaterial, this.callback, aaHandle);
        }

        public String toString() {
            return "Builder(ver=" + this.ver + ", timestamp=" + this.timestamp + ", txnid=" + this.txnid
                    + ", fIDataRange=" + this.fIDataRange + ", consent=" + this.consent
                    + ", keyMaterial=" + this.keyMaterial + ", callback=" + this.callback
                    + ", aaHandle=" + this.aaHandle +")";
        }
    }
}
