/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa.std;

import io.finarkein.api.aa.dataflow.response.Datum;
import io.finarkein.api.aa.dataflow.response.FI;
import io.finarkein.api.aa.dataflow.response.FIFetchResponse;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.fiul.dataflow.dto.AAFIDatum;
import io.finarkein.fiul.dataflow.dto.FIDataHeader;
import io.finarkein.fiul.dataflow.easy.DataSaveRequest;
import io.finarkein.fiul.dataflow.jpa.RepoFIDataHeader;
import io.finarkein.fiul.dataflow.store.AAFIDataStore;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static io.finarkein.api.aa.util.Functions.timestampToStr;
import static io.finarkein.api.aa.util.Functions.uuidSupplier;

@Log4j2
@Service
public class AAFIDataStoreImpl implements AAFIDataStore {

    @Autowired
    private RepoFIDataHeader repoDataHeader;

    @Autowired
    private RepoAAFIDataStore repoAAFiDataStore;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public void saveFIData(DataSaveRequest<FIFetchResponse> request) {
        final var consentId = request.getConsentId();
        final var sessionId = request.getSessionId();
        final var aaName = request.getAaName();
        final var dataLife = request.getDataLife();
        final var fiFetchResponse = request.getFiFetchResponse();
        final var dataLifeExpirationOn = request.getDataLifeExpireOn();

        long start = System.currentTimeMillis();
        final var dataHeader = FIDataHeader.builder()
                .version(fiFetchResponse.getVer())
                .txnId(fiFetchResponse.getTxnid())
//                .timestamp(toTimestamp.apply(fiFetchResponse.getTimestamp()))
                .consentId(consentId)
                .sessionId(sessionId)
                .aaName(aaName)
                .dataLifeUnit(dataLife.getUnit())
                .dataLifeValue(dataLife.getValue())
                .dataLifeExpireOn(dataLifeExpirationOn)
                .build();

        final var fiDataList = prepareFIDataList(request);

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            repoDataHeader.save(dataHeader);
            repoAAFiDataStore.saveAll(fiDataList);
        });

        if (log.isDebugEnabled()) {
            long end = System.currentTimeMillis();
            log.debug("Time to store data [{},{},{}] :{} ms", aaName, consentId, sessionId, (end - start));
        }
    }

    @Override
    public Map<String, Integer> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp) {
        Map<String, Integer> deletionCounts = new HashMap<>(8);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            var rowsDeleted = repoDataHeader.deleteByDataLifeExpireOnBefore(triggerTimestamp);
            if (rowsDeleted > 0)
                deletionCounts.put("DataHeader", rowsDeleted);
            rowsDeleted = repoAAFiDataStore.deleteByDataLifeExpireOnBefore(triggerTimestamp);
            if (rowsDeleted > 0)
                deletionCounts.put("AAFIDataStore", rowsDeleted);
        });
        return deletionCounts;
    }

    @Override
    public Map<String, Integer> deleteFIDataByConsentId(String consentId) {
        Map<String, Integer> deletionCounts = new HashMap<>(8);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            var rowsDeleted = repoDataHeader.deleteByConsentId(consentId);
            if (rowsDeleted > 0)
                deletionCounts.put("DataHeader", rowsDeleted);
            rowsDeleted = repoAAFiDataStore.deleteByConsentId(consentId);
            if (rowsDeleted > 0)
                deletionCounts.put("AAFIDataStore", rowsDeleted);
        });
        return deletionCounts;
    }

    @Override
    public Map<String, Integer> deleteFIDataBySessionId(String sessionId) {
        Map<String, Integer> deletionCounts = new HashMap<>(8);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            var rowsDeleted = repoDataHeader.deleteBySessionId(sessionId);
            if (rowsDeleted > 0)
                deletionCounts.put("DataHeader", rowsDeleted);
            rowsDeleted = repoAAFiDataStore.deleteBySessionId(sessionId);
            if (rowsDeleted > 0)
                deletionCounts.put("AAFIDataStore", rowsDeleted);
        });
        return deletionCounts;
    }

    private List<AAFIDatum> prepareFIDataList(DataSaveRequest<FIFetchResponse> request) {
        List<AAFIDatum> fiDataList = new ArrayList<>();
        request.getFiFetchResponse().getFi()
                .stream()
                .flatMap(fi -> {
                    final var builder = AAFIDatum.builder()
                            .consentId(request.getConsentId())
                            .sessionId(request.getSessionId())
                            .aaName(request.getAaName())
                            .fipId(fi.getFipID())
                            .keyMaterial(fi.getKeyMaterial())
                            .dataLifeExpireOn(request.getDataLifeExpireOn());
                    return fi.getData()
                            .stream()
                            .map(datum -> builder
                                    .linkRefNumber(datum.getLinkRefNumber())
                                    .maskedAccNumber(datum.getMaskedAccNumber())
                                    .fiData(datum.getEncryptedFI())
                                    .build());
                }).forEach(fiDataList::add);
        return fiDataList;
    }

    @Override
    public FIFetchResponse getFIData(String consentId, String sessionId, String aaName, String fipId, String[] linkRefNumber) {
        final var dataHeaderOptional = repoDataHeader.findById(new FIDataHeader.Key(consentId, sessionId));
        if (dataHeaderOptional.isEmpty())
            throw Errors.NoDataFound.with(uuidSupplier.get(), "No data found for consentId:" + consentId + ", sessionId:" + sessionId);

        final var dataHeader = dataHeaderOptional.get();
        final var response = prepareFIFetchResponse(dataHeader);
        final var datumList = retrieveFIDatum(dataHeader, fipId, linkRefNumber);
        if (datumList.isEmpty())
            return response;
        response.setFi(prepareFIList(datumList));
        return response;
    }

    private List<FI> prepareFIList(List<AAFIDatum> datumList) {
        return datumList.stream()
                .collect(Collectors.groupingBy(AAFIDatum::getFipId, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> {
                    final var fi = new FI();
                    fi.setFipID(entry.getKey());
                    fi.setData(entry.getValue().stream()
                            .map(aaFIDatum -> {
                                if (fi.getKeyMaterial() == null)
                                    fi.setKeyMaterial(aaFIDatum.getKeyMaterial());
                                return Datum.builder()
                                        .linkRefNumber(aaFIDatum.getLinkRefNumber())
                                        .maskedAccNumber(aaFIDatum.getMaskedAccNumber())
                                        .encryptedFI(new String(aaFIDatum.getFiData()))
                                        .build();
                            }).collect(Collectors.toList()));
                    return fi;
                }).collect(Collectors.toList());
    }

    private FIFetchResponse prepareFIFetchResponse(FIDataHeader dataHeader) {
        var response = new FIFetchResponse();
        response.setTxnid(dataHeader.getTxnId());
        response.setTimestamp(timestampToStr.apply(Timestamp.from(Instant.now())));
        response.setVer(dataHeader.getVersion());
        response.setFi(Collections.emptyList());
        return response;
    }

    private List<AAFIDatum> retrieveFIDatum(FIDataHeader dataHeader, String fipId, String[] linkRefNumber) {
        AAFIDatum.Builder builder = null;
        if (fipId != null && linkRefNumber != null && linkRefNumber.length > 0) {
            return repoAAFiDataStore.getByLinkRefNumbers(dataHeader.getConsentId(), dataHeader.getSessionId(),
                    dataHeader.getAaName(), fipId, Arrays.asList(linkRefNumber));
        }
        builder = AAFIDatum.builder()
                .consentId(dataHeader.getConsentId())
                .sessionId(dataHeader.getSessionId())
                .aaName(dataHeader.getAaName())
                .fipId(fipId);
        return repoAAFiDataStore.findAll(Example.of(builder.build()));
    }
}
