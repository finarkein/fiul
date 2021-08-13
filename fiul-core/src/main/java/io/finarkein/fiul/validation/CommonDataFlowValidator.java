/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.validation;

import io.finarkein.fiul.common.Version;

import java.sql.Timestamp;
import java.time.Instant;

import static io.finarkein.api.aa.exception.Errors.InvalidRequest;
import static io.finarkein.fiul.Functions.timestampToSqlDate;

public class CommonDataFlowValidator {

    public CommonDataFlowValidator() {
    }

    protected void validateVersion(String txnId, String version) {
        if (!Version.versionSet.contains(version))
            throw InvalidRequest.with(txnId, "Version not supported : " + version);
    }

    protected void validateTimestamp(String txnId, String timestamp) {
        Timestamp consentTimestamp = timestampToSqlDate.apply(timestamp);
        if (!consentTimestamp.before(Timestamp.from(Instant.now())))
            throw InvalidRequest.with(txnId, "Timestamp invalid : " + timestamp);
    }

    protected void validateAfterTimeStamp(String txnId, String timestamp) {
        Timestamp consentTimestamp = timestampToSqlDate.apply(timestamp);
        if (!consentTimestamp.after(Timestamp.from(Instant.now())))
            throw InvalidRequest.with(txnId, "Timestamp invalid : " + timestamp);

    }

}
