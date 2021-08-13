/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.finarkein.api.aa.notification.FIStatusNotification;
import io.finarkein.api.aa.notification.FIStatusResponse;
import io.finarkein.fiul.common.JSONAttrConverter;

import java.util.List;

/**
 *
 */
abstract class Converter {

    @javax.persistence.Converter
    public static class OfFIStatusNotification extends JSONAttrConverter<FIStatusNotification> {

        @Override
        public Class<FIStatusNotification> type() {
            return FIStatusNotification.class;
        }
    }

    @javax.persistence.Converter
    public static class OfFIStatusResponseList extends JSONAttrConverter<List<FIStatusResponse>> {

        protected List<FIStatusResponse> readValue(String input) throws JsonProcessingException {
            return mapper.readValue(input, new TypeReference<List<FIStatusResponse>>() {
            });
        }
    }
}
