/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.converter.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.finarkein.aa.fi.FIAccount;
import io.finarkein.fiul.Functions;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@NoArgsConstructor
public abstract class XmlToBeanConverter {

    public abstract String name();

    public abstract Class<?> getType();

    public Function<String, ? extends FIAccount> converter() {
        return xmlStr -> {
            XmlMapper xmlMapper = ThreadLocalXmlMapperUtil.getOrCreate();
            try {
                return (FIAccount) xmlMapper.readValue(xmlStr, getType());
            } catch (ClassCastException e) {
                throw new IllegalStateException(getType().getName() + " not extending:" + FIAccount.class.getName(), e);
            } catch (Exception e) {
                //Currently on any exception while parsing the FI xml(mostly narration attribute), retry parsing by escaping the FI xml content.
                //Not sure this is use case is handled by FIP/AA spec. This issue was observed on AAkash test data
                final String escapedFIXML = Functions.escapeNarrationAttrInFIXml(xmlStr);
                try {
                    return (FIAccount) xmlMapper.readValue(escapedFIXML, getType());
                } catch (Exception e1) {
                    throw new IllegalStateException(e1);
                }
            }
        };
    }
}
