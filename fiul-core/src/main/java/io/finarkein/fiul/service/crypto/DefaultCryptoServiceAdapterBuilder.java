/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.service.crypto;

import io.finarkein.fiul.CryptoServiceAdapter;
import io.finarkein.fiul.CryptoServiceAdapterBuilder;
import org.kohsuke.MetaInfServices;

import java.util.Properties;

import static io.finarkein.fiul.Functions.prefixFilter;

@MetaInfServices
public class DefaultCryptoServiceAdapterBuilder implements CryptoServiceAdapterBuilder {
    public static final String DEFAULT_CRYPTO_SERVICE = "defaultCryptoService";

    @Override
    public CryptoServiceAdapter build(Properties properties) {
        return new DefaultCryptoServiceAdapter(prefixFilter.apply(properties, "forwardsecrecy."));
    }

    @Override
    public String adapterName() {
        return DEFAULT_CRYPTO_SERVICE;
    }
}
