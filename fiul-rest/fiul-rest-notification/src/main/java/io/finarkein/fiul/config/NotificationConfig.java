/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.config;

import io.finarkein.api.aa.jws.JWSSigner;
import io.finarkein.fiul.filter.NotificationJwsWebFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class NotificationConfig {

    public static final String NOTIFICATION_API_PATTERNS = "fiul.aa.notification.pathPatterns";

    @Bean(NOTIFICATION_API_PATTERNS)
    public List<PathPattern> notificationsPaths() {
        var paths = Arrays.asList(
                "/Consent/Notification",
                "/FI/Notification"
        );
        PathPatternParser parser = new PathPatternParser();
        parser.setCaseSensitive(false);
        parser.setMatchOptionalTrailingSeparator(false);
        return paths.stream().map(parser::parse).collect(Collectors.toList());
    }

    /**
     * Define a {@link org.springframework.web.server.WebFilter} for attaching body signature
     * to header `x-jws-signature`.
     *
     * @param signer {@link JWSSigner} service (defined in commons/aa-jws module)
     * @return instance configured to sign body
     */
    @Bean // Attach a web filter for server response header: x-jws-signature
    @ConditionalOnProperty(name = "fiul.notification.jws-filter-qualifier", havingValue = "fiul", matchIfMissing = true)
    public NotificationJwsWebFilter jwsFilter(JWSSigner signer, @Qualifier(NOTIFICATION_API_PATTERNS) List<PathPattern> pathPatterns) {
        return new NotificationJwsWebFilter(signer, pathPatterns);
    }

}
