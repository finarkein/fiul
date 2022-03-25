/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.model;

import io.finarkein.fiul.Functions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTag {
    private String version;
    private String type;

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    public static final Pattern fiTypePattern = Pattern.compile("type=(\"|\\\\\")(.*?)(\"|\\\\\")");
    public static final Pattern versionPattern = Pattern.compile("<Account.*\\sversion=(\"|\\\\\")(.*?)(\"|\\\\\")");

    public static AccountTag readFromXml(String xml) {
        try {
            return getAccountTag(xml);
        } catch (Exception e) {
            final String escapedFIXML = Functions.escapeNarrationAttrInFIXml(xml);
            try {
                return getAccountTag(escapedFIXML);
            } catch (Exception e1) {
                throw new IllegalStateException(e1);
            }
        }
    }

    private static AccountTag getAccountTag(String xml) throws ParserConfigurationException, SAXException, IOException {
        var tag = new AccountTag();
        tag.setType(extractFiType(xml));
        tag.setVersion(extractVersion(xml));
        return tag;
    }

    public static String extractVersion(String xml) {
        Matcher matcher = versionPattern.matcher(xml);
        if (matcher.find())
            return matcher.group(2);
        throw new IllegalStateException("Unable to extract FIType from given xml");
    }

    public static String extractFiType(String xml) {
        Matcher matcher = fiTypePattern.matcher(xml);
        if (matcher.find())
            return matcher.group(2);
        throw new IllegalStateException("Unable to extract FIType from given xml");
    }
}