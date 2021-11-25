/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa.easy;

import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentStatusNotification;
import io.finarkein.fiul.dataflow.dto.FIDataHeader;
import io.finarkein.fiul.dataflow.easy.DataSaveRequest;
import io.finarkein.fiul.dataflow.easy.crypto.CipheredFIData;
import io.finarkein.fiul.dataflow.easy.crypto.DecipheredDataRecord;
import io.finarkein.fiul.dataflow.easy.crypto.DecipheredFIData;
import io.finarkein.fiul.dataflow.easy.crypto.FIDataCryptoService;
import io.finarkein.fiul.dataflow.easy.dto.FIDataRecord;
import io.finarkein.fiul.dataflow.easy.dto.FIDataRecordDataKey;
import io.finarkein.fiul.dataflow.easy.dto.KeyMaterialDataKey;
import io.finarkein.fiul.dataflow.jpa.RepoFIDataHeader;
import io.finarkein.fiul.dataflow.response.decrypt.DecryptedDatum;
import io.finarkein.fiul.dataflow.response.decrypt.DecryptedFI;
import io.finarkein.fiul.dataflow.response.decrypt.FIFetchResponse;
import io.finarkein.fiul.dataflow.store.EasyFIDataStore;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static io.finarkein.api.aa.util.Functions.*;
import static io.finarkein.fiul.dataflow.easy.crypto.FIDataCryptoService.SERVICE_NAME_PROPERTY;

@Log4j2
@Service
public class EasyFIDataStoreImpl implements EasyFIDataStore {

    @Autowired
    FIDataCryptoService cryptoService;

    @Autowired
    RepoFIDataHeader repoDataHeader;

    @Autowired
    RepoFIDataRecord repoFIDataRecord;

    @Autowired
    RepoFIDataRecordDataKey repoFIDataRecordDataKey;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RepoKeyStorageEntry repoKeyStorage;

    @Value("${" + SERVICE_NAME_PROPERTY + "}")
    private String cryptServiceName;

    @PostConstruct
    void postConstruct() {
        log.info("FIDataCryptoService name:'{}'", cryptServiceName);
    }

    @Override
    public void saveKey(KeyMaterialDataKey entry) {
        repoKeyStorage.save(entry);
    }

    @Override
    public Optional<KeyMaterialDataKey> getKey(String consentId, String sessionId) {
        return repoKeyStorage.findById(new KeyMaterialDataKey.Key(consentId, sessionId));
    }

    @Override
    public void deleteKey(String consentId, String sessionId) {
        repoKeyStorage.deleteById(new KeyMaterialDataKey.Key(consentId, sessionId));
    }

