/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.api.aa.validators;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.dataflow.FIRequest;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.fiul.validation.FIRequestValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FIRequestValidatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final FIRequestValidator fiRequestValidator = new FIRequestValidator();

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private static String returnString(String fileName) throws IOException {
        return readFile(fileName);
    }

    @Test
    @DisplayName("Testing Files Read Stream")
    void testReadAsStream() throws IOException {
        try(InputStream is = this.getClass().getResourceAsStream("/testcases/nullVersionCases.txt")) {
            assertNotNull(is);
        }
    }


    @ParameterizedTest(name = "{index} - {1}")
    @MethodSource("argumentProvider")
    @DisplayName("Null Value Validators Test")
    void nullValidatorTest(String filePath, String nameOfTest) throws IOException {
        String content = returnString(filePath);
        List<String> requestBodies = Arrays.asList(content.split("\\*"));
        requestBodies.forEach(e -> {
                assertThrows(SystemException.class, () -> fiRequestValidator.validateFIRequestBody(objectMapper.readValue(e , FIRequest.class)));
        });
    }

    @ParameterizedTest(name = "{index} - {1}")
    @MethodSource("invalidArgumentProvider")
    @DisplayName("Invalid Value Validators Test")
    void invalidValueTest(String filePath, String nameOfTest) throws IOException {
        String content = returnString(filePath);
        List<String> requestBodies = Arrays.asList(content.split("\\*"));
        requestBodies.forEach(e -> {
            assertThrows(SystemException.class, () -> fiRequestValidator.validateFIRequestBody(objectMapper.readValue(e , FIRequest.class)));
        });
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
                Arguments.of("src/test/resources/testcases/nullVersionCases.txt", "Null Version Cases"),
                Arguments.of("src/test/resources/testcases/nullTxnIdCases.txt", "Null TxnId Cases"),
                Arguments.of("src/test/resources/testcases/nullTimeStampCases.txt", "Null TimeStamp Cases"),
                Arguments.of("src/test/resources/testcases/nullKeyMaterial.txt", "Null KeyMaterial Cases"),
                Arguments.of("src/test/resources/testcases/nullFiDataRange.txt", "Null FI DataRange Cases"),
                Arguments.of("src/test/resources/testcases/nullConsentCases.txt", "Null Consent Cases")
        );
    }

    private static Stream<Arguments> invalidArgumentProvider() {
        return Stream.of(
                Arguments.of("src/test/resources/testcases/invalidCases.txt", "Invalid Version Cases")
        );
    }

}