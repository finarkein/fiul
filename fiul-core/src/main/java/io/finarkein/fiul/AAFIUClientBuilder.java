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

/**
 * This pattern can be used to create FIU client with runtime properties(api-key, credentials, etc.)
 */
public interface AAFIUClientBuilder {
    AAFIUClient build(Properties javaProperties);
    String fiuClientName();

    final class Registry {
        private static final Map<String, AAFIUClientBuilder> clientBuilders;

        static {
            clientBuilders = Functions.loadImplMap(AAFIUClientBuilder.class, AAFIUClientBuilder::fiuClientName);
        }

        public static AAFIUClientBuilder builderFor(String name) {
            return clientBuilders.get(name);
        }
    }
}
