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

    @Builder(builderClassName = "FIUFIRequestBuilder",access = AccessLevel.PUBLIC, builderMethodName = "builder")
    public FIUFIRequest(final String ver, final String timestamp, final String txnid, final FIDataRange fIDataRange,
                        final Consent consent, final KeyMaterial keyMaterial, Callback callback) {
        super(ver, timestamp, txnid, fIDataRange, consent, keyMaterial);
        this.callback = callback;
    }

    public FIRequest toAAFIRequest(){
        return new FIRequest(ver, timestamp, txnid, fIDataRange, consent, keyMaterial);
    }
}
