/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.validators;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.fiul.Functions;
import io.finarkein.fiul.consent.model.ConsentRequestInput;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static io.finarkein.aa.fiu.consent.utils.ReadUtil.returnString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidatorsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConsentRequestInputValidator consentRequestInputValidator = new ConsentRequestInputValidatorImpl();

    private final ConsentTemplateValidator consentTemplateValidator = new ConsentTemplateValidatorImpl();

    @Test
    @DisplayName("Testing Files Read Stream")
    void testReadAsStream() throws IOException {
        try(InputStream is = this.getClass().getResourceAsStream("/validatorstestcase/invalidConsentRequestTestCases.txt")) {
            assertNotNull(is);
        }
    }

    @ParameterizedTest(name = "{index} - {1}")
    @MethodSource("consentRequestInputArgumentProvider")
    @DisplayName("Null Value Validators Test")
    void consentRequestInputValidatorTest(String filePath, String nameOfTest) throws IOException {
        String content = returnString(filePath);
        List<String> requestBodies = Arrays.asList(content.split("\\*"));
        requestBodies.forEach(e -> {
            assertThrows(SystemException.class, () -> consentRequestInputValidator.validateConsentRequestInput(objectMapper.readValue(e , ConsentRequestInput.class)));
        });
    }


    @ParameterizedTest(name = "{index} - {1}")
    @MethodSource("consentTemplateArgumentProvider")
    @DisplayName("Null Value Validators Test")
    void consentTemplateValidatorTest(String filePath, String nameOfTest) throws IOException {
        String content = returnString(filePath);
        List<String> requestBodies = Arrays.asList(content.split("\\*"));
        requestBodies.forEach(e -> {
            assertThrows(SystemException.class, () -> consentTemplateValidator.validateConsentTemplate(objectMapper.readValue(e , ConsentTemplate.class), Functions.UUIDSupplier.get()));
        });
    }

    private static Stream<Arguments> consentRequestInputArgumentProvider(){
        return Stream.of(
                Arguments.of("src/test/resources/validatorstestcase/invalidConsentRequestTestCases.txt","Invalid ConsentRequestInput Cases")
        );
    }

    private static Stream<Arguments> consentTemplateArgumentProvider(){
        return  Stream.of(
                Arguments.of("src/test/resources/validatorstestcase/invalidConsentTemplateTestCases.txt", "Invalid ConsentTemplate Cases")
        );
    }
}
