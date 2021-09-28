/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.api.aa.util.Functions;

import javax.persistence.AttributeConverter;
import java.nio.charset.StandardCharsets;

import static io.finarkein.api.aa.util.Functions.keyMaterialCompressor;
import static io.finarkein.api.aa.util.Functions.keyMaterialDeCompressor;

public abstract class ZippedBlobAttrConverter<T> implements AttributeConverter<T, byte[]> {
    static ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] convertToDatabaseColumn(T attribute) {
        return Functions.gzipCompression.apply(writeAsBytes(attribute));
    }

    protected abstract byte[] writeAsBytes(T object);
    protected abstract T readFromBytes(byte[] bytes);

    @Override
    public T convertToEntityAttribute(byte[] dbData) {
        return readFromBytes(Functions.gzipDecompression.apply(dbData));
    }

    public static class OfString extends ZippedBlobAttrConverter<String>{

        @Override
        protected byte[] writeAsBytes(String value) {
            return value.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        protected String readFromBytes(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static class OfKeyMaterial extends ZippedBlobAttrConverter<KeyMaterial>{

        @Override
        public byte[] convertToDatabaseColumn(KeyMaterial attribute) {
            return keyMaterialCompressor.apply(attribute);
        }

        @Override
        public KeyMaterial convertToEntityAttribute(byte[] dbData) {
            return keyMaterialDeCompressor.apply(dbData);
        }

        @Override
        protected byte[] writeAsBytes(KeyMaterial object) {
            return new byte[0];
        }

        @Override
        protected KeyMaterial readFromBytes(byte[] bytes) {
            return null;
        }
    }
}
