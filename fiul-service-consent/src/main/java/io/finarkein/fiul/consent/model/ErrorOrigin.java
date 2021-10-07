/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ErrorOrigin {
    ERROR_IN_VALIDATION,
    ERROR_FROM_AA;

    private static final Map<String, ErrorOrigin> lookup;

    static {
        lookup = Arrays
                .stream(ErrorOrigin.values())
                .sequential()
                .collect(Collectors.toMap(ErrorOrigin::name, Function.identity()));
    }

    public static ErrorOrigin get(String errorOrigin) {
        return lookup.get(errorOrigin);
    }
}
