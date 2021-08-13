/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.converter.xml;

import io.finarkein.aa.fi.common.FITypeMeta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XmlToBeanConverters {
    private static final Map<String, XmlToBeanConverter> XML_TO_BEAN_CONVERTER_MAP;

    static {
        final var converters = FITypeMeta
                .getVersionWiseFITypeMeta()
                .entrySet()
                .stream()
                .map(entry ->
                        new XmlToBeanConverter() {
                            @Override
                            public String name() {
                                return entry.getKey();
                            }

                            @Override
                            public Class<?> getType() {
                                return entry.getValue().classType();
                            }
                        }
                ).collect(Collectors.toMap(XmlToBeanConverter::name, v -> v));
        XML_TO_BEAN_CONVERTER_MAP = Collections.unmodifiableMap(converters);

    }

    public static XmlToBeanConverter getConverter(String name) {
        var xmlToBeanConverter = XML_TO_BEAN_CONVERTER_MAP.get(name);

        if (xmlToBeanConverter == null) {
            String replacedName = name.replace("_", "-");
            xmlToBeanConverter = XML_TO_BEAN_CONVERTER_MAP.get(replacedName);
            if (xmlToBeanConverter == null)
                throw new IllegalArgumentException(String.format("XmlToBean converter not found for name:'%s'", name));
        }

        return xmlToBeanConverter;
    }

    public static XmlToBeanConverter getConverter(String fiType, String version) {
        return getConverter(FITypeMeta.createKey(fiType, version));
    }
}
