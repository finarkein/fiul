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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
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
    public AAFIUClient fiuClient(JWSSigner signer) throws IOException {
        Resource resource = new ClassPathResource("application.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        return new FiulWebClientBuilder().build(props, signer);
    }
}
