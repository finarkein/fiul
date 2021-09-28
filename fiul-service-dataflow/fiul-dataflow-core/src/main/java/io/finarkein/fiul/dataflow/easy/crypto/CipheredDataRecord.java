/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy.crypto;

import lombok.Builder;
import lombok.Getter;

@Builder(builderClassName = "Builder")
@Getter
public final class CipheredDataRecord {
    private final String fipId;
    private final String linkRefNumber;
    private final String maskedAccNumber;
    private final byte[] fiData;
    private final byte[] encryptedDataKey;
}
