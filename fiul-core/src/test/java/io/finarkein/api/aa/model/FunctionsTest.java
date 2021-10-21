/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.api.aa.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.api.aa.service.crypto.CryptoService;
import io.finarkein.fiul.CryptoServiceAdapter;
import io.finarkein.fiul.CryptoServiceAdapterBuilder;
import io.finarkein.fiul.Functions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Properties;

import static io.finarkein.api.aa.util.Functions.timestampToSqlDate;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FunctionsTest {

    Properties properties = new Properties();
    private final CryptoService cryptoService = new CryptoService(properties);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SerializedKeyPair generatedKeyPair = cryptoService.generateKey();
    private final CryptoServiceAdapter crypto = CryptoServiceAdapterBuilder.Registry.builderFor("defaultCryptoService").build(properties);

    @Test
    void timestampToSqlDateTest() {
        String timestampString = null;
        Timestamp timestamp = timestampToSqlDate.apply(timestampString);
        Assert.isNull(timestamp, "Timestamp generated is not null");
        String timestampStringIllegal = "illegalTimestamp";
        assertThrows(IllegalArgumentException.class, () -> timestampToSqlDate.apply(timestampStringIllegal));

        timestampString = "2021-06-05T14:07:28.289Z";
        Assert.notNull(timestampToSqlDate.apply(timestampString), "Timestamp not generated");
    }

    @Test
    void toTimestampTest() {
        String timestampString = "IllegalTimestamp";
        assertThrows(IllegalArgumentException.class, () -> Functions.toTimestamp.apply(timestampString));
    }

    @Test
    void stringToDateTest() {
        String timestampString = null;
        Date date = Functions.stringToDate.apply(timestampString);
        Assert.isNull(date, "Date generated is not null");
        String timestampStringIllegal = "illegalTimestamp";
        assertThrows(IllegalArgumentException.class, () -> Functions.stringToDate.apply(timestampStringIllegal));

        timestampString = "2021-06-05T14:07:28.289Z";
        Assert.notNull(Functions.stringToDate.apply(timestampString), "Date not generated");
    }

    @Test
    void stringsTest() {
        Assert.isTrue(Functions.Strings.isBlank(null), "True not returned for null string");
        Assert.isTrue(Functions.Strings.isBlank(""), "True not returned for empty string");
    }

    @Test
    void getSignatureTest() {
        Assert.isNull(Functions.getSignature(null), "JWS signature not null");
        assertThrows(IllegalArgumentException.class, () -> Functions.getSignature("InvalidSignature"));
        Assert.notNull(Functions.getSignature("test.signature.value"), "Valid signature not returned");
    }

    @Test
    void prepareCipherParamTest() {
        Assert.notNull(Functions.prepareCipherParam(generatedKeyPair, "stringData", new KeyMaterial()), "Cipher generated is null");
    }
}
