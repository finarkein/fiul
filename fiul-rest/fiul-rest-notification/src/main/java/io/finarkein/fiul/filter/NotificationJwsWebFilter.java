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
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationJwsWebFilter extends JwsResponseWebFilter {

    List<PathPattern> applicablePaths;

    public NotificationJwsWebFilter(JWSSigner signer, List<String> paths) {
        super(signer);
        PathPatternParser parser = new PathPatternParser();
        parser.setCaseSensitive(false);
        parser.setMatchOptionalTrailingSeparator(false);
        applicablePaths = paths.stream().map(parser::parse).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        PathContainer requestPath = request.getPath().pathWithinApplication();
        for (PathPattern pattern : applicablePaths) {
            if (pattern.matches(requestPath)) { // if any path matches
                return super.filter(exchange, chain); // attach x-jws-signature header
            }
        }
        return chain.filter(exchange);
    }
}
