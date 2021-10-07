/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import io.finarkein.api.aa.util.Functions;

import java.util.Map;
import java.util.Properties;

public interface RequestCacheServiceBuilder {
    CacheService build(Properties properties);

    String name();

    final class Registry {
        private static final Map<String, RequestCacheServiceBuilder> clientBuilders;

        static {
            clientBuilders = Functions.loadImplMap(RequestCacheServiceBuilder.class, RequestCacheServiceBuilder::name);
        }

        public static RequestCacheServiceBuilder builderFor(String name) {
            return clientBuilders.get(name);
        }
    }
}
