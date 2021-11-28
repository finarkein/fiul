/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SessionStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED,
    FAILED,
    READY;

    static final Map<String, SessionStatus> statuses = Arrays.stream(values())
            .collect(Collectors.toMap(Enum::name, Function.identity()));

    public static SessionStatus get(String value){
        final SessionStatus sessionStatus = statuses.get(value);
        if(sessionStatus == null)
            return statuses.get(value.toUpperCase());

        return sessionStatus;
    }
}
