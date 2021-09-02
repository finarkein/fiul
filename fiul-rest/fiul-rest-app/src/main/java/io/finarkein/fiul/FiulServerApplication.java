/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import io.finarkein.api.aa.jws.JWSSigner;
import io.finarkein.fiul.aa.FiulWebClientBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Properties;

@Log4j2
@EntityScan(basePackages = {"io.finarkein.fiul", "io.finarkein.api.aa"})
@ComponentScan(basePackages = {"io.finarkein.fiul", "io.finarkein.api.aa"})
@SpringBootApplication
public class FiulServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiulServerApplication.class, args);
    }

    @Bean
    public AAFIUClient fiuClient(@Qualifier("aaClientProperties") Properties aaClientProperties,
                                 JWSSigner signer) {
        final var props = new Properties();
        aaClientProperties.entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith("aa-client") || entry.getKey().toString().startsWith("aa-properties"))
                .forEach(entry -> props.put(entry.getKey(), entry.getValue()));
        return new FiulWebClientBuilder().build(props, signer);
    }

    @Bean
    @ConfigurationProperties
    Properties aaClientProperties(){
        return new Properties();
    }
}
