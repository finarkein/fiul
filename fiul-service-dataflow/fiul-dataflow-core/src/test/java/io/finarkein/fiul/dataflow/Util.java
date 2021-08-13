/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.file.Files;

public class Util {
    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T loadJsonFromFile(String fileName, Class<T> classType) {
        try {
            File file = new ClassPathResource(fileName).getFile();
            String content = new String(Files.readAllBytes(file.toPath()));
            return mapper.readValue(content, classType);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
