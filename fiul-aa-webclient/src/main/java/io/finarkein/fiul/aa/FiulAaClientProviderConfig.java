/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.aa;

import io.finarkein.api.aa.AAClient;
import io.finarkein.fiul.AAFIUClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
@ConditionalOnProperty(name = "fiu.aa-webclient", havingValue = "fiul", matchIfMissing = true)
public class FiulAaClientProviderConfig {

    @Bean
    @ConditionalOnProperty(name = "fiu.aa-webclient", havingValue = "fiul", matchIfMissing = true)
    public AAFIUClient prepareAaWebclient(FiulWebClientConfig config, AAClient aaClient, CryptoServiceConfig cryptoServiceConfig) {
        log.info("Using fiu.aa-webclient=fiul");
        return new AAClientService(config, aaClient, cryptoServiceConfig);
    }
}
