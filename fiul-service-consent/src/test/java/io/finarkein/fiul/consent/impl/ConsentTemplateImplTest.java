/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.fiul.Functions;
import io.finarkein.fiul.consent.model.ConsentTemplate;
import io.finarkein.fiul.consent.repo.ConsentTemplateRepository;
import io.finarkein.fiul.consent.validators.ConsentTemplateValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;

import static io.finarkein.aa.fiu.consent.utils.ReadUtil.returnString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class ConsentTemplateImplTest {
    @InjectMocks
    private ConsentTemplateServiceImpl consentTemplateManagerImpl;

    @Mock
    private ConsentTemplateRepository consentTemplateRepository;

    @Mock
    private ConsentTemplateValidator consentTemplateValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private ConsentTemplate createConsentTemplate() {
        ConsentTemplate consentTemplate = null;
        try {
            String consentTemplateString = returnString("src/test/resources/consenttemplate/createConsentTemplate.txt");
            consentTemplate = objectMapper.readValue(consentTemplateString, ConsentTemplate.class);
            consentTemplate.setId(io.finarkein.api.aa.util.Functions.uuidSupplier.get());
            return consentTemplate;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return consentTemplate;
    }

    @Test
    @DisplayName("Save ConsentTemplate Test")
    void saveConsentTemplateTest() {
        var consentTemplate = createConsentTemplate();
        when(consentTemplateRepository.save(consentTemplate)).thenReturn(consentTemplate);
        doNothing().when(consentTemplateValidator).validateConsentTemplate(consentTemplate, Functions.UUIDSupplier.get());
        assertDoesNotThrow(() -> consentTemplateManagerImpl.saveConsentTemplate(consentTemplate));
    }

    @Test
    @DisplayName("Get ConsentTemplate Test")
    void getConsentTemplateTest() {
        final ConsentTemplate consentTemplate = createConsentTemplate();
        when(consentTemplateRepository.findById(consentTemplate.getId())).thenReturn(Optional.of(consentTemplate));

        ConsentTemplate returnedConsentTemplate = consentTemplateManagerImpl.getConsentTemplate(consentTemplate.getId()).get();
        Assertions.assertEquals(returnedConsentTemplate, consentTemplate);
    }

    @Test
    @DisplayName("Delete ConsentTemplate Test")
    void deleteConsentTemplateTest() {
        final String templateId = io.finarkein.api.aa.util.Functions.uuidSupplier.get();
        doNothing().when(consentTemplateRepository).deleteById(templateId);

        consentTemplateManagerImpl.deleteConsentTemplate(templateId);

        verify(consentTemplateRepository, times(1)).deleteById(templateId);
    }
}
