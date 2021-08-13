/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import io.finarkein.api.aa.crypto.CipherParameter;
import io.finarkein.api.aa.crypto.CipherResponse;
import io.finarkein.api.aa.crypto.SerializedKeyPair;

/**
 * Crypto service adapter which generate the KeyPair(private-key, key-material) required for data decryption and posting FIRequest<br>
 */
public interface CryptoServiceAdapter {
    SerializedKeyPair generateKey();

    CipherResponse decrypt(CipherParameter cipherParam);
}
