/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.aa;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FiulWebClientConfig {

    @Value("${aa-client.request-timestamp-setter:generateIfNull}")
    private String requestTimestampSetter;

    @Value("${aa-client.request-txn-id-setter:generateIfNull}")
    private String requestTxnIdSetter;

    @Value("${aa-client.crypto-service:defaultCryptoService}")
    private String cryptoServiceName;
}
