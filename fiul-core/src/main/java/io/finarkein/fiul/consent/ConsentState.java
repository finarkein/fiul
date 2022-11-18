/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ConsentState {
    ACTIVE,
    PAUSED,
    EXPIRED,
    REVOKED,
    REJECTED,
    FAILED;

    private static final Map<String, ConsentState> lookup;

    //Populate the lookup table on loading time
    static {
        lookup = Arrays
                .stream(ConsentState.values())
                .sequential()
                .collect(Collectors.toMap(Enum::name, Function.identity()));
    }

    public static ConsentState get(String stateValue) {
        final var consentState = lookup.get(stateValue);
        if (consentState != null)
            return consentState;
        return lookup.get(stateValue.toUpperCase());
    }
}
