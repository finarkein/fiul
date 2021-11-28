/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul;

import io.finarkein.api.aa.consent.*;
import io.finarkein.fiul.consent.model.*;
import io.finarkein.fiul.consent.repo.ConsentNotificationLogRepository;
import io.finarkein.fiul.consent.repo.ConsentRequestDTORepository;
import io.finarkein.fiul.consent.repo.ConsentStateRepository;
import io.finarkein.fiul.consent.repo.ConsentTemplateRepository;
import io.finarkein.fiul.notification.callback.RepoConsentCallback;
import io.finarkein.fiul.notification.callback.RepoFICallback;
import io.finarkein.fiul.notification.callback.model.ConsentCallback;
import io.finarkein.fiul.notification.callback.model.FICallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Optional;

import static io.finarkein.api.aa.util.Functions.*;

@SpringBootTest(classes = FiulServerApplication.class)
@Disabled
class RepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RepoConsentCallback repoConsentCallback;

    @Autowired
    private RepoFICallback repoFICallback;

    @Autowired
    private ConsentNotificationLogRepository consentNotificationLogRepository;

    @Autowired
    private ConsentRequestDTORepository consentRequestDTORepository;

    @Autowired
    private ConsentStateRepository consentStateRepository;

    @Autowired
    private ConsentTemplateRepository consentTemplateRepository;

    @Test
    @DisplayName("Test adding ConsentCallback in database and fetching it.")
    void createReadConsentCallBackRepoTest() {
        ConsentCallback consentCallback = new ConsentCallback();
        consentCallback.setCallbackUrl("URL");
        consentCallback.setConsentHandleId("consentId");
        ConsentCallback consentCallbackRepo = repoConsentCallback.save(consentCallback);
        Assertions.assertTrue(repoConsentCallback.findById("consentId").isPresent());
        Assertions.assertSame(1, repoConsentCallback.findAll().size());
        Assertions.assertEquals(consentCallbackRepo.getConsentHandleId(), repoConsentCallback.findById("consentId").get().getConsentHandleId());
        Assertions.assertEquals(consentCallbackRepo.getCallbackUrl(), repoConsentCallback.findById("consentId").get().getCallbackUrl());
    }

    @Test
    @DisplayName("Test updating ConsentCallback in database.")
    void updateConsentCallBackRepoTest() {
        ConsentCallback consentCallback = new ConsentCallback();
        consentCallback.setCallbackUrl("URL");
        consentCallback.setConsentHandleId("consentId");
        repoConsentCallback.save(consentCallback);
        Assertions.assertTrue(repoConsentCallback.findById("consentId").isPresent());
        ConsentCallback updateCallBack = repoConsentCallback.findById("consentId").get();
        updateCallBack.setCallbackUrl("UpdatedURL");
        repoConsentCallback.save(updateCallBack);
        Assertions.assertEquals(updateCallBack.getCallbackUrl(), repoConsentCallback.findById("consentId").get().getCallbackUrl());
    }

    @Test
    @DisplayName("Test deleting ConsentCallback in database.")
    void deleteConsentCallBackRepoTest() {
        ConsentCallback consentCallback = new ConsentCallback();
        consentCallback.setCallbackUrl("URL");
        consentCallback.setConsentHandleId("consentId");
        repoConsentCallback.save(consentCallback);
        Assertions.assertTrue(repoConsentCallback.findById("consentId").isPresent());
        ConsentCallback deleteCallBack = repoConsentCallback.findById("consentId").get();
        repoConsentCallback.delete(deleteCallBack);
        Optional<ConsentCallback> optionalConsentCallback = repoConsentCallback.findById("consentId");
        Assertions.assertTrue(optionalConsentCallback.isEmpty());
    }

    private FICallback createFICallback() {
        FICallback fiCallback = new FICallback();
        fiCallback.setCallbackUrl("URL");
        fiCallback.setSessionId("sessionId");
        fiCallback.setConsentId("consentId");
        return fiCallback;
    }

    @Test
    @DisplayName("Test adding FICallback in database and fetching it.")
    void fiCallBackRepoTest() {
        FICallback fiCallbackRepo = repoFICallback.save(createFICallback());

        Assertions.assertTrue(repoFICallback.findById("sessionId").isPresent());
        Assertions.assertSame(1, repoFICallback.findAll().size());
        Assertions.assertEquals(fiCallbackRepo.getConsentId(), repoFICallback.findById("sessionId").get().getConsentId());
        Assertions.assertEquals(fiCallbackRepo.getSessionId(), repoFICallback.findById("sessionId").get().getSessionId());
        Assertions.assertEquals(fiCallbackRepo.getCallbackUrl(), repoFICallback.findById("sessionId").get().getCallbackUrl());
    }

    @Test
    @DisplayName("Test updating FICallback in database.")
    void updateFICallbackRepoTest() {
        repoFICallback.save(createFICallback());
        Assertions.assertTrue(repoFICallback.findById("sessionId").isPresent());
        FICallback updateFiCallBack = repoFICallback.findById("sessionId").get();
        updateFiCallBack.setCallbackUrl("UpdatedURL");
        updateFiCallBack.setConsentId("UpdatedConsentId");
        repoFICallback.save(updateFiCallBack);
        Assertions.assertEquals(updateFiCallBack.getCallbackUrl(), repoFICallback.findById("sessionId").get().getCallbackUrl());
        Assertions.assertEquals(updateFiCallBack.getConsentId(), repoFICallback.findById("sessionId").get().getConsentId());
    }

    @Test
    @DisplayName("Test deleting FICallback in database.")
    void deleteFICallBackRepoTest() {
        repoFICallback.save(createFICallback());
        Assertions.assertTrue(repoFICallback.findById("sessionId").isPresent());
        FICallback deleteCallBack = repoFICallback.findById("sessionId").get();
        repoFICallback.delete(deleteCallBack);
        Optional<FICallback> optionalFICallback = repoFICallback.findById("sessionId");
        Assertions.assertTrue(optionalFICallback.isEmpty());
    }

    @Test
    @DisplayName("Test adding ConsentNotificationLog in database and fetching it.")
    void saveConsentNotificationLogTest() {
        ConsentNotificationLog consentNotificationLog = new ConsentNotificationLog();
        consentNotificationLog.setConsentHandle("consentHandle");
        consentNotificationLog.setConsentId("consentId");
        ConsentNotificationLog save = consentNotificationLogRepository.save(consentNotificationLog);

        Assertions.assertTrue(consentNotificationLogRepository.findConsentNotificationLogByConsentHandle("consentHandle").isPresent());
        Assertions.assertSame(1, consentNotificationLogRepository.findAll().size());
        Assertions.assertEquals(save.getConsentId(), consentNotificationLogRepository.findConsentNotificationLogByConsentHandle("consentHandle").get().getConsentId());
        Assertions.assertEquals(save.getConsentHandle(), consentNotificationLogRepository.findConsentNotificationLogByConsentHandle("consentHandle").get().getConsentHandle());
    }

    @Test
    @DisplayName("Test deleting ConsentNotificationLog in database.")
    void deleteConsentNotificationLogTest() {
        ConsentNotificationLog consentNotificationLog = new ConsentNotificationLog();
        consentNotificationLog.setConsentHandle("consentHandleDelete");
        consentNotificationLog.setConsentId("consentIdDelete");
        ConsentNotificationLog save = consentNotificationLogRepository.save(consentNotificationLog);
        Assertions.assertTrue(consentNotificationLogRepository.findConsentNotificationLogByConsentHandle("consentHandleDelete").isPresent());
        ConsentNotificationLog consentNotificationLogDelete = consentNotificationLogRepository.findConsentNotificationLogByConsentHandle("consentHandleDelete").get();
        consentNotificationLogRepository.delete(consentNotificationLogDelete);
        Optional<ConsentNotificationLog> consentNotificationLogOptional = consentNotificationLogRepository.findConsentNotificationLogByConsentHandle("consentHandleDelete");
        Assertions.assertTrue(consentNotificationLogOptional.isEmpty());
    }

    private ConsentRequestDTO createConsentRequestDTO() {
        DataFilter dataFilter = new DataFilter();
        dataFilter.setOperator(">=");
        dataFilter.setType("TRANSACTIONAMOUNT");
        dataFilter.setValue("200");
        DataFilter dataFilter2 = new DataFilter();
        dataFilter2.setOperator(">");
        dataFilter2.setType("TRANSACTIONAMOUNT");
        dataFilter2.setValue("2000");
        Purpose purpose = new Purpose();
        Category category = new Category();
        category.setType("type");
        purpose.setCategory(category);
        purpose.setCode("code");
        purpose.setRefUri("refURI");
        purpose.setText("text");
        return ConsentRequestDTO.builder()
                .consentHandle("consentHandle")
                .consentId(null)
                .txnId("Txnid")
                .version("Ver")
                .timestamp(strToTimeStamp.apply("2021-06-21T11:57:55.499Z"))
                .aaName(aaNameExtractor.apply("Customer@aaId"))
                .customerId("Customer@aaId")
                .consentStartDate(strToTimeStamp.apply("2021-06-21T11:57:55.499Z"))
                .consentEndDate(strToTimeStamp.apply("2021-07-21T11:57:55.499Z"))
                .consentMode(ConsentMode.valueOf("STORE"))
                .consentTypes(String.join(",", Arrays.asList("fdas", "dsafdas")))
                .fiTypes(String.join(",", Arrays.asList("fitypes", "fitypes1")))
                .dataConsumerId("dataConsumerId")
                .purposeJson(purpose)
                .dataFilterJson(Arrays.asList(dataFilter, dataFilter2))
                .dataLifeUnit("Unit")
                .dataLifeValue(1)
                .fiDateRangeFrom(strToTimeStamp.apply("2021-06-21T11:57:55.499Z"))
                .fiDateRangeTo(strToTimeStamp.apply("2021-08-21T11:57:55.499Z"))
                .fetchType(FetchType.valueOf("ONETIME"))
                .frequencyUnit("FrequencyUnit")
                .frequencyValue(1)
                .build();

    }

    @Test
    @DisplayName("Test adding ConsentRequestDTO in database and fetching it.")
    void saveConsentRequestDTOTest() {
        ConsentRequestDTO save = consentRequestDTORepository.save(createConsentRequestDTO());

        Assertions.assertTrue(consentRequestDTORepository.findById("consentHandle").isPresent());
        Assertions.assertSame(1, consentRequestDTORepository.findAll().size());
        Assertions.assertEquals(save.getConsentId(), consentRequestDTORepository.findById("consentHandle").get().getConsentId());
        Assertions.assertEquals(save.getConsentHandle(), consentRequestDTORepository.findById("consentHandle").get().getConsentHandle());
    }

    @Test
    @DisplayName("Test deleting ConsentRequestDTO in database.")
    void deleteConsentRequestDTOTest() {
        ConsentRequestDTO save = consentRequestDTORepository.save(createConsentRequestDTO());
        Assertions.assertTrue(consentRequestDTORepository.findById("consentHandle").isPresent());
        ConsentRequestDTO consentRequestDTODelete = consentRequestDTORepository.findById("consentHandle").get();
        consentRequestDTORepository.delete(consentRequestDTODelete);
        Optional<ConsentRequestDTO> optionalConsentState = consentRequestDTORepository.findById("consentHandle");
        Assertions.assertTrue(optionalConsentState.isEmpty());
    }

    @Test
    @DisplayName("Test adding ConsentState in database and fetching it.")
    void saveConsentStateTest() {
        ConsentState consentState = new ConsentState();
        consentState.setConsentHandle("consentHandle");
        consentState.setConsentId("consentId");
        ConsentState save = consentStateRepository.save(consentState);

        Assertions.assertTrue(consentStateRepository.findById("consentHandle").isPresent());
        Assertions.assertTrue(consentStateRepository.findByConsentId("consentId").isPresent());
        Assertions.assertSame(1, consentStateRepository.findAll().size());
        Assertions.assertEquals(save.getConsentId(), consentStateRepository.findById("consentHandle").get().getConsentId());
        Assertions.assertEquals(save.getConsentHandle(), consentStateRepository.findById("consentHandle").get().getConsentHandle());
        Assertions.assertEquals(save.getConsentId(), consentStateRepository.findByConsentId("consentId").get().getConsentId());
        Assertions.assertEquals(save.getConsentHandle(), consentStateRepository.findByConsentId("consentId").get().getConsentHandle());
    }

    @Test
    @DisplayName("Test deleting ConsentState in database.")
    void deleteConsentStateTest() {
        ConsentState consentState = new ConsentState();
        consentState.setConsentHandle("consentHandle");
        consentState.setConsentId("consentId");
        ConsentState save = consentStateRepository.save(consentState);
        Assertions.assertTrue(consentStateRepository.findById("consentHandle").isPresent());
        Assertions.assertTrue(consentStateRepository.findByConsentId("consentId").isPresent());
        ConsentState consentStateDelete = consentStateRepository.findById("consentHandle").get();
        consentStateRepository.delete(consentStateDelete);
        Optional<ConsentState> optionalConsentState = consentStateRepository.findById("consentHandle");
        Assertions.assertTrue(optionalConsentState.isEmpty());
    }

    private ConsentTemplate createConsentTemplate() {
        ConsentTemplate consentTemplate = new ConsentTemplate();
        consentTemplate.setId(uuidSupplier.get());
        consentTemplate.setConsentVersion("1.1.2");
        consentTemplate.setTags("Tags");
        consentTemplate.setDescription("Description");
        consentTemplate.setConsentTemplateDefinition(new ConsentTemplateDefinition());
        return consentTemplate;
    }

    @Test
    @DisplayName("Test adding ConsentTemplate in database and fetching it.")
    void saveConsentTemplateTest() {
        ConsentTemplate save = consentTemplateRepository.save(createConsentTemplate());
        final Optional<ConsentTemplate> templateOptional = consentTemplateRepository.findById(save.getId());
        Assertions.assertTrue(templateOptional.isPresent());
        Assertions.assertEquals(save, templateOptional.get());
    }

    @Test
    @DisplayName("Test updating ConsentTemplate in database and fetching it.")
    void updateConsentTemplateTest() {
        ConsentTemplate save = consentTemplateRepository.save(createConsentTemplate());
        String id = save.getId();

        final Optional<ConsentTemplate> templateOptional = consentTemplateRepository.findById(id);
        Assertions.assertTrue(templateOptional.isPresent());
        Assertions.assertEquals(save, templateOptional.get());

        ConsentTemplate templateForUpdate = templateOptional.get();
        templateForUpdate.setDescription("UpdatedDescription");
        templateForUpdate.setTags("UpdatedTags");
        templateForUpdate.setConsentVersion("1.1.5");
        templateForUpdate.setConsentTemplateDefinition(new ConsentTemplateDefinition());
        consentTemplateRepository.save(templateForUpdate);

        Optional<ConsentTemplate> updatedTemplateOptional = consentTemplateRepository.findById(id);
        Assertions.assertTrue(updatedTemplateOptional.isPresent());
        Assertions.assertEquals(templateForUpdate, updatedTemplateOptional.get());
    }

    @Test
    @DisplayName("Test deleting ConsentTemplate in database.")
    void deleteConsentTemplateTest() {
        ConsentTemplate save = consentTemplateRepository.save(createConsentTemplate());

        Assertions.assertTrue(consentTemplateRepository.findById(save.getId()).isPresent());
        ConsentTemplate consentTemplateDelete = consentTemplateRepository.findById(save.getId()).get();
        consentTemplateRepository.delete(consentTemplateDelete);
        Optional<ConsentTemplate> optionalConsentTemplate = consentTemplateRepository.findById(save.getId());
        Assertions.assertTrue(optionalConsentTemplate.isEmpty());
    }
}
