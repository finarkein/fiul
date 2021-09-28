/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeDuration {

    private static final Map<String, String> timeHashMap;
    static {
        timeHashMap = new HashMap<>();
        timeHashMap.put("i", "INF");
        timeHashMap.put("s", "SECOND");
        timeHashMap.put("m", "MINUTE");
        timeHashMap.put("h", "HOUR");
        timeHashMap.put("d", "DAY");
        timeHashMap.put("w", "WEEK");
        timeHashMap.put("M", "MONTH");
        timeHashMap.put("y", "YEAR");
    }

    public static final Map<String, String> TIME_DURATION_MAP = Collections.unmodifiableMap(timeHashMap);
}
