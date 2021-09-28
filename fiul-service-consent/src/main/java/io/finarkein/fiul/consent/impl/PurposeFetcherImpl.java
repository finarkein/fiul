/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import io.finarkein.api.aa.consent.Category;
import io.finarkein.api.aa.consent.Purpose;
import io.finarkein.fiul.consent.service.PurposeFetcher;
import org.springframework.stereotype.Service;

@Service
public class PurposeFetcherImpl implements PurposeFetcher{
    @Override
    public Purpose fetchPurpose(String purposeCode) {
        Purpose purpose = new Purpose();
        purpose.setCode(purposeCode);
        purpose.setText("Wealth management service");
        purpose.setRefUri("https://api.rebit.org.in/aa/purpose/101.xml");
        Category category  = new Category();
        category.setType("string");
        purpose.setCategory(category);
        return purpose;
    }
}
