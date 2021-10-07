/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Version {
    public static final Set<String> versionSet = new HashSet<>(Arrays.asList("1.0", "1.1.2"));

    private Version() {}
}
