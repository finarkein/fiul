/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.api.aa.fiu;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

    public static void main(String[] args) {
        String dateValue = "2021-04-09T09:07:09.366+0000";
        String[] formats = new String[]{"yyyy-MM-dd'T'HH:mm:ss*SSSZZZZ",
                "yyyy MMM dd HH:mm:ss.SSS zzz", "MMM dd HH:mm:ss ZZZZ yyyy",
                "dd/MMM/yyyy:HH:mm:ss ZZZZ", "MMM dd, yyyy hh:mm:ss a",
                "MMM dd yyyy HH:mm:ss", "MMM dd HH:mm:ss yyyy", "MMM dd HH:mm:ss ZZZZ",
                "MMM dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssZZZZ", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd HH:mm:ss ZZZZ", "yyyy-MM-dd HH:mm:ssZZZZ", "yyyy-MM-dd HH:mm:ss,SSS",
                "yyyy/MM/dd*HH:mm:ss", "yyyy MMM dd HH:mm:ss.SSS*zzz", "yyyy MMM dd HH:mm:ss.SSS",
                "yyyy-MM-dd HH:mm:ss,SSSZZZZ", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SSSZZZZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS"};

        final List<String> collect = Arrays.stream(formats)
                .filter(format -> {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
                    try {
                        simpleDateFormat.parse(dateValue);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }).collect(Collectors.toList());
        System.out.println(collect);
    }
}
