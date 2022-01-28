/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.fiul.common.JSONAttrConverter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Converter;

public class ConsentJsonAttrConverter extends JSONAttrConverter {

    @Autowired
    protected ConsentJsonAttrConverter(ObjectMapper mapper) {
        super(mapper);
    }

    @Converter
    public static final class OfTypeConsentTemplateDefinition extends JSONAttrConverter<ConsentTemplateDefinition> {

        @Autowired
        protected OfTypeConsentTemplateDefinition(ObjectMapper mapper) {
            super(mapper);
        }

        @Override
        public Class<ConsentTemplateDefinition> type() {
            return ConsentTemplateDefinition.class;
        }
    }

}
