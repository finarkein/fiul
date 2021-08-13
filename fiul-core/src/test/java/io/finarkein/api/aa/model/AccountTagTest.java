/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.api.aa.model;

import io.finarkein.fiul.Functions;
import io.finarkein.fiul.converter.xml.XmlToBeanConverters;
import io.finarkein.fiul.model.AccountTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

class AccountTagTest {

    @Test
    void readXMLJackson() {
        var xmlFiles = new String[]{"AIF.xml", "Deposit.xml", "Term_Deposit.xml"};
        final var collect = Arrays.stream(xmlFiles)
                .map(fileName -> new File(getClass().getClassLoader().getResource(fileName).getFile()))
                .map(file -> {
                    try {
                        return inputStreamToString(new FileInputStream(file));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).map(AccountTag::readFromXml)
                .collect(Collectors.toList());
        Assertions.assertNotNull(collect);
    }

    private String inputStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    @Test
    void testXmlEscape() throws Exception {
        final var file = new File(getClass().getClassLoader().getResource("account-xml-escape.xml").getFile());
        try(final var inputStream = new FileInputStream(file)) {
            var xmlString = inputStreamToString(inputStream);

            final var escapedXML = Functions.escapeNarrationAttrInFIXml(xmlString);
            final var accountTag = AccountTag.readFromXml(escapedXML);
            final var account = XmlToBeanConverters.getConverter(accountTag.getType(), accountTag.getVersion()).converter().apply(escapedXML);
            Assertions.assertNotNull(account);
        }
    }
}
