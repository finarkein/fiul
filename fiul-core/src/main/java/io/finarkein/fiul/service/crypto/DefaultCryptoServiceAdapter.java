/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.service.crypto;

import io.finarkein.api.aa.crypto.CipherParameter;
import io.finarkein.api.aa.crypto.CipherResponse;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.api.aa.service.crypto.CryptoService;
import io.finarkein.fiul.CryptoServiceAdapter;
import lombok.Data;

import java.util.Properties;

@Data
class DefaultCryptoServiceAdapter implements CryptoServiceAdapter {

    protected CryptoService cryptoService;

    DefaultCryptoServiceAdapter(Properties properties){
        cryptoService = new CryptoService(properties);
    }

    @Override
    public SerializedKeyPair generateKey() {
        return cryptoService.generateKey();
    }

    @Override
    public CipherResponse decrypt(CipherParameter cipherParam) {
        return cryptoService.decrypt(cipherParam);
    }
}
