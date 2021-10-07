/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.api.aa.cryptotest;

import io.finarkein.api.aa.crypto.CipherParameter;
import io.finarkein.api.aa.crypto.CipherResponse;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.api.aa.service.crypto.CryptoService;
import io.finarkein.fiul.CryptoServiceAdapter;
import io.finarkein.fiul.service.crypto.DefaultCryptoServiceAdapterBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.Properties;

class CryptoTest {

    Properties properties = new Properties();
    private final DefaultCryptoServiceAdapterBuilder defaultCryptoServiceAdapterBuilder = new DefaultCryptoServiceAdapterBuilder();
    private final CryptoServiceAdapter cryptoServiceAdapter = defaultCryptoServiceAdapterBuilder.build(properties);
    private final CryptoService cryptoService = new CryptoService(properties);

    @Test
    void generateKeyTest() {
        SerializedKeyPair serializedKeyPair = cryptoServiceAdapter.generateKey();
        Assert.notNull(serializedKeyPair, "Crypto Key pair not generated");
        Assert.notNull(serializedKeyPair.getPrivateKey(), "Private Key not generated");
        Assert.notNull(serializedKeyPair.getKeyMaterial(), "Key Material not generated");
        Assert.notNull(serializedKeyPair.getKeyMaterial().getCryptoAlg(), "Crypto Alg not generated");
        Assert.notNull(serializedKeyPair.getKeyMaterial().getCurve(), "Curve not generated");
        Assert.notNull(serializedKeyPair.getKeyMaterial().getDhPublicKey(), "DHPublic Key not generated");
        Assert.notNull(serializedKeyPair.getKeyMaterial().getDhPublicKey().getExpiry(), "DHPublic Key Expiry not generated");
        Assert.notNull(serializedKeyPair.getKeyMaterial().getDhPublicKey().getKeyValue(), "DHPublic Key Value not generated");
        Assert.notNull(serializedKeyPair.getKeyMaterial().getNonce(), "Private Key not generated");
    }

    @Test
    void encryptTest() {
        SerializedKeyPair serializedKeyPair = cryptoService.generateKey();
        CipherParameter cipherParameter = new CipherParameter();
        cipherParameter.setBase64Data("testcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadsfav");
        cipherParameter.setOurPrivateKey(serializedKeyPair.getPrivateKey());
        cipherParameter.setRemoteKeyMaterial(serializedKeyPair.getKeyMaterial());
        cipherParameter.setBase64RemoteNonce(serializedKeyPair.getKeyMaterial().getNonce());
        cipherParameter.setBase64YourNonce(serializedKeyPair.getKeyMaterial().getNonce());
        CipherResponse cipherResponse = cryptoService.encrypt(cipherParameter);
        Assert.notNull(cipherResponse, "Error created encrypted cipher");
    }

    @Test
    void decryptTest() {
        SerializedKeyPair serializedKeyPair = cryptoService.generateKey();
        CipherParameter cipherParameter = new CipherParameter();
        cipherParameter.setBase64Data("testcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadsfav");
        cipherParameter.setOurPrivateKey(serializedKeyPair.getPrivateKey());
        cipherParameter.setRemoteKeyMaterial(serializedKeyPair.getKeyMaterial());
        cipherParameter.setBase64RemoteNonce(serializedKeyPair.getKeyMaterial().getNonce());
        cipherParameter.setBase64YourNonce(serializedKeyPair.getKeyMaterial().getNonce());
        CipherResponse cipherResponse = cryptoService.encrypt(cipherParameter);
        cipherParameter.setBase64Data(cipherResponse.getBase64Data());
        CipherResponse decrypt = cryptoServiceAdapter.decrypt(cipherParameter);
        Assert.notNull(decrypt.getBase64Data(), "Error created encrypted cipher");
        Assertions.assertEquals("testcaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadsfav", decrypt.getBase64Data(), "Decrypted Data does not match");
    }
}
