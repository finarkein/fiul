/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

@SpringBootTest(classes = FiulServerApplication.class)
public class AbstractTest {

    protected String inputStreamToString(String file) {
        try {
            return inputStreamToString(new FileInputStream(getClass().getClassLoader().getResource(file).getFile()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String inputStreamToString(InputStream is) {
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
