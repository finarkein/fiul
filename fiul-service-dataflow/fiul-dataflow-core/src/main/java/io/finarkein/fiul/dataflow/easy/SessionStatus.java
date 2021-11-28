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
    /**
     * I am on the job of trying to get you your data. Hang in there.
     */
    ACTIVE,
    /**
     * I have finished my job of trying to get you, your data.
     * I guarantee you that ALL the data you wanted is either READY or has been DENIED.
     * (As an AA - There is no failure in my ability to connect to an FIP and get a valid response.
     * Further if an FIP returns a failed session status to the AA,
     * the AA deems it as a failure for the overall session it has initiated for the FIU.)
     */
    COMPLETED,
    /**
     * I am done waiting for you to pick up your data. If you have not done it yet, tough luck.
     * You will have to raise a new job request if you want me to get you your data again.
     * I may charge you separately for this new job, as it is not my fault that you did not pick up your data in time.
     */
    EXPIRED,
    /**
     * Oops! Something went wrong at my end. At least one of the accounts for which you want data could not be returned to you,
     * owing to failure at either my end or some system downstream.
     * (if an AA - this would mean an FIP). Kindly raise a new job request.
     * Don’t worry - you will not be charged as it was my fault that I could not finish the job successfully the previous time.
     */
    FAILED;

    static final Map<String, SessionStatus> statuses = Arrays.stream(values())
            .collect(Collectors.toMap(Enum::name, Function.identity()));

    public static SessionStatus get(String value) {
        final SessionStatus sessionStatus = statuses.get(value);
        if (sessionStatus == null)
            return statuses.get(value.toUpperCase());

        return sessionStatus;
    }
}
