/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import io.finarkein.api.aa.crypto.CipherParameter;
import io.finarkein.api.aa.crypto.CipherResponse;
import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.api.aa.dataflow.response.Datum;
import io.finarkein.api.aa.dataflow.response.FI;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.fiul.dataflow.response.decrypt.DecryptedDatum;
import io.finarkein.fiul.dataflow.response.decrypt.DecryptedFI;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Functions {
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final SimpleDateFormat dateFormat;

    public static final Supplier<String> UUIDSupplier = () -> java.util.UUID.randomUUID().toString();

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    private static SimpleDateFormat createFormat() {
        SimpleDateFormat timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US);
        timestampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return timestampFormat;
    }

    public static final Supplier<String> currentTimestampSupplier = () -> createFormat().format(Date.from(Instant.now()));

    public static final Function<String, Timestamp> toTimestamp = timestamp -> {
        final Date inputTimestamp;
        try {
            inputTimestamp = createFormat().parse(timestamp);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid timestamp value:" + timestamp + ", expected format:" + TIMESTAMP_FORMAT);
        }
        return Timestamp.from(inputTimestamp.toInstant());
    };

    public static final Function<String, java.sql.Date> stringToDate = dateString -> {
        if (dateString == null)
            return null;
        try {
            return new java.sql.Date(dateFormat.parse(dateString).getTime());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    };

    /**
     * Interface to decode / decrypt the given FI
     */
    @FunctionalInterface
    public interface FIFetchResponseDecoder {
        io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse decode(FIFetchResponse responseDecode, CryptoServiceAdapter cryptoService, SerializedKeyPair key);
    }

    /**
     * Interface to decode / decrypt the given FI
     */
    @FunctionalInterface
    public interface FIDecoder {
        DecryptedFI decode(FI fiToDecode, CryptoServiceAdapter cryptoService, SerializedKeyPair key);
    }

    /**
     * Interface to decode / decrypt the given List<Datum>
     */
    @FunctionalInterface
    public interface DatumDecoder {
        List<DecryptedDatum> decode(List<Datum> datumList, CryptoServiceAdapter cryptoService, KeyMaterial remoteKeyMaterial, SerializedKeyPair key);
    }

    public static DatumDecoder datumDecoder = (datumListToDecode, cryptoService, remoteKeyMaterial, key) -> datumListToDecode
            .stream()
            .map(datum -> {
                        DecryptedDatum decryptedDatum = new DecryptedDatum(datum);
                        CipherParameter cipherParameter = prepareCipherParam(key, datum.getEncryptedFI(), remoteKeyMaterial);
                        CipherResponse decrypt = cryptoService.decrypt(cipherParameter);
                        decryptedDatum.setAccountData(decrypt.getBase64Data());
                        return decryptedDatum;
                    }
            ).collect(Collectors.toList());

    public static FIDecoder fiDecoder = (fiToDecode, cryptoService, key) -> {
        DecryptedFI fiData = new DecryptedFI(fiToDecode);
        fiData.setAccounts(datumDecoder.decode(fiToDecode.getData(), cryptoService, fiToDecode.getKeyMaterial(), key));
        return fiData;
    };

    public static final FIFetchResponseDecoder fiFetchResponseDecoder = (responseDecode, cryptoService, key) -> {
        List<DecryptedFI> fiDataList = responseDecode
                .getFi()
                .stream()
                .map(fiToDecode -> fiDecoder.decode(fiToDecode, cryptoService, key))
                .collect(Collectors.toList());
        io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse response = new io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse(responseDecode);
        response.setFipData(fiDataList);
        return response;
    };

    public static CipherParameter prepareCipherParam(SerializedKeyPair key, String base64Data, KeyMaterial remoteKeyMaterial) {
        return new CipherParameter(remoteKeyMaterial, key.getPrivateKey(), key.getKeyMaterial().getNonce(), remoteKeyMaterial.getNonce(), base64Data);
    }

    public static <T> T doGet(Mono<T> mono) {
        try {
            return mono.toFuture().get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static final BiFunction<Properties, String, Properties> prefixFilter = (inputProps, prefix) -> {
        Properties outputProps = new Properties();
        inputProps.entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith(prefix))
                .forEach(entry -> outputProps.put(entry.getKey(), entry.getValue()));
        return outputProps;
    };

    public static final BiFunction<Properties, String, Properties> regexPrefixFilter = (inputProps, prefix) -> {
        Properties outputProps = new Properties();
        inputProps.entrySet().stream()
                .filter(entry -> entry.getKey().toString().matches(prefix))
                .forEach(entry -> outputProps.put(entry.getKey(), entry.getValue()));
        return outputProps;
    };

    public static final BiFunction<Properties, String, Properties> prefixFilterAndReplacer = (inputProps, prefix) -> {
        Properties outputProps = new Properties();
        inputProps.entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith(prefix))
                .forEach(entry -> outputProps.put(entry.getKey().toString().replaceFirst(prefix, ""), entry.getValue()));
        return outputProps;
    };

    public static class Strings {
        public static boolean isBlank(String s) {
            return s == null || s.trim().isEmpty();
        }
    }

    /**
     * @return Signature part of the Consent JWS<br>
     * <b>e.g. For given </b>"yJhbGciOiJSUzI1NiIsImtpZCI6ImY2NzZiNjg4LTA3YTItNDUwYi1hYmI1LTU3ZmJiOThlY2FiZSIsImI2NCI6dHJ1ZSwiY3JpdCI6WyJiNjQiXX0.eyJjb25zZW50U3RhcnQiOiIyMDIxLTA0LTA2VDE0OjUzOjUzLjQ3OSswMDAwIiwiY29uc2VudEV4cGlyeSI6IjIwMjEtMTItMzFUMTE6Mzk6NTcuMTUzKzAwMDAiLCJjb25zZW50TW9kZSI6IlNUT1JFIiwiZmV0Y2hUeXBlIjoiUEVSSU9ESUMiLCJjb25zZW50VHlwZXMiOlsiVFJBTlNBQ1RJT05TIiwiUFJPRklMRSIsIlNVTU1BUlkiXSwiZmlUeXBlcyI6WyJERVBPU0lUIl0sIkRhdGFDb25zdW1lciI6eyJpZCI6ImZpdUBmaW5hcmtlaW4iLCJ0eXBlIjoiRklVIn0sIkRhdGFQcm92aWRlciI6eyJpZCI6ImNvb2tpZWphci1hYUBmaW52dS5pbiIsInR5cGUiOiJBQSJ9LCJDdXN0b21lciI6eyJpZCI6InByYXRobWVzaEBmaW52dSJ9LCJBY2NvdW50cyI6W3siZmlUeXBlIjoiREVQT1NJVCIsImZpcElkIjoiQkFSQjBLSU1YWFgiLCJhY2NUeXBlIjoiU0FWSU5HUyIsImxpbmtSZWZOdW1iZXIiOiJhNWYzOWEyZS01NDJhLTQ0MjMtYTRmNC1kMjY1ZmEwMDUzOTgiLCJtYXNrZWRBY2NOdW1iZXIiOiJYWFhYWFhYWDEyMjEifV0sIlB1cnBvc2UiOnsiY29kZSI6IjEwMSIsInJlZlVyaSI6Imh0dHBzOi8vYXBpLnJlYml0Lm9yZy5pbi9hYS9wdXJwb3NlLzEwMS54bWwiLCJ0ZXh0IjoiV2VhbHRoIG1hbmFnZW1lbnQgc2VydmljZSIsIkNhdGVnb3J5Ijp7InR5cGUiOiJzdHJpbmcifX0sIkZJRGF0YVJhbmdlIjp7ImZyb20iOiIyMDE4LTEyLTA2VDExOjM5OjU3LjE1MyswMDAwIiwidG8iOiIyMDIwLTA3LTAzVDE0OjI1OjMzLjQ0MCswMDAwIn0sIkRhdGFMaWZlIjp7InVuaXQiOiJNT05USCIsInZhbHVlIjoxfSwiRnJlcXVlbmN5Ijp7InVuaXQiOiJNT05USCIsInZhbHVlIjoxfSwiRGF0YUZpbHRlciI6W3sidHlwZSI6IlRSQU5TQUNUSU9OQU1PVU5UIiwib3BlcmF0b3IiOiI-PSIsInZhbHVlIjoiMjAwMDAifV19.cBWc0nAR1mKT47Zd3CS617RDlcvZmeRMDet36Sg2OAr6jBIfjR2PU39M8dunhdtHNiGJf8H8EmbczON-LidCgPN6D7fOAGG68eXQphefeHtglx5LLTSheXhXQDyO6b8ukECLh1qkOUnxp6eRiv08YW60X3h0Nq0X0BjCXImFF1W_8bHXKaenfok8xswQXySePffFWn5z8SPvK9E-ndM20XYprjTixl0FB0pH-Rlw10E2zUleI2YYtPYA2J-8OppUt3V6gC-TiDpxWSgvMrEisGjTR5Mq8VZ-yy07_wCsZc8Uk17h6gPqCE3DQfbTmulZ-N6vlGx8I6uiyff2rNxWFA"<br><br>
     * <b>returns,</b><br>"cBWc0nAR1mKT47Zd3CS617RDlcvZmeRMDet36Sg2OAr6jBIfjR2PU39M8dunhdtHNiGJf8H8EmbczON-LidCgPN6D7fOAGG68eXQphefeHtglx5LLTSheXhXQDyO6b8ukECLh1qkOUnxp6eRiv08YW60X3h0Nq0X0BjCXImFF1W_8bHXKaenfok8xswQXySePffFWn5z8SPvK9E-ndM20XYprjTixl0FB0pH-Rlw10E2zUleI2YYtPYA2J-8OppUt3V6gC-TiDpxWSgvMrEisGjTR5Mq8VZ-yy07_wCsZc8Uk17h6gPqCE3DQfbTmulZ-N6vlGx8I6uiyff2rNxWFA"
     */
    public static String getSignature(String jws) {
        if (jws == null)
            return null;
        final int lastIndexOf = jws.lastIndexOf(".");
        if (lastIndexOf == -1)
            throw new IllegalArgumentException("Invalid jws string passed, could not find lastIndexOf(.) in the string:");

        return jws.substring(lastIndexOf + 1);
    }

    private static final Pattern addressPattern = Pattern.compile("((address=\")[^\"]*)");
    private static final Pattern addressTextPattern = Pattern.compile("(address=\")");
    private static final Pattern narrationPattern = Pattern.compile("((narration=\")[^\"]*)");
    private static final Pattern narrationTextPattern = Pattern.compile("(narration=\")");
    private static final List<String[]> escapeCharPairs = Arrays.asList(new String[]{"<", "&lt;"},
            new String[]{">", "&gt;"},
            new String[]{"'", "&apos;"},
            new String[]{"&", "&amp;"}
    );

    /**
     * Currently on any exception while parsing the FI xml(mostly narration attribute), retry parsing by escaping the FI xml content.
     * Not sure this is use case is handled by FIP/AA spec. This issue was observed on AAkash test data
     *
     * @param xmlString
     * @return
     */
    public static String escapeNarrationAttrInFIXml(String xmlString) {
        final String[] strContainer = new String[]{xmlString};
        narrationPattern.matcher(xmlString)
                .results()
                .map(matchResult -> matchResult.group(0))
                .filter(narration -> escapeCharPairs.stream().anyMatch(escapeChar -> narration.indexOf(escapeChar[0]) > 0))
                .map(narration -> narrationTextPattern.matcher(narration).replaceAll(""))
                .map(narrationText -> {
                    String[] textPair = new String[]{narrationText, narrationText};
                    escapeCharPairs.stream().forEach(escapeChar -> textPair[1] = textPair[1].replaceAll(escapeChar[0], escapeChar[1]));
                    return textPair;
                })
                .map(textPair -> strContainer[0] = strContainer[0].replaceAll(textPair[0], textPair[1]))
                .count()
        ;
        addressPattern.matcher(xmlString)
                .results()
                .map(matchResult -> matchResult.group(0))
                .filter(address -> escapeCharPairs.stream().anyMatch(escapeChar -> address.indexOf(escapeChar[0]) > 0))
                .map(address -> addressTextPattern.matcher(address).replaceAll(""))
                .map(addressText -> {
                    String[] textPair = new String[]{addressText, addressText};
                    escapeCharPairs.stream().forEach(escapeChar -> textPair[1] = textPair[1].replaceAll(escapeChar[0], escapeChar[1]));
                    return textPair;
                })
                .map(textPair -> strContainer[0] = strContainer[0].replaceAll(textPair[0], textPair[1]))
                .count();
        return strContainer[0];
    }
}
