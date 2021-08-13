/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.config;

import io.finarkein.api.aa.jws.JWSSigner;
import io.finarkein.fiul.filter.NotificationJwsWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class NotificationConfig {

    /**
     * Define a {@link org.springframework.web.server.WebFilter} for attaching body signature
     * to header `x-jws-signature`.
     *
     * @param signer {@link JWSSigner} service (defined in commons/aa-jws module)
     * @return instance configured to sign body
     */
    @Bean // Attach a web filter for server response header: x-jws-signature
    public NotificationJwsWebFilter jwsFilter(JWSSigner signer) {
        List<String> paths = Arrays.asList(
                "/Consent/Notification",
                "/FI/Notification"
        );
        return new NotificationJwsWebFilter(signer, paths);
    }

}
