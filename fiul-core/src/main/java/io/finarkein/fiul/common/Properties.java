/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.common;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extending to support caseInsensitive get of property
 */
public class Properties extends java.util.Properties {

    public Properties(java.util.Properties javaProperties) {
        this(javaProperties.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString())));
    }

    public Properties(Map<?, ?> properties) {
        putAll(properties);
    }

    public String getPropertyIgnoreCase(String key, String defaultValue) {
        if (containsKey(key))
            return getProperty(key, defaultValue);

        final String lowerCaseKey = key.toLowerCase();
        if (containsKey(lowerCaseKey))
            return getProperty(lowerCaseKey, defaultValue);
        return defaultValue;
    }

    public String getPropertyIgnoreCase(String key) {
        if (containsKey(key))
            return getProperty(key);

        final String lowerCaseKey = key.toLowerCase();
        return getProperty(lowerCaseKey);
    }

    public static Properties regexPrefixFilter(java.util.Properties inputProps, String prefix) {
        final Map<Object, Object> collect = inputProps.entrySet().stream()
                .filter(entry -> entry.getKey().toString().matches(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Properties(collect);
    }
}
