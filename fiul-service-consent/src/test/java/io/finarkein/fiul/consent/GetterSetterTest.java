/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.test.openpojo.OpenPojoUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

class GetterSetterTest {

    @Test
    void validateSettersAndGetters() {
        String packageName = ConsentNotificationLog.class.getPackageName();
        List<PojoClass> pojoClasses = PojoClassFactory.getPojoClassesRecursively(packageName, null);
        pojoClasses.removeIf(n ->
                {
                    final String className = n.getClazz().getName();
                    return className.matches(".*Builder$")
                            || className.matches(".*ConsentJsonAttrConverter.*|.*TimeDuration");
                }
        );
        OpenPojoUtils.validateSettersAndGetters(pojoClasses);
    }
}