/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.api.aa.validators;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.consent.request.ConsentRequest;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.fiul.Functions;
import io.finarkein.fiul.validation.ConsentValidator;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConsentValidatorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String returnString(String fileName) throws IOException {
        return readFile(fileName);
    }

    @Test
    @DisplayName("Testing Files Read Stream")
    void testReadAsStream() throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream("/consenttestcases/NullVersion.txt")) {
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
            assertThrows(SystemException.class, () -> ConsentValidator.validateCreateConsent(objectMapper.readValue(e, ConsentRequest.class)));
        });
    }

    @ParameterizedTest(name = "{index} - {1}")
    @MethodSource("invalidArgumentProvider")
    @DisplayName("Invalid Consent Value Validators Test")
    void invalidValueTest(String filePath, String nameOfTest) throws IOException {
        String content = returnString(filePath);
        List<String> requestBodies = Arrays.asList(content.split("\\*"));
        requestBodies.forEach(e -> {
            assertThrows(SystemException.class, () -> ConsentValidator.validateCreateConsent(objectMapper.readValue(e, ConsentRequest.class)));
        });
    }

    @Test
    @DisplayName("ConsentState Validator Test")
    void consentStateValidator() {
        List<String> Status = new ArrayList<>();
        Status.addAll(Arrays.asList("ACTIVEs", "EXPIREd", "PAUSEd", "REVOKEd"));
        for (String status : Status) {
            String txnId = Functions.UUIDSupplier.get();
            assertThrows(SystemException.class, () -> ConsentValidator.validateStatus(txnId, status));
        }
    }

    private static Stream<Arguments> argumentProvider() {
        return Stream.of(
                Arguments.of("src/test/resources/consenttestcases/NullVersion.txt", "Null Version Cases"),
                Arguments.of("src/test/resources/consenttestcases/NullTxnId.txt", "Null Txn Id Cases"),
                Arguments.of("src/test/resources/consenttestcases/NullTimestamp.txt", "Null Time Stamp Cases"),
                Arguments.of("src/test/resources/consenttestcases/NullConsentDetail.txt", "Null Consent Detail Cases")
        );
    }

    private static Stream<Arguments> invalidArgumentProvider() {
        return Stream.of(
                Arguments.of("src/test/resources/consenttestcases/InvalidConsentCases.txt", "Invalid Consent Cases")
        );
    }
}
