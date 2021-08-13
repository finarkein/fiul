/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.aa;

import io.finarkein.api.aa.jws.JWSSigner;
import io.finarkein.fiul.AAFIUClient;
import lombok.NoArgsConstructor;

import java.util.Properties;

@NoArgsConstructor
public class FiulWebClientBuilder {

    public static final String NAME = "FIUWebClient";

    public AAFIUClient build(Properties javaProperties, JWSSigner util) {
        io.finarkein.fiul.common.Properties props = new io.finarkein.fiul.common.Properties(javaProperties);
        return new AAClientService(new FiulWebClientConfig(props), util);
    }
}
