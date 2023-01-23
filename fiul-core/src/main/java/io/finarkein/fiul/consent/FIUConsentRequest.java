/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentRequest;
import io.finarkein.fiul.ext.Callback;
import lombok.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ver",
        "timestamp",
        "txnid",
        "ConsentDetail",
        "callback",
        "webhooks"
})
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class FIUConsentRequest extends ConsentRequest {

    // Custom fields.
    @JsonProperty("callback")
    protected Callback callback;

    protected List<Callback> webhooks;

    @Builder(builderClassName = "FIUConsentRequestBuilder",access = AccessLevel.PUBLIC, builderMethodName = "builder")
    public FIUConsentRequest(final String ver,
                             final String timestamp,
                             final String txnId,
                             final ConsentDetail consentDetail,
                             Callback callback,
                             List<Callback> webhooks) {
        super(ver, timestamp, txnId, consentDetail);
        this.callback = callback;
        this.webhooks = webhooks;
    }

    public ConsentRequest toAAConsentRequest() {
        return new ConsentRequest(ver, timestamp, txnid, consentDetail);
    }
}
