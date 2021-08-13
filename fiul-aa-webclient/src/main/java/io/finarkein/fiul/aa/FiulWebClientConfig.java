/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.aa;

import io.finarkein.fiul.common.CommonConfig;
import io.finarkein.fiul.common.Properties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FiulWebClientConfig extends CommonConfig {

    private final String fiRequestCacheServiceName;

    private final Properties properties;

    public FiulWebClientConfig(Properties properties) {
        super(properties);
        fiRequestCacheServiceName = properties.getPropertyIgnoreCase("aa-client.request-cache-service-name", "in-mem");
        this.properties = properties;
    }
}
