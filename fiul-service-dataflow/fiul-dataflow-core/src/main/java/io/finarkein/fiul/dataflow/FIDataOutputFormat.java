/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import io.finarkein.api.aa.exception.Errors;

import java.util.UUID;

public enum FIDataOutputFormat {
    json, xml;

    public static FIDataOutputFormat validateAndGetValue(String outputFormatAsString) {
        if (xml.name().equalsIgnoreCase(outputFormatAsString))
            return xml;
        if (json.name().equalsIgnoreCase(outputFormatAsString))
            return json;

        throw Errors.InvalidRequest.with(UUID.randomUUID().toString(), "Invalid value specified:" + outputFormatAsString);
    }
}
