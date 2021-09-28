/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.common.FIDataRange;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import io.finarkein.fiul.consent.model.DataRangeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static io.finarkein.aa.fiu.consent.utils.ReadUtil.returnString;
import static io.finarkein.fiul.consent.impl.ConsentTemplateUtils.generateConsentDateRange;

class ConsentTemplateUtilsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Testing Files Read Stream")
    void testReadAsStream() throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream("/validatorstestcase/validConsentTemplateTestCases.txt")) {
            Assertions.assertNotNull(is);
        }
    }

    @Test
    @DisplayName("Duration Values Test")
    void testConsentGeneration() throws IOException {
        String content = returnString("src/test/resources/validatorstestcase/validConsentTemplateTestCases.txt");
        List<String> requestBodies = Arrays.asList(content.split("\\*"));

        requestBodies.forEach(e -> {
            ConsentTemplate consentTemplate;
            try {
                consentTemplate = objectMapper.readValue(e, ConsentTemplate.class);
                String consentStart = "2021-07-24T07:33:13.128Z";
                int currentYear = 2021;

                String[] strings = generateConsentDateRange(consentTemplate.getConsentTemplateDefinition().getConsentStartOffset(), consentTemplate.getConsentTemplateDefinition().getConsentExpiryDuration(), consentStart);
                FIDataRange fiDataRange = ConsentTemplateUtils.generateFIDataRange(consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange(), strings[0], strings[1], currentYear);
                if (consentTemplate.getConsentTemplateDefinition().getConsentStartOffset().equals("0M") && consentTemplate.getConsentTemplateDefinition().getConsentExpiryDuration().equals("1M")) {
                    Assertions.assertEquals("2021-07-24T07:33:13.128Z", strings[0]);
                    Assertions.assertEquals("2021-08-24T07:33:13.128Z", strings[1]);
                    if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getYear().equals("1")) {
                        Assertions.assertEquals("2020-04-01T00:00:00.000Z", fiDataRange.getFrom());
                        Assertions.assertEquals("2022-03-31T23:59:59.999Z", fiDataRange.getTo());
                    } else if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getYear().equals("0")) {
                        Assertions.assertEquals("2021-04-01T00:00:00.000Z", fiDataRange.getFrom());
                        Assertions.assertEquals("2022-03-31T23:59:59.999Z", fiDataRange.getTo());
                    } else if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.CONSENT_START_RELATIVE) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getFrom().equals("-1y") && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getTo().equals("-1M")) {
                        Assertions.assertEquals("2020-07-24T07:33:13.128", fiDataRange.getFrom());
                        Assertions.assertEquals("2021-06-24T07:33:13.128", fiDataRange.getTo());
                    }
                }
                if (consentTemplate.getConsentTemplateDefinition().getConsentStartOffset().equals("1M") && consentTemplate.getConsentTemplateDefinition().getConsentExpiryDuration().equals("1y")) {
                    Assertions.assertEquals("2021-08-24T07:33:13.128Z", strings[0]);
                    Assertions.assertEquals("2022-08-24T07:33:13.128Z", strings[1]);
                    if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getYear().equals("1")) {
                        Assertions.assertEquals("2020-04-01T00:00:00.000Z", fiDataRange.getFrom());
                        Assertions.assertEquals("2022-03-31T23:59:59.999Z", fiDataRange.getTo());
                    } else if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getYear().equals("0")) {
                        Assertions.assertEquals("2021-04-01T00:00:00.000Z", fiDataRange.getFrom());
                        Assertions.assertEquals("2022-03-31T23:59:59.999Z", fiDataRange.getTo());
                    } else if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.CONSENT_START_RELATIVE) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getFrom().equals("-1y") && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getTo().equals("-1M")) {
                        Assertions.assertEquals("2020-08-24T07:33:13.128", fiDataRange.getFrom());
                        Assertions.assertEquals("2021-07-24T07:33:13.128", fiDataRange.getTo());
                    }

                }
                if (consentTemplate.getConsentTemplateDefinition().getConsentStartOffset().equals("1w") && consentTemplate.getConsentTemplateDefinition().getConsentExpiryDuration().equals("1M")) {
                    Assertions.assertEquals("2021-07-31T07:33:13.128Z", strings[0]);
                    Assertions.assertEquals("2021-08-31T07:33:13.128Z", strings[1]);
                    if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getYear().equals("1")) {
                        Assertions.assertEquals("2020-04-01T00:00:00.000Z", fiDataRange.getFrom());
                        Assertions.assertEquals("2022-03-31T23:59:59.999Z", fiDataRange.getTo());
                    } else if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getYear().equals("0")) {
                        Assertions.assertEquals("2021-04-01T00:00:00.000Z", fiDataRange.getFrom());
                        Assertions.assertEquals("2022-03-31T23:59:59.999Z", fiDataRange.getTo());
                    } else if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.CONSENT_START_RELATIVE) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getFrom().equals("-1y") && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getTo().equals("-1M")) {
                        Assertions.assertEquals("2020-07-31T07:33:13.128", fiDataRange.getFrom());
                        Assertions.assertEquals("2021-06-30T07:33:13.128", fiDataRange.getTo());
                    }

                }
                if (consentTemplate.getConsentTemplateDefinition().getConsentStartOffset().equals("2M") && consentTemplate.getConsentTemplateDefinition().getConsentExpiryDuration().equals("2y")) {
                    Assertions.assertEquals("2021-09-24T07:33:13.128Z", strings[0]);
                    Assertions.assertEquals("2023-09-24T07:33:13.128Z", strings[1]);
                    if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getYear().equals("1")) {
                        Assertions.assertEquals("2020-04-01T00:00:00.000Z", fiDataRange.getFrom());
                        Assertions.assertEquals("2022-03-31T23:59:59.999Z", fiDataRange.getTo());
                    } else if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.FINANCIAL_YEAR) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getYear().equals("0")) {
                        Assertions.assertEquals("2021-04-01T00:00:00.000Z", fiDataRange.getFrom());
                        Assertions.assertEquals("2022-03-31T23:59:59.999Z", fiDataRange.getTo());
                    } else if (consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getDataRangeType().equals(DataRangeType.CONSENT_START_RELATIVE) && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getFrom().equals("-1y") && consentTemplate.getConsentTemplateDefinition().getConsentTemplateDataRange().getTo().equals("-1M")) {
                        Assertions.assertEquals("2020-09-24T07:33:13.128", fiDataRange.getFrom());
                        Assertions.assertEquals("2021-08-24T07:33:13.128", fiDataRange.getTo());
                    }
                }
            } catch (JsonProcessingException jsonProcessingException) {
                jsonProcessingException.printStackTrace();
            }
        });
    }
}
