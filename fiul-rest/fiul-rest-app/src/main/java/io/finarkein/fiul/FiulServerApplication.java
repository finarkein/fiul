/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@Log4j2
@EntityScan(basePackages = {"io.finarkein.fiul", "io.finarkein.api.aa"})
@ComponentScan(basePackages = {"io.finarkein.fiul", "io.finarkein.api.aa","io.finarkein.aa"})
@SpringBootApplication(exclude = ReactiveUserDetailsServiceAutoConfiguration.class)
public class FiulServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FiulServerApplication.class, args);
    }
}
