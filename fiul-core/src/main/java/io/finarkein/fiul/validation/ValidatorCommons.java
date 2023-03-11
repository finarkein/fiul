/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validation;

import io.finarkein.api.aa.exception.Errors;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.finarkein.aa.validators.ArgsValidator.checkNotEmpty;

@UtilityClass
public class ValidatorCommons {
    public static final Pattern STRING_NON_XSS_PATTERN = Pattern.compile("^[a-zA-Z0-9_.,@\\-\\s]*$");

    public static void requireNonXssCharsInString(String txnId, String value, String parameterName) {
        checkNotEmpty(txnId, value, parameterName);
        final Matcher matcher = STRING_NON_XSS_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw Errors.InvalidRequest.with(txnId, "Unsupported character(s) in '"+parameterName+"'");
        }
    }
}
