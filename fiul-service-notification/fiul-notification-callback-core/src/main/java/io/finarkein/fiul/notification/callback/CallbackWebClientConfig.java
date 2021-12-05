/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.notification.callback;

import io.finarkein.api.aa.webclient.headers.FixedHeaderExchangeFilter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import static io.finarkein.fiul.notification.callback.Constants.FIUL_CALLBACK_WEB_CLIENT_QUALIFIER;


@Configuration
@Log4j2
public class CallbackWebClientConfig {
    /**
     * Note: should match with method name mentioned below
     */
    public static final String WEBHOOK_QUALIFIER_SUPPLIER_METHOD = "webhookClientQualifier";

    private static final String DEFAULT_WEBCLIENT_NAME = "fiul-default";

    @Bean(DEFAULT_WEBCLIENT_NAME)
    @ConditionalOnProperty(name = FIUL_CALLBACK_WEB_CLIENT_QUALIFIER, havingValue = DEFAULT_WEBCLIENT_NAME, matchIfMissing = true)
    WebClient getWebClient(@Value("${fiul.notification.callback.request.header}") String header,
                               @Value("${fiul.notification.callback.request.value}") String value) {
        WebClient.Builder client = WebClient.builder();
        client.filter(new FixedHeaderExchangeFilter(header, value));
        log.info("Notification-callback webhook: webclient configured for qualifier:{}", DEFAULT_WEBCLIENT_NAME);
        return client.build();
    }

    @Bean(WEBHOOK_QUALIFIER_SUPPLIER_METHOD)
    public WebClient webhookClientQualifiers(@Value("${" + FIUL_CALLBACK_WEB_CLIENT_QUALIFIER + "}") String webclientName,
                                            ApplicationContext context) {
        log.info("Notification-callback webhook: qualifier: {}={}", FIUL_CALLBACK_WEB_CLIENT_QUALIFIER, webclientName);
        return (WebClient) context.getBean(webclientName);
    }
}
