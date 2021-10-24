/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.converter.xml;

import io.finarkein.aa.fi.FIAccount;
import io.finarkein.fiul.dataflow.response.decrypt.*;
import io.finarkein.fiul.model.AccountTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XMLConverterFunctions {

    public static final Function<String, FIAccount> xmlToFIBeanComputer = fiXML -> {
        final var accountTag = AccountTag.readFromXml(fiXML);
        return XmlToBeanConverters.getConverter(accountTag.getType(), accountTag.getVersion()).converter().apply(fiXML);
    };

    static final Function<DecryptedDatum, AccountData> datumToObjectifiedDatum = decryptedDatum -> {
        var objectifiedDatum = new AccountData(decryptedDatum);
        objectifiedDatum.setAccountData(xmlToFIBeanComputer.apply(decryptedDatum.getAccountData()));
        return objectifiedDatum;
    };

    public static final Function<FIFetchResponse, FIData> fiFetchResponseToObject = fiFetchResponse -> {
        final List<FIPData> FIPDataDataList = fiFetchResponse.getFipData().stream().map(decryptedFIData -> {
            final List<AccountData> AccountDataList = decryptedFIData.getAccounts()
                    .stream()
                    .map(datumToObjectifiedDatum)
                    .collect(Collectors.toList());
            var objectifiedFIData = new FIPData(decryptedFIData);
            objectifiedFIData.setAccountData(AccountDataList);
            return objectifiedFIData;
        }).collect(Collectors.toList());
        var objectifiedFIFetchResponse = new FIData(fiFetchResponse);
        objectifiedFIFetchResponse.setFipData(FIPDataDataList);
        return objectifiedFIFetchResponse;
    };
}
