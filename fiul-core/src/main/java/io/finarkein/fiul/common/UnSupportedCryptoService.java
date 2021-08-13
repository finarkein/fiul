/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.common;

import io.finarkein.api.aa.crypto.CipherParameter;
import io.finarkein.api.aa.crypto.CipherResponse;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.fiul.CryptoServiceAdapter;

public enum UnSupportedCryptoService implements CryptoServiceAdapter {
    INSTANCE;

    @Override
    public SerializedKeyPair generateKey() {
        throw new UnsupportedOperationException("CryptoService is not configured");
    }

    @Override
    public CipherResponse decrypt(CipherParameter cipherParam) {
        throw new UnsupportedOperationException("CryptoService is not configured");
    }
}
