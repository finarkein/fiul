/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy.crypto;

import io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse;

public interface FIDataCryptoService {
    String SERVICE_NAME_PROPERTY = "fiul.dataflow.fi-data-crypto-service";

    CipheredFIData encrypt(FIFetchResponse fiFetchResponse);

    DecipheredFIData decrypt(CipheredFIData cipheredFIData);
}
