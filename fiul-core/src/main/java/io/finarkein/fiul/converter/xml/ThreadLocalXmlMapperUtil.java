/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.converter.xml;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.util.ServiceLoader;
import java.util.stream.Collectors;

@UtilityClass
@Log4j2
public class ThreadLocalXmlMapperUtil {
    private static final ThreadLocal<XmlMapper> formatStore;

    static {
        formatStore = ThreadLocal.withInitial(() -> {
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector());
            xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            final var collect = ServiceLoader.load(Module.class)
                    .stream()
                    .map(ServiceLoader.Provider::get)
                    .collect(Collectors.toList());
            log.info("ThreadLocalXmlMapperUtil.JacksonModules: loaded:{}",
                    collect.stream().map(Module::getModuleName).collect(Collectors.toList()));

            xmlMapper.registerModules(collect);
            return xmlMapper;
        });
    }

    public static XmlMapper getOrCreate() {
        return formatStore.get();
    }
}
