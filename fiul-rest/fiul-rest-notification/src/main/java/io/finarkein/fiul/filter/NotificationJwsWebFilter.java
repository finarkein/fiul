/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.filter;

import io.finarkein.api.aa.jws.JWSSigner;
import io.finarkein.api.aa.jws.response.JwsResponseWebFilter;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;

public class NotificationJwsWebFilter extends JwsResponseWebFilter {

    private final List<PathPattern> pathPatterns;

    public NotificationJwsWebFilter(JWSSigner signer, List<PathPattern> pathPatterns) {
        super(signer);
        this.pathPatterns = pathPatterns;
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        PathContainer requestPath = request.getPath().pathWithinApplication();
        for (PathPattern pattern : pathPatterns) {
            if (pattern.matches(requestPath)) { // if any path matches
                return super.filter(exchange, chain); // attach x-jws-signature header
            }
        }
        return chain.filter(exchange);
    }
}
