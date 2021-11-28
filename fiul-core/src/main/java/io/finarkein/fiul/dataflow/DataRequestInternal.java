/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import io.finarkein.api.aa.util.Functions;
import lombok.*;

import java.util.UUID;

@ToString
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class DataRequestInternal extends DataRequest {

    protected String txnId;
    protected String consentId;

    public DataRequestInternal(DataRequest dataRequest) {
        super(dataRequest);
        txnId = Functions.uuidSupplier.get();
    }
}
