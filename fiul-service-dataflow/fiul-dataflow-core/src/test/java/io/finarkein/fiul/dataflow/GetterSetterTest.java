/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterChain;
import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.reflection.impl.PojoClassFactory;
import io.finarkein.fiul.dataflow.dto.FIDataHeader;
import io.finarkein.fiul.dataflow.easy.DataRequestStatus;
import io.finarkein.fiul.dataflow.easy.SessionStatus;
import io.finarkein.test.openpojo.OpenPojoUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

class GetterSetterTest {

    @Test
    void validateSettersAndGetters() {
        String packageName = FIDataHeader.class.getPackageName();

        FilterChain chain = new FilterChain(
                new FilterClassName(".*Test$"),
                new FilterClassName(".*Converter.*"),
                new FilterClassName(".*Builder$"),
                new FilterClassName(".*FIDataKeyEntityListener$")
        );

        List<PojoClass> pojoClasses = PojoClassFactory.getPojoClassesRecursively(packageName, chain);
        pojoClasses.add(PojoClassFactory.getPojoClass(DataRequestStatus.class));
        pojoClasses.add(PojoClassFactory.getPojoClass(SessionStatus.class));

        OpenPojoUtils.validateSettersAndGetters(pojoClasses);
    }
}
