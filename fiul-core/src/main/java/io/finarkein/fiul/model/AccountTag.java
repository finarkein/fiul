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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTag {
    private String linkedAccRef;
    private String maskedAccNumber;
    private String xmlns;
    private String xmlnsXsi;
    private String xsiSchemaLocation;
    private String version;
    private String type;

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

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
        var db = dbf.newDocumentBuilder();
        final Document parse = db.parse(new ByteArrayInputStream(xml.getBytes()));
        final NodeList account = parse.getElementsByTagName("Account");
        final Node item = account.item(0);
        var tag = new AccountTag();
        tag.setLinkedAccRef(item.getAttributes().getNamedItem("linkedAccRef").getNodeValue());
        tag.setVersion(item.getAttributes().getNamedItem("version").getNodeValue());
        tag.setType(item.getAttributes().getNamedItem("type").getNodeValue());
        tag.setMaskedAccNumber(item.getAttributes().getNamedItem("maskedAccNumber").getNodeValue());
//        tag.setXmlns(item.getAttributes().getNamedItem("xmlns").getNodeValue());
//        tag.setXmlnsXsi(item.getAttributes().getNamedItem("xmlns:xsi").getNodeValue());
//        tag.setXsiSchemaLocation(item.getAttributes().getNamedItem("xsi:schemaLocation").getNodeValue());
        return tag;
    }
}