    @Override
    public void saveFIData(DataSaveRequest<FIFetchResponse> dataSaveRequest) {
        final var aaName = dataSaveRequest.getAaName();
        final var consentId = dataSaveRequest.getConsentId();
        final var sessionId = dataSaveRequest.getSessionId();
        final var fiFetchResponse = dataSaveRequest.getFiFetchResponse();
        final var dataLife = dataSaveRequest.getDataLife();
        final var dataLifeExpirationOn = dataSaveRequest.getDataLifeExpireOn();

        final var dataHeader = FIDataHeader.builder()
                .version(fiFetchResponse.getVer())
                .txnId(fiFetchResponse.getTxnid())
                .consentId(consentId)
                .sessionId(sessionId)
                .aaName(aaName)
                .dataLifeUnit(dataLife.getUnit())
                .dataLifeValue(dataLife.getValue())
                .dataLifeExpireOn(dataLifeExpirationOn)
                .build();

        long start = System.currentTimeMillis();
        CipheredFIData cipheredFIData;
        try {
            cipheredFIData = cryptoService.encrypt(fiFetchResponse);
            if (log.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                log.debug("Time to encrypt FI-Data [{},{},{}] :{} ms", aaName, consentId, sessionId, (end - start));
            }
        } catch (Exception e) {
            log.error("Error while encrypting FI-data, consentId:{}, sessionId:{}, error:{}", consentId, sessionId, e.getMessage());
            throw e;
        }

        start = System.currentTimeMillis();
        List<FIDataRecord> dataRecords = new ArrayList<>();
        List<FIDataRecordDataKey> dataKeys = new ArrayList<>();
        cipheredFIData.getRecords()
                .forEach(encryptedDataRecord -> {
                    final var compressedData = gzipCompression.apply(encryptedDataRecord.getFiData());
                    dataRecords.add(FIDataRecord.builder()
                            .consentId(consentId)
                            .sessionId(sessionId)
                            .aaName(aaName)
                            .fipId(encryptedDataRecord.getFipId())
                            .linkRefNumber(encryptedDataRecord.getLinkRefNumber())
                            .maskedAccNumber(encryptedDataRecord.getMaskedAccNumber())
                            .fiData(compressedData)
                            .dataLifeExpireOn(dataLifeExpirationOn)
                            .build());
                    dataKeys.add(FIDataRecordDataKey.builder()
                            .consentId(consentId)
                            .sessionId(sessionId)
                            .fipId(encryptedDataRecord.getFipId())
                            .encryptedDataKey(encryptedDataRecord.getEncryptedDataKey())
                            .dataLifeExpireOn(dataLifeExpirationOn)
                            .build());
                });

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            repoDataHeader.save(dataHeader);
            repoFIDataRecord.saveAll(dataRecords);
            repoFIDataRecordDataKey.saveAll(dataKeys);
        });

        if (log.isDebugEnabled()) {
            long end = System.currentTimeMillis();
            log.debug("Time to store data [{},{},{}] :{} ms", aaName, consentId, sessionId, (end - start));
        }
    }

    @Override
    public Optional<FIFetchResponse> getFIData(String consentId, String sessionId) {
        final var dataHeaderOptional = repoDataHeader.findById(new FIDataHeader.Key(consentId, sessionId));
        if (dataHeaderOptional.isEmpty())
            throw Errors.NoDataFound.with(uuidSupplier.get(), "No data found for consentId:'" + consentId + "', sessionId:'" + sessionId + "'");
        final var fiDataHeader = dataHeaderOptional.get();

        final var fiDataRecords = retrieveDataRecords(fiDataHeader);
        if (fiDataRecords.isEmpty())
            return Optional.empty();

        final var cipheredFIData = cipheredFIDataConverter.apply(fiDataHeader, fiDataRecords);

        long start = System.currentTimeMillis();
        DecipheredFIData decipheredFIData;
        try {
            decipheredFIData = cryptoService.decrypt(cipheredFIData);
            if (log.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                log.debug("Time to decrypt FI-Data [{},{}] :{} ms", consentId, sessionId, (end - start));
            }
        } catch (Exception e) {
            log.error("Error while decrypting FI-Data, consentId:{}, sessionId:{}, error:{}", consentId, sessionId, e.getMessage());
            throw e;
        }

        final List<DecryptedFI> decryptedFIList = createDecryptedFI(decipheredFIData);
        var response = new FIFetchResponse();
        response.setVer(fiDataHeader.getVersion());
        response.setTxnid(fiDataHeader.getTxnId());
        response.setTimestamp(timestampToStr.apply(Timestamp.from(Instant.now())));
        response.setFipData(decryptedFIList);
        return Optional.of(response);
    }

    public void handleConsentRevoked(ConsentStatusNotification statusNotification) {
        final var deletionCounts = deleteFIDataByConsentId(statusNotification.getConsentId());
        if (log.isDebugEnabled())
            log.debug("HandleConsentRevoked consentStatus:{}, deletionCounts:{}", statusNotification, deletionCounts);
    }

    @Override
    public Map<String, Integer> deleteFIDataByConsentId(String consentId) {
        Map<String, Integer> deletionCounts = new HashMap<>(8);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            var rowsDeleted = repoDataHeader.deleteByConsentId(consentId);
            if (rowsDeleted > 0)
                deletionCounts.put("DataHeader", rowsDeleted);
            rowsDeleted = repoFIDataRecord.deleteByConsentId(consentId);
            if (rowsDeleted > 0)
                deletionCounts.put("DataRecord", rowsDeleted);

            rowsDeleted = repoFIDataRecordDataKey.deleteByConsentId(consentId);
            if (rowsDeleted > 0)
                deletionCounts.put("DataRecordDataKey", rowsDeleted);
        });
        return deletionCounts;
    }

    @Override
    public Map<String, Integer> deleteFIDataByConsentIdAndSessionId(String consentId, String sessionId) {
        Map<String, Integer> deletionCounts = new HashMap<>(8);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            var rowsDeleted = repoDataHeader.deleteByConsentIdAndSessionId(consentId, sessionId);
            if (rowsDeleted > 0)
                deletionCounts.put("DataHeader", rowsDeleted);
            rowsDeleted = repoFIDataRecord.deleteByConsentIdAndSessionId(consentId, sessionId);
            if (rowsDeleted > 0)
                deletionCounts.put("DataRecord", rowsDeleted);

            rowsDeleted = repoFIDataRecordDataKey.deleteByConsentIdAndSessionId(consentId, sessionId);
            if (rowsDeleted > 0)
                deletionCounts.put("DataRecordDataKey", rowsDeleted);
        });
        return deletionCounts;
    }

    @Override
    public Map<String, Integer> deleteByDataLifeExpireOnBefore(Timestamp triggerTimestamp) {
        Map<String, Integer> deletionCounts = new HashMap<>(8);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            var rowsDeleted = repoDataHeader.deleteByDataLifeExpireOnBefore(triggerTimestamp);
            if (rowsDeleted > 0)
                deletionCounts.put("DataHeader", rowsDeleted);
            rowsDeleted = repoFIDataRecord.deleteByDataLifeExpireOnBefore(triggerTimestamp);
            if (rowsDeleted > 0)
                deletionCounts.put("DataRecord", rowsDeleted);

            rowsDeleted = repoFIDataRecordDataKey.deleteByDataLifeExpireOnBefore(triggerTimestamp);
            if (rowsDeleted > 0)
                deletionCounts.put("DataRecordDataKey", rowsDeleted);
        });
        return deletionCounts;
    }

    public void handleConsentExpired(ConsentStatusNotification statusNotification) {
        final var deletionCounts = deleteFIDataByConsentId(statusNotification.getConsentId());
        if (log.isDebugEnabled() && !deletionCounts.isEmpty())
            log.debug("HandleConsentExpired consentStatus:{}, deletionCounts:{}", statusNotification, deletionCounts);
    }

    private List<DecryptedFI> createDecryptedFI(DecipheredFIData decipheredFIData) {
        return decipheredFIData.getRecords()
                .stream()
                .collect(Collectors.groupingBy(DecipheredDataRecord::getFipId, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> {
                    final var decryptedFI = new DecryptedFI();
                    decryptedFI.setFipId(entry.getKey());
                    decryptedFI.setAccounts(entry.getValue().stream()
                            .map(decipheredDataRecord -> {
                                final var datum = new DecryptedDatum();
                                datum.setLinkRefNumber(decipheredDataRecord.getLinkRefNumber());
                                datum.setMaskedAccNumber(decipheredDataRecord.getMaskedAccNumber());
                                datum.setAccountData(decipheredDataRecord.getData());
                                return datum;
                            }).collect(Collectors.toList()));
                    return decryptedFI;
                }).collect(Collectors.toList());
    }

    private List<FIDataRecord> retrieveDataRecords(FIDataHeader dataHeader) {
        final Example<FIDataRecord> example = Example.of(FIDataRecord.builder()
                .consentId(dataHeader.getConsentId())
                .sessionId(dataHeader.getSessionId())
                .aaName(dataHeader.getAaName())
                .build());

        return repoFIDataRecord.findAll(example);
    }

    BiFunction<FIDataHeader, List<FIDataRecord>, CipheredFIData> cipheredFIDataConverter = (fiDataHeader, fiDataRecords) -> {
        final var cipheredFIDataBuilder = CipheredFIData.builder();
        final var keyBuilder = FIDataRecordDataKey.Key.builder().consentId(fiDataHeader.getConsentId()).sessionId(fiDataHeader.getSessionId());

        fiDataRecords.forEach(fiDataRecord -> {
            final var dataKeyOptional = repoFIDataRecordDataKey.findById(keyBuilder.fipId(fiDataRecord.getFipId()).build());
            if (dataKeyOptional.isEmpty())
                throw Errors.InternalError.with(uuidSupplier.get(),
                        "DataKey not found for consentId:'" + fiDataHeader.getConsentId() + "', sessionId:'" + fiDataHeader.getConsentId() + "', fipId:'" + fiDataRecord.getFipId() + "'");
            cipheredFIDataBuilder.addRecord(fiDataRecord.getFipId(),
                    fiDataRecord.getLinkRefNumber(),
                    fiDataRecord.getMaskedAccNumber(),
                    gzipDecompression.apply(fiDataRecord.getFiData()),
                    dataKeyOptional.get().getEncryptedDataKey());
        });
        return cipheredFIDataBuilder.build();
    };
}
