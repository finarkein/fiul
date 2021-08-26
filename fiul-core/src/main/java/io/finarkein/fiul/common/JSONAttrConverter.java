/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.consent.DataFilter;
import io.finarkein.api.aa.consent.Purpose;
import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.fiul.ext.Callback;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

public abstract class JSONAttrConverter<T> implements AttributeConverter<T, String> {
    protected static ObjectMapper mapper = new ObjectMapper();

    protected Class<T> type() {
        return null;
    }

    protected T readValue(String input) throws JsonProcessingException {
        return mapper.readValue(input, type());
    }

    @Override
    public String convertToDatabaseColumn(T object) {
        try {
            return (object != null) ? mapper.writeValueAsString(object) : null;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public T convertToEntityAttribute(String input) {
        try {
            return (input != null) ? readValue(input) : null;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Converter
    public static class OfKeyMaterial extends JSONAttrConverter<KeyMaterial> {

        @Override
        public Class<KeyMaterial> type() {
            return KeyMaterial.class;
        }
    }

    @Converter
    public static class OfCallback extends JSONAttrConverter<Callback> {

        @Override
        public Class<Callback> type() {
            return Callback.class;
        }
    }

    @Converter
    public static final class OfTypeConsentDetail extends JSONAttrConverter<ConsentDetail> {
        @Override
        public Class<ConsentDetail> type() {
            return ConsentDetail.class;
        }
    }

    @Converter
    public static final class OfTypePurpose extends JSONAttrConverter<Purpose> {
        @Override
        public Class<Purpose> type() {
            return Purpose.class;
        }
    }

    @Converter
    public static final class OfTypeDataFilterList extends JSONAttrConverter<List<DataFilter>> {
        @Override
        protected List<DataFilter> readValue(String input) throws JsonProcessingException {
            return mapper.readValue(input, new TypeReference<List<DataFilter>>() {
            });
        }
    }
}
