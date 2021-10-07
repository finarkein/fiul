/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.api.aa.model;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import io.finarkein.test.openpojo.OpenPojoUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class GetterSetterTest {

    @Test
    public void validateSettersAndGetters() {
        String packageName = "io.finarkein.api.aa.model";
        List<PojoClass> pojoClasses = PojoClassFactory.getPojoClassesRecursively(packageName, null);
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("io.finarkein.fiul.ext", null));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("io.finarkein.fiul.model", null));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("io.finarkein.fiul.dataflow", null));
        pojoClasses.addAll(PojoClassFactory.getPojoClassesRecursively("io.finarkein.fiul.consent", null));
        pojoClasses.removeIf(n ->
                n.getClazz().getName().matches(".*Test$")
                        || n.getClazz().getName().matches(".*Builder$")
                        || n.getClazz().getName().matches(".*DataRequest$")
                        || n.getClazz().getName().matches(".*AccountData$")
                        || n.getClazz().getName().matches(".*(DecryptedFI|DecryptedDatum|FIFetchResponse|ObjectifiedFI|ObjectifiedDatum|ObjectifiedFIFetchResponse)$")

        );

        pojoClasses.stream()
                .forEach(pojoClass -> {
                            try {
                                OpenPojoUtils.validateSettersAndGetters(Collections.singletonList(pojoClass));
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw e;
                            }
                        }
                );
    }
}
