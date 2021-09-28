/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.common;


import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Properties;

@Getter
@NoArgsConstructor
public class CommonConfig {
    protected String requestTimestampSetter;
    protected String requestTxnIdSetter;
    protected String cryptoServiceName;
    protected Properties properties;

    protected CommonConfig(Properties properties){
        requestTxnIdSetter = properties.getProperty("aa-client.request-txn-id-setter", "noop");
        requestTimestampSetter = properties.getProperty("aa-client.request-timestamp-setter", "noop");
        cryptoServiceName = properties.getProperty("aa-client.crypto-service");
        this.properties = properties;
    }
}
