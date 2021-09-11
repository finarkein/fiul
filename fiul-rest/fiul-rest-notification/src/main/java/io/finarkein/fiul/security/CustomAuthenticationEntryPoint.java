/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.exception.Error;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.FINotification;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Log4j2
@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -8970718410437077606L;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(ServerWebExchange serverWebExchange, AuthenticationException e) {
        //TODO add some logging
        try {
            return DataBufferUtils
                    .join(serverWebExchange.getRequest().getBody())
                    .map(dataBuffer -> {
                        ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                        byte[] byteArray = new byte[byteBuffer.remaining()];
                        byteBuffer.get(byteArray, 0, byteBuffer.remaining());
                        return byteArray;
                    }).map(bodyAsBytes -> {
                            try {
                                final String path = serverWebExchange.getRequest().getURI().getPath();
                                if(path.equalsIgnoreCase("/consent/notification"))
                                    return objectMapper.readValue(bodyAsBytes, ConsentNotification.class).getTxnid();
                                if(path.equalsIgnoreCase("/fi/notification"))
                                    return objectMapper.readValue(bodyAsBytes, FINotification.class).getTxnid();
                                return UUID.randomUUID().toString();
                            } catch (IOException ex) {
                                return UUID.randomUUID().toString();
                            }
                    }).map(txn -> {
                        Error responseErrorBean = new Error();
                        responseErrorBean.setErrorCode(Errors.Unauthorized.name());
                        responseErrorBean.setErrorMessage(e.getMessage());
                        responseErrorBean.setTimestamp(Timestamp.from(Instant.now()));
                        responseErrorBean.setVer(Error.VERSION);
                        responseErrorBean.setTxnId(txn);
                        return responseErrorBean;
                    }).flatMap(error -> {
                        final var response = serverWebExchange.getResponse();
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                        var dataBufferFactory = response.bufferFactory();
                        try {
                            var buffer = dataBufferFactory.wrap(objectMapper
                                    .writeValueAsString(error)
                                    .getBytes(Charset.defaultCharset()));

                            return response.writeWith(Mono.just(buffer))
                                    .doOnError(throwable -> DataBufferUtils.release(buffer))
                            ;
                        } catch (JsonProcessingException ex) {
                            return Mono.error(ex);
                        }
                    });
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
