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
 * Implementation can be identified using builder-name<br>
 * Specify {@linkplain aa-client.crypto-service} property to plugin {@link CryptoServiceAdapter} for your CryptoService
 */
public interface CryptoServiceAdapterBuilder {
    CryptoServiceAdapter build(Properties properties);

    String adapterName();

    final class Registry {
        private static final Map<String, CryptoServiceAdapterBuilder> clientBuilders;

        static {
            clientBuilders = Functions.loadImplMap(CryptoServiceAdapterBuilder.class, CryptoServiceAdapterBuilder::adapterName);
        }

        public static CryptoServiceAdapterBuilder builderFor(String name) {
            return clientBuilders.get(name);
        }

    }
}
