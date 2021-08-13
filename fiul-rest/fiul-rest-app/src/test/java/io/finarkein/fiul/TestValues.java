/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.common.FIDataRange;
import io.finarkein.api.aa.consent.*;
import io.finarkein.api.aa.consent.artefact.ConsentArtefact;
import io.finarkein.api.aa.consent.artefact.ConsentUse;
import io.finarkein.api.aa.consent.handle.ConsentHandleResponse;
import io.finarkein.api.aa.consent.handle.ConsentStatus;
import io.finarkein.api.aa.consent.request.ConsentDetail;
import io.finarkein.api.aa.consent.request.ConsentResponse;
import io.finarkein.api.aa.consent.request.DataConsumer;
import io.finarkein.api.aa.crypto.DHPublicKey;
import io.finarkein.api.aa.crypto.KeyMaterial;
import io.finarkein.api.aa.crypto.SerializedKeyPair;
import io.finarkein.api.aa.dataflow.Consent;
import io.finarkein.api.aa.dataflow.FIRequestResponse;
import io.finarkein.api.aa.dataflow.response.Datum;
import io.finarkein.api.aa.dataflow.response.FI;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.exception.SystemException;
import io.finarkein.api.aa.heartbeat.HeartbeatResponse;
import io.finarkein.api.aa.notification.*;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.model.ConsentRequestLog;
import io.finarkein.fiul.consent.model.ErrorOrigin;
import io.finarkein.fiul.dataflow.FIUFIRequest;
import io.finarkein.fiul.dataflow.FetchDataRequest;
import io.finarkein.fiul.dataflow.response.decrypt.DecryptedDatum;
import io.finarkein.fiul.dataflow.response.decrypt.DecryptedFI;
import io.finarkein.fiul.ext.Callback;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TestValues {
    public static String expectedValue = "JWS_SIGNED(abcd)";
    public static String oneMoney = "onemoney";
    public static String finvu = "finvu";
    public static String consentHandle = Functions.UUIDSupplier.get();
    public static String consentId = Functions.UUIDSupplier.get();
    public static String txnId = Functions.UUIDSupplier.get();
    public static String timestamp = Functions.currentTimestampSupplier.get();
    public static Timestamp timestamp1 = Timestamp.from(Instant.now());
    public static String sessionId = Functions.UUIDSupplier.get() ;
    public static String dataSessionId = Functions.UUIDSupplier.get();
    public static String LinkRefNUmber = Functions.UUIDSupplier.get();
    public static String[] linkRefArray = {LinkRefNUmber};
    public static String nonce = Functions.UUIDSupplier.get();
    public static String fetchDataRequest = "{\t\"ver\":\"1.1.2\",\n" +
            "\t\"from\": \"2021-07-13T13:46:20.525Z\",\n" +
            "\t\"to\": \"2021-12-31T11:39:57.153Z\",\n" +
            "\t\"consentHandleId\": \"de2c7a26-2ad2-4fce-85eb-d4b0cabe8bf4\",\n" +
            "\t\"KeyMaterial\": {\n" +
            "    \t\"cryptoAlg\": \"ECDH\",\n" +
            "    \t\"curve\": \"Curve25519\",\n" +
            "    \t\"params\": \"cipher=AES/GCM/NoPadding;KeyPairGenerator=ECDH\",\n" +
            "    \t\"DHPublicKey\": {\n" +
            "      \t\t\"expiry\": \"2022-12-06T11:39:57.153Z\",\n" +
            "      \t\t\"Parameters\": \"string\",\n" +
            "      \t\t\"KeyValue\": \"string\"\n" +
            "    \t},\n" +
            "    \t\"Nonce\": \"fec04c52-ed4d-428e-ba6c-cacdbdc7d56c\"\n" +
            "  \t}\n" +
            "  }";



    public static HeartbeatResponse getHeartbeatResponse() {
        HeartbeatResponse heartbeatResponse = new HeartbeatResponse();
        heartbeatResponse.setVer("1.1.2");
        heartbeatResponse.setTimestamp(timestamp);
        heartbeatResponse.setStatus("UP");
        return heartbeatResponse;
    }



    public static SystemException exception(){
        return Errors.InvalidRequest.with(txnId,"Test Controller advice");
    }



    public static Customer getFinvuCustomer(){
        Customer customerFin = new Customer();
        customerFin.setId("9657739012@onemoney");
        return customerFin;
    }

    public static SerializedKeyPair getSerializedPair(){
        SerializedKeyPair serializedKeyPair = new SerializedKeyPair();
        serializedKeyPair.setPrivateKey("Private Key");
        KeyMaterial keyMaterial = new KeyMaterial();
        keyMaterial.setCryptoAlg("ECDH");
        keyMaterial.setParams("cipher=AES/GCM/NoPadding;KeyPairGenerator=ECDH");
        keyMaterial.setNonce(nonce);
        keyMaterial.setCurve("Curve25519");
        DHPublicKey dhPublicKey = new DHPublicKey();
        dhPublicKey.setExpiry("2022-12-06T11:39:57.153Z");
        dhPublicKey.setParameters("string");
        dhPublicKey.setKeyValue("string");
        keyMaterial.setDhPublicKey(dhPublicKey);
        serializedKeyPair.setKeyMaterial(keyMaterial);
//        ErrorInfo errorInfo = new ErrorInfo();
//        errorInfo.setErrorCode("101");
//        errorInfo.setErrorMessage("String");
//        serializedKeyPair.setErrorInfo(errorInfo);
        return serializedKeyPair;
    }


    public static FIUConsentRequest getFinvuConsentRequest(){
        FIUConsentRequest fiuConsentRequestFin = new FIUConsentRequest();
        fiuConsentRequestFin.setVer("1.1.2");
        fiuConsentRequestFin.setTxnid(txnId);
        fiuConsentRequestFin.setTimestamp(timestamp);
        ConsentDetail consentDetailFin = new ConsentDetail();
        consentDetailFin.setConsentStart(Functions.currentTimestampSupplier.get());
        consentDetailFin.setConsentExpiry("2021-12-31T11:39:57.153Z");
        consentDetailFin.setConsentMode("STORE");
        consentDetailFin.setFetchType("PERIODIC");
        consentDetailFin.setConsentTypes( new ArrayList<>(Arrays.asList("TRANSACTIONS","PROFILE","SUMMARY")));
        consentDetailFin.setFiTypes(new ArrayList<>(Arrays.asList("DEPOSIT")));
        DataConsumer dataConsumerFin = new DataConsumer();
        dataConsumerFin.setId("FIN0566");
        consentDetailFin.setDataConsumer(dataConsumerFin);
        Purpose purpose = new Purpose();
        purpose.setCode("101");
        purpose.setRefUri("https://api.rebit.org.in/aa/purpose/101.xml");
        purpose.setText("Wealth management service");
        Category category = new Category();
        category.setType("string");
        purpose.setCategory(category);
        consentDetailFin.setPurpose(purpose);
        FIDataRange fiDataRange = new FIDataRange();
        fiDataRange.setFrom("2018-12-06T11:39:57.153Z");
        fiDataRange.setTo("2020-07-03T14:25:33.440Z");
        consentDetailFin.setFIDataRange(fiDataRange);
        DataLife dataLife = new DataLife();
        dataLife.setUnit("MONTH");
        dataLife.setValue(1);
        consentDetailFin.setDataLife(dataLife);
        Frequency frequency = new Frequency();
        frequency.setUnit("MONTH");
        frequency.setValue(100);
        consentDetailFin.setFrequency(frequency);
        DataFilter dataFilter = new DataFilter();
        dataFilter.setType("TRANSACTIONAMOUNT");
        dataFilter.setOperator(">=");
        dataFilter.setValue("2000");
        consentDetailFin.setDataFilter(new ArrayList<>(Arrays.asList(dataFilter)));
        fiuConsentRequestFin.setConsentDetail(consentDetailFin);
        return fiuConsentRequestFin;
    }

    public static FIUConsentRequest consentRequestError(){
        FIUConsentRequest fiuConsentRequestFin = new FIUConsentRequest();
        fiuConsentRequestFin.setVer("1.1.1");
        fiuConsentRequestFin.setTxnid(txnId);
        fiuConsentRequestFin.setTimestamp(timestamp);
        ConsentDetail consentDetailFin = new ConsentDetail();
        consentDetailFin.setConsentStart(Functions.currentTimestampSupplier.get());
        consentDetailFin.setConsentExpiry("2021-12-31T11:39:57.153Z");
        consentDetailFin.setConsentMode("STORE");
        consentDetailFin.setFetchType("PERIODIC");
        consentDetailFin.setConsentTypes( new ArrayList<>(Arrays.asList("TRANSACTIONS","PROFILE","SUMMARY")));
        consentDetailFin.setFiTypes(List.of("DEPOSIT"));
        DataConsumer dataConsumerFin = new DataConsumer();
        dataConsumerFin.setId("FIN0566");
        consentDetailFin.setDataConsumer(dataConsumerFin);
        Purpose purpose = new Purpose();
        purpose.setCode("101");
        purpose.setRefUri("https://api.rebit.org.in/aa/purpose/101.xml");
        purpose.setText("Wealth management service");
        Category category = new Category();
        category.setType("string");
        purpose.setCategory(category);
        consentDetailFin.setPurpose(purpose);
        FIDataRange fiDataRange = new FIDataRange();
        fiDataRange.setFrom("2018-12-06T11:39:57.153Z");
        fiDataRange.setTo("2020-07-03T14:25:33.440Z");
        consentDetailFin.setFIDataRange(fiDataRange);
        DataLife dataLife = new DataLife();
        dataLife.setUnit("MONTH");
        dataLife.setValue(1);
        consentDetailFin.setDataLife(dataLife);
        Frequency frequency = new Frequency();
        frequency.setUnit("MONTH");
        frequency.setValue(100);
        consentDetailFin.setFrequency(frequency);
        DataFilter dataFilter = new DataFilter();
        dataFilter.setType("TRANSACTIONAMOUNT");
        dataFilter.setOperator(">=");
        dataFilter.setValue("2000");
        consentDetailFin.setDataFilter(List.of(dataFilter));
        fiuConsentRequestFin.setConsentDetail(consentDetailFin);
        Callback callback = new Callback();
        callback.setUrl("http://fiul:8080/Callback-url-for-consentRequestError");
        fiuConsentRequestFin.setCallback(callback);
        return fiuConsentRequestFin;
    }

    public static FIUConsentRequest consentRequestCallback(){
        FIUConsentRequest fiuConsentRequestFin = new FIUConsentRequest();
        fiuConsentRequestFin.setVer("1.1.2");
        fiuConsentRequestFin.setTxnid(txnId);
        fiuConsentRequestFin.setTimestamp(timestamp);
        ConsentDetail consentDetailFin = new ConsentDetail();
        consentDetailFin.setConsentStart(Functions.currentTimestampSupplier.get());
        consentDetailFin.setConsentExpiry("2021-12-31T11:39:57.153Z");
        consentDetailFin.setConsentMode("STORE");
        consentDetailFin.setFetchType("PERIODIC");
        consentDetailFin.setConsentTypes( new ArrayList<>(Arrays.asList("TRANSACTIONS","PROFILE","SUMMARY")));
        consentDetailFin.setFiTypes(new ArrayList<>(Arrays.asList("DEPOSIT")));
        DataConsumer dataConsumerFin = new DataConsumer();
        dataConsumerFin.setId("FIN0566");
        consentDetailFin.setDataConsumer(dataConsumerFin);
        Purpose purpose = new Purpose();
        purpose.setCode("101");
        purpose.setRefUri("https://api.rebit.org.in/aa/purpose/101.xml");
        purpose.setText("Wealth management service");
        Category category = new Category();
        category.setType("string");
        purpose.setCategory(category);
        consentDetailFin.setPurpose(purpose);
        FIDataRange fiDataRange = new FIDataRange();
        fiDataRange.setFrom("2018-12-06T11:39:57.153Z");
        fiDataRange.setTo("2020-07-03T14:25:33.440Z");
        consentDetailFin.setFIDataRange(fiDataRange);
        DataLife dataLife = new DataLife();
        dataLife.setUnit("MONTH");
        dataLife.setValue(1);
        consentDetailFin.setDataLife(dataLife);
        Frequency frequency = new Frequency();
        frequency.setUnit("MONTH");
        frequency.setValue(100);
        consentDetailFin.setFrequency(frequency);
        DataFilter dataFilter = new DataFilter();
        dataFilter.setType("TRANSACTIONAMOUNT");
        dataFilter.setOperator(">=");
        dataFilter.setValue("2000");
        Customer customer = new Customer();
        customer.setId("prathmesh@finvu");
        consentDetailFin.setCustomer(customer);
        consentDetailFin.setDataFilter(new ArrayList<>(Arrays.asList(dataFilter)));
        fiuConsentRequestFin.setConsentDetail(consentDetailFin);
        Callback callback = new Callback();
        callback.setUrl("callback url");
        fiuConsentRequestFin.setCallback(callback);
        return fiuConsentRequestFin;
    }

    public static ConsentResponse getFinvuConsentResponse(){
        ConsentResponse consentResponseFin = new ConsentResponse();
        consentResponseFin.setVer("1.1.2");
        consentResponseFin.setTimestamp(timestamp);
        consentResponseFin.setTxnid(txnId);
        consentResponseFin.setCustomer(getFinvuCustomer());
        consentResponseFin.setConsentHandle(consentHandle);
        return consentResponseFin;
    }

    public static ConsentRequestLog getConsentRequestLogCallback(){
        ConsentRequestLog consentRequestLog = new ConsentRequestLog();
        consentRequestLog.setId(1234);
        consentRequestLog.setVersion("1.1.2");
        consentRequestLog.setTxnId(txnId);
        consentRequestLog.setTimestamp(timestamp1);
        consentRequestLog.setAaId("AA-1");
        consentRequestLog.setCustomerAAId("CustomerAAId");
        ConsentDetail consentDetailFin = new ConsentDetail();
        consentDetailFin.setConsentStart(Functions.currentTimestampSupplier.get());
        consentDetailFin.setConsentExpiry("2021-12-31T11:39:57.153Z");
        consentDetailFin.setConsentMode("STORE");
        consentDetailFin.setFetchType("PERIODIC");
        consentDetailFin.setConsentTypes( new ArrayList<>(Arrays.asList("TRANSACTIONS","PROFILE","SUMMARY")));
        consentDetailFin.setFiTypes(new ArrayList<>(Arrays.asList("DEPOSIT")));
        DataConsumer dataConsumerFin = new DataConsumer();
        dataConsumerFin.setId("FIN0566");
        consentDetailFin.setDataConsumer(dataConsumerFin);
        Purpose purpose = new Purpose();
        purpose.setCode("101");
        purpose.setRefUri("https://api.rebit.org.in/aa/purpose/101.xml");
        purpose.setText("Wealth management service");
        Category category = new Category();
        category.setType("string");
        purpose.setCategory(category);
        consentDetailFin.setPurpose(purpose);
        FIDataRange fiDataRange = new FIDataRange();
        fiDataRange.setFrom("2018-12-06T11:39:57.153Z");
        fiDataRange.setTo("2020-07-03T14:25:33.440Z");
        consentDetailFin.setFIDataRange(fiDataRange);
        DataLife dataLife = new DataLife();
        dataLife.setUnit("MONTH");
        dataLife.setValue(1);
        consentDetailFin.setDataLife(dataLife);
        Frequency frequency = new Frequency();
        frequency.setUnit("MONTH");
        frequency.setValue(100);
        consentDetailFin.setFrequency(frequency);
        DataFilter dataFilter = new DataFilter();
        dataFilter.setType("TRANSACTIONAMOUNT");
        dataFilter.setOperator(">=");
        dataFilter.setValue("2000");
        consentDetailFin.setDataFilter(new ArrayList<>(Arrays.asList(dataFilter)));
        consentRequestLog.setConsentDetail(consentDetailFin);
        consentRequestLog.setCreatedOn(timestamp1);
        return consentRequestLog;
    }

    public static ConsentRequestLog getConsentRequestLog(){
        ConsentRequestLog consentRequestLog = new ConsentRequestLog();
        consentRequestLog.setId(1234);
        consentRequestLog.setVersion("1.1.1");
        consentRequestLog.setTxnId(txnId);
        consentRequestLog.setTimestamp(timestamp1);
        consentRequestLog.setAaId("AA-1");
        consentRequestLog.setCustomerAAId("CustomerAAId");
        ConsentDetail consentDetailFin = new ConsentDetail();
        consentDetailFin.setConsentStart(Functions.currentTimestampSupplier.get());
        consentDetailFin.setConsentExpiry("2021-12-31T11:39:57.153Z");
        consentDetailFin.setConsentMode("STORE");
        consentDetailFin.setFetchType("PERIODIC");
        consentDetailFin.setConsentTypes( new ArrayList<>(Arrays.asList("TRANSACTIONS","PROFILE","SUMMARY")));
        consentDetailFin.setFiTypes(new ArrayList<>(Arrays.asList("DEPOSIT")));
        DataConsumer dataConsumerFin = new DataConsumer();
        dataConsumerFin.setId("FIN0566");
        consentDetailFin.setDataConsumer(dataConsumerFin);
        Purpose purpose = new Purpose();
        purpose.setCode("101");
        purpose.setRefUri("https://api.rebit.org.in/aa/purpose/101.xml");
        purpose.setText("Wealth management service");
        Category category = new Category();
        category.setType("string");
        purpose.setCategory(category);
        consentDetailFin.setPurpose(purpose);
        FIDataRange fiDataRange = new FIDataRange();
        fiDataRange.setFrom("2018-12-06T11:39:57.153Z");
        fiDataRange.setTo("2020-07-03T14:25:33.440Z");
        consentDetailFin.setFIDataRange(fiDataRange);
        DataLife dataLife = new DataLife();
        dataLife.setUnit("MONTH");
        dataLife.setValue(1);
        consentDetailFin.setDataLife(dataLife);
        Frequency frequency = new Frequency();
        frequency.setUnit("MONTH");
        frequency.setValue(100);
        consentDetailFin.setFrequency(frequency);
        DataFilter dataFilter = new DataFilter();
        dataFilter.setType("TRANSACTIONAMOUNT");
        dataFilter.setOperator(">=");
        dataFilter.setValue("2000");
        consentDetailFin.setDataFilter(new ArrayList<>(Arrays.asList(dataFilter)));
        consentRequestLog.setConsentDetail(consentDetailFin);
        consentRequestLog.setCreatedOn(timestamp1);
        consentRequestLog.setErrorDetails("Test Error");
        consentRequestLog.setErrorOrigin(ErrorOrigin.ERROR_IN_VALIDATION);
        return consentRequestLog;
    }


    public static ConsentHandleResponse getConsentHandleResponse(){
        ConsentHandleResponse consentHandleResponse = new ConsentHandleResponse();
        consentHandleResponse.setVer("1.1.2");
        consentHandleResponse.setTxnid(txnId);
        consentHandleResponse.setTimestamp(timestamp);
        consentHandleResponse.setConsentHandle(consentHandle);
        ConsentStatus consentStatus = new ConsentStatus();
        consentStatus.setId(consentId);
        consentStatus.setStatus("Ready");
        consentHandleResponse.setConsentStatus(consentStatus);
        return consentHandleResponse;
    }

    public static ConsentArtefact getConsentArtefact(){
        ConsentArtefact consentArtefact = new ConsentArtefact();
        consentArtefact.setVer("1.1.2");
        consentArtefact.setTxnid(txnId);
        consentArtefact.setCreateTimestamp(timestamp);
        ConsentUse consentUse = new ConsentUse();
        consentUse.setCount(1);
        consentUse.setLogUri("string");
        consentUse.setLastUseDateTime(timestamp);
        consentArtefact.setConsentUse(consentUse);
        consentArtefact.setSignedConsent("eyJhbGciOiJSUzI1NiIsImtpZCI6IjQyNzE5MTNlLTdiOTMtNDlkZC05OTQ5LTFjNzZmZjVmYzVjZiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19.ew0KICAgICAgICAiY29uc2VudFN0YXJ0IjogIjIwMTktMDUtMjhUMTE6Mzg6MjAuMzgwKzAwMDAiLA0KICAgICAgICAiY29uc2VudEV4cGlyeSI6ICIyMDIwLTA1LTI4VDExOjM4OjIwLjM4MSswMDAwIiwNCiAgICAgICAgImNvbnNlbnRNb2RlIjogIlZJRVciLA0KICAgICAgICAiZmV0Y2hUeXBlIjogIk9ORVRJTUUiLA0KICAgICAgICAiY29uc2VudFR5cGVzIjogWw0KICAgICAgICAgICAgIlBST0ZJTEUiLA0KICAgICAgICAgICAgIlNVTU1BUlkiLA0KICAgICAgICAgICAgIlRSQU5TQUNUSU9OUyINCiAgICAgICAgXSwNCiAgICAgICAgImZpVHlwZXMiOiBbDQogICAgICAgICAgICAiREVQT1NJVCIsDQogICAgICAgICAgICAiVEVSTS1ERVBPU0lUIg0KICAgICAgICBdLA0KICAgICAgICAiRGF0YUNvbnN1bWVyIjogew0KICAgICAgICAgICAgImlkIjogImNvb2tpZWphci1hYUBmaW52dS5pbiIsDQogICAgICAgICAgICAidHlwZSI6ICJBQSINCiAgICAgICAgfSwNCiAgICAgICAgIkRhdGFQcm92aWRlciI6IHsNCiAgICAgICAgICAgICJpZCI6ICJCQVJCMEtJTVhYWCIsDQogICAgICAgICAgICAidHlwZSI6ICJGSVAiDQogICAgICAgIH0sDQogICAgICAgICJDdXN0b21lciI6IHsNCiAgICAgICAgICAgICJpZCI6ICJkZW1vQGZpbnZ1Ig0KICAgICAgICB9LA0KICAgICAgICAiQWNjb3VudHMiOiBbDQogICAgICAgICAgICB7DQogICAgICAgICAgICAgICAgImZpVHlwZSI6ICJERVBPU0lUIiwNCiAgICAgICAgICAgICAgICAiZmlwSWQiOiAiQkFSQjBLSU1YWFgiLA0KICAgICAgICAgICAgICAgICJhY2NUeXBlIjogIlNBVklOR1MiLA0KICAgICAgICAgICAgICAgICJsaW5rUmVmTnVtYmVyIjogIlVCSTQ4NTk2NDU3OSIsDQogICAgICAgICAgICAgICAgIm1hc2tlZEFjY051bWJlciI6ICJVQkk4NTIxNzg4MTI3OSINCiAgICAgICAgICAgIH0sDQogICAgICAgICAgICB7DQogICAgICAgICAgICAgICAgImZpVHlwZSI6ICJERVBPU0lUIiwNCiAgICAgICAgICAgICAgICAiZmlwSWQiOiAiQkFSQjBLSU1YWFgiLA0KICAgICAgICAgICAgICAgICJhY2NUeXBlIjogIlNBVklOR1MiLA0KICAgICAgICAgICAgICAgICJsaW5rUmVmTnVtYmVyIjogIlVCSTQ4NTk2NDUiLA0KICAgICAgICAgICAgICAgICJtYXNrZWRBY2NOdW1iZXIiOiAiVUJJODUyMTc4ODEyIg0KICAgICAgICAgICAgfQ0KICAgICAgICBdLA0KICAgICAgICAiUHVycG9zZSI6IHsNCiAgICAgICAgICAgICJjb2RlIjogIjEwMSIsDQogICAgICAgICAgICAicmVmVXJpIjogImh0dHBzOi8vYXBpLnJlYml0Lm9yZy5pbi9hYS9wdXJwb3NlLzEwMS54bWwiLA0KICAgICAgICAgICAgInRleHQiOiAiV2VhbHRoIG1hbmFnZW1lbnQgc2VydmljZSIsDQogICAgICAgICAgICAiQ2F0ZWdvcnkiOiB7DQogICAgICAgICAgICAgICAgInR5cGUiOiAicHVycG9zZUNhdGVnb3J5VHlwZSINCiAgICAgICAgICAgIH0NCiAgICAgICAgfSwNCiAgICAgICAgIkZJRGF0YVJhbmdlIjogew0KICAgICAgICAgICAgImZyb20iOiAiMjAxOS0wNS0yOFQxMTozODoyMC4zODMrMDAwMCIsDQogICAgICAgICAgICAidG8iOiAiMjAyMC0wNS0yOFQxMTozODoyMC4zODErMDAwMCINCiAgICAgICAgfSwNCiAgICAgICAgIkRhdGFMaWZlIjogew0KICAgICAgICAgICAgInVuaXQiOiAiTU9OVEgiLA0KICAgICAgICAgICAgInZhbHVlIjogNA0KICAgICAgICB9LA0KICAgICAgICAiRnJlcXVlbmN5Ijogew0KICAgICAgICAgICAgInVuaXQiOiAiSE9VUiIsDQogICAgICAgICAgICAidmFsdWUiOiA0DQogICAgICAgIH0sDQogICAgICAgICJEYXRhRmlsdGVyIjogWw0KICAgICAgICAgICAgew0KICAgICAgICAgICAgICAgICJ0eXBlIjogIlRSQU5TQUNUSU9OQU1PVU5UIiwNCiAgICAgICAgICAgICAgICAib3BlcmF0b3IiOiAiPiIsDQogICAgICAgICAgICAgICAgInZhbHVlIjogIjIwMDAwIg0KICAgICAgICAgICAgfQ0KICAgICAgICBdDQogICAgfQ.O3KPh-eTpW2w47QXYidOBe1Hk2y7djVAEcOnZyRRvxQ3cY18-9ZWiodF16jff-e7yNQgsYZpAy95Fx2Fft8LoYugkYh9_6qHiG_7LCtW8Ng4nCMgZM3Wwsj11ks1msrK5C1ksPrGlTkFhm9-FufNkPTAlW76_5Sb8G_lOsIj1lB8TrvKpOvPlhEIgsS4WBNdPfv3SBqTV2suw2LvkX3QTilqwuMgXMkrm9-RYL90fweX_yyoyaBWHOJNQaKNuQWPpoRRNHGOx3v4_QiwgrELdfeTVtKn6R_AsfaBoEthQ3wrc8tY1q0Wx5j0x18NdU2R2C26dHyZ9M11dEH99psA1A");
        consentArtefact.setStatus("ACTIVE");
        consentArtefact.setConsentId(consentId);
        return consentArtefact;
    }

    public static FIUFIRequest createDataRequest(){
        FIUFIRequest fiufiRequest = new FIUFIRequest();
        fiufiRequest.setVer("1.1.2");
        fiufiRequest.setTxnid(txnId);
        fiufiRequest.setTimestamp(timestamp);
        Consent consent = new Consent();
        consent.setId(consentId);
        consent.setDigitalSignature("consentDigitalSignature");
        fiufiRequest.setConsent(consent);
        fiufiRequest.setKeyMaterial(getSerializedPair().getKeyMaterial());
        fiufiRequest.setFIDataRange(getFinvuConsentRequest().getConsentDetail().getFIDataRange());
        return fiufiRequest;
    }

    public static FIUFIRequest createDataRequest1(){
        FIUFIRequest fiufiRequest = new FIUFIRequest();
        fiufiRequest.setVer("1.1.0");
        fiufiRequest.setTxnid(txnId);
        fiufiRequest.setTimestamp(timestamp);
        Consent consent = new Consent();
        consent.setId(consentId);
        consent.setDigitalSignature("consentDigitalSignature");
        Callback callback = new Callback();
        callback.setUrl("String URI");
        fiufiRequest.setCallback(callback);
        fiufiRequest.setConsent(consent);
        fiufiRequest.setKeyMaterial(getSerializedPair().getKeyMaterial());
        fiufiRequest.setFIDataRange(getFinvuConsentRequest().getConsentDetail().getFIDataRange());
        return fiufiRequest;
    }

    public static FIUFIRequest createDataRequest2(){
        FIUFIRequest fiufiRequest = new FIUFIRequest();
        fiufiRequest.setVer("1.1.1");
        fiufiRequest.setTxnid(txnId);
        fiufiRequest.setTimestamp(timestamp);
        Consent consent = new Consent();
        consent.setId(consentId);
        consent.setDigitalSignature("consentDigitalSignature");
        Callback callback = new Callback();
        callback.setUrl("String URI");
        fiufiRequest.setCallback(callback);
        fiufiRequest.setConsent(consent);
        fiufiRequest.setKeyMaterial(getSerializedPair().getKeyMaterial());
        fiufiRequest.setFIDataRange(getFinvuConsentRequest().getConsentDetail().getFIDataRange());
        return fiufiRequest;
    }

    public static FIRequestResponse createDataResponse(){
        FIRequestResponse fiRequestResponse = new FIRequestResponse();
        fiRequestResponse.setVer("1.1.2");
        fiRequestResponse.setTxnid(txnId);
        fiRequestResponse.setTimestamp(timestamp);
        fiRequestResponse.setConsentId(consentId);
        fiRequestResponse.setSessionId(sessionId);
        return fiRequestResponse;
    }

    public static io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse FIUFetchDecryptResponse(){
        io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse fiFetchResponse = new io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse();
        fiFetchResponse.setVer("1.1.2");
        fiFetchResponse.setTxnid(txnId);
        fiFetchResponse.setTimestamp(timestamp);
        DecryptedFI decryptedFI = new DecryptedFI();
        DecryptedDatum decryptedDatum = new DecryptedDatum();
        decryptedFI.setFipID("FIP-1");
        decryptedDatum.setDecryptedFI("DecryptedFI");
        decryptedDatum.setMaskedAccNumber("MaskedAccNumber");
        decryptedDatum.setLinkRefNumber(LinkRefNUmber);
        decryptedFI.setDecryptedDatum(Collections.singletonList(decryptedDatum));
        fiFetchResponse.setDecryptedFI(Collections.singletonList(decryptedFI));
        return fiFetchResponse;
    }

    public static FIFetchResponse FIUFetchResponse(){
        String EncryptedFI = "Encrypted FI";
        String MaskedAccNumber = "MaskedAccNumber";
        FIFetchResponse fiFetchResponse = new FIFetchResponse();
        fiFetchResponse.setVer("1.1.2");
        fiFetchResponse.setTxnid(txnId);
        fiFetchResponse.setTimestamp(timestamp);
        FI fi = new FI();
        fi.setFipID("FIP-1");
        fi.setKeyMaterial(getSerializedPair().getKeyMaterial());
        Datum datum = new Datum();
        datum.setLinkRefNumber(LinkRefNUmber);
        datum.setEncryptedFI(EncryptedFI);
        datum.setMaskedAccNumber(MaskedAccNumber);
        fi.setData(Collections.singletonList(datum));
        fiFetchResponse.setFi(Collections.singletonList(fi));
        return fiFetchResponse;
    }


    public static FetchDataRequest getDataRequest() throws JsonProcessingException {
        FetchDataRequest dataRequest = new FetchDataRequest();
        ObjectMapper mapper = new ObjectMapper();
        dataRequest = mapper.readValue(fetchDataRequest,FetchDataRequest.class);
        return dataRequest;
    }

    public static ConsentNotification getConsentNotification(){
        ConsentNotification consentNotification = new ConsentNotification();
        consentNotification.setVer("1.1.2");
        consentNotification.setTimestamp(timestamp);
        consentNotification.setTxnid(txnId);
        Notifier notifier = new Notifier();
        notifier.setId("FIP-1");
        notifier.setType("FIP");
        consentNotification.setNotifier(notifier);
        ConsentStatusNotification consentStatusNotification = new ConsentStatusNotification();
        consentStatusNotification.setConsentId(consentId);
        consentStatusNotification.setConsentHandle(consentHandle);
        consentStatusNotification.setConsentStatus("ACTIVE");
        consentNotification.setConsentStatusNotification(consentStatusNotification);
        return consentNotification;
    }

    public static ConsentNotification getConsentNotifiCallback(){
        ConsentNotification consentNotification = new ConsentNotification();
        consentNotification.setVer("1.1.1");
        consentNotification.setTimestamp(timestamp);
        consentNotification.setTxnid(txnId);
        Notifier notifier = new Notifier();
        notifier.setId("FIP-2");
        notifier.setType("FIP");
        consentNotification.setNotifier(notifier);
        ConsentStatusNotification consentStatusNotification = new ConsentStatusNotification();
        consentStatusNotification.setConsentId(consentId);
        consentStatusNotification.setConsentHandle(consentHandle);
        consentStatusNotification.setConsentStatus("ACTIVE");
        consentNotification.setConsentStatusNotification(consentStatusNotification);
        return consentNotification;
    }

    public static NotificationResponse getNotificationResponse(){
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setVer("1.0");
        notificationResponse.setTxnId(txnId);
        notificationResponse.setTimestamp(timestamp);
        notificationResponse.getResponse();
        return notificationResponse;
    }

    public static FINotification fiNotificationRequest(){
        FINotification fiNotification = new FINotification();
        fiNotification.setVer("1.0");
        fiNotification.setTxnid(txnId);
        fiNotification.setTimestamp(timestamp);
        FIStatusNotification fiStatusNotification = new FIStatusNotification();
        fiStatusNotification.setSessionId(sessionId);
        fiStatusNotification.setSessionStatus("ACTIVE");
        FIStatusResponse fiStatusResponse = new FIStatusResponse();
        Account account = new Account();
        account.setLinkRefNumber(LinkRefNUmber);
        account.setFiStatus("ACTIVE");
        account.setDescription("");
        fiStatusResponse.setFipID("FIP-1");
        fiStatusResponse.setAccounts(Collections.singletonList(account));
        fiStatusNotification.setFiStatusResponse(Collections.singletonList(fiStatusResponse));
        fiNotification.setFIStatusNotification(fiStatusNotification);
        Notifier notifier = new Notifier();
        notifier.setId("FIP-1");
        notifier.setType("FIP");
        fiNotification.setNotifier(notifier);
        return fiNotification;
    }

    public static FINotification fiNotificationCallBack(){
        FINotification fiNotification = new FINotification();
        fiNotification.setVer("1.0");
        fiNotification.setTxnid(txnId);
        fiNotification.setTimestamp(timestamp);
        FIStatusNotification fiStatusNotification = new FIStatusNotification();
        fiStatusNotification.setSessionId(sessionId);
        fiStatusNotification.setSessionStatus("ACTIVE");
        FIStatusResponse fiStatusResponse = new FIStatusResponse();
        Account account = new Account();
        account.setLinkRefNumber(LinkRefNUmber);
        account.setFiStatus("ACTIVE");
        account.setDescription("");
        fiStatusResponse.setFipID("FIP-2");
        fiStatusResponse.setAccounts(Collections.singletonList(account));
        fiStatusNotification.setFiStatusResponse(Collections.singletonList(fiStatusResponse));
        fiNotification.setFIStatusNotification(fiStatusNotification);
        Notifier notifier = new Notifier();
        notifier.setId("FIP-1");
        notifier.setType("FIP");
        fiNotification.setNotifier(notifier);
        return fiNotification;
    }

    public static ConsentNotificationLog getConsentNotificationLog(ConsentNotification body){
        ConsentNotificationLog consentNotificationLog = new ConsentNotificationLog();
        consentNotificationLog.setVersion(body.getVer());
        consentNotificationLog.setTxnId(body.getTxnid());
        consentNotificationLog.setNotificationTimestamp(io.finarkein.api.aa.util.Functions.strToTimeStamp.apply(body.getTimestamp()));
        consentNotificationLog.setNotifierType(body.getNotifier().getType());
        consentNotificationLog.setNotifierId(body.getNotifier().getId());
        consentNotificationLog.setConsentHandle(body.getConsentStatusNotification().getConsentHandle());
        consentNotificationLog.setConsentId(body.getConsentStatusNotification().getConsentId());
        consentNotificationLog.setConsentState(body.getConsentStatusNotification().getConsentStatus());
        return consentNotificationLog;
    }
}
