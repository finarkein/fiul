/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.model;

import io.finarkein.aa.fi.FIProfile;
import io.finarkein.aa.fi.FISummary;
import io.finarkein.aa.fi.FITransactions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class AccountData<P extends FIProfile, S extends FISummary, T extends FITransactions> {
    private String linkRefNumber;
    private String maskedAccNumber;
    private String type;
    private String version;
    private P profile;
    private S summary;
    private T transactions;
}
