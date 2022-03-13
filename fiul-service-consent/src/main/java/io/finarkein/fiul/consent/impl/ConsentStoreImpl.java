/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import io.finarkein.api.aa.consent.ConsentMode;
import io.finarkein.api.aa.consent.FetchType;
import io.finarkein.api.aa.consent.request.ConsentRequest;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.ConsentStatusNotification;
import io.finarkein.api.aa.notification.Notifier;
import io.finarkein.api.aa.util.Functions;
import io.finarkein.fiul.config.DBCallHandlerSchedulerConfig;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.model.ConsentRequestDTO;
import io.finarkein.fiul.consent.model.ConsentStateDTO;
import io.finarkein.fiul.consent.model.SignedConsentDTO;
import io.finarkein.fiul.consent.repo.ConsentNotificationLogRepository;
import io.finarkein.fiul.consent.repo.ConsentRequestDTORepository;
import io.finarkein.fiul.consent.repo.ConsentStateRepository;
import io.finarkein.fiul.consent.repo.RepoSignedConsent;
import io.finarkein.fiul.consent.service.ConsentStore;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static io.finarkein.api.aa.util.Functions.aaNameExtractor;
import static io.finarkein.api.aa.util.Functions.strToTimeStamp;

@Service
@Log4j2
class ConsentStoreImpl implements ConsentStore {

    @Autowired
    private ConsentRequestDTORepository consentRequestDTORepository;

    @Autowired
    private ConsentStateRepository consentStateRepository;

    @Autowired
    private ConsentNotificationLogRepository consentNotificationLogRepository;

    @Autowired
    private RepoSignedConsent repoSignedConsent;

    @Autowired
    protected DBCallHandlerSchedulerConfig dbBlockingCallSchedulerConfig;

    @Override
    public void saveConsentRequest(String consentHandle, ConsentRequest consentRequest) {
        ConsentRequestDTO consentRequestDTO = ConsentRequestDTO.builder()
                .consentHandle(consentHandle)
                .consentId(null)
                .txnId(consentRequest.getTxnid())
                .version(consentRequest.getVer())
                .timestamp(strToTimeStamp.apply(consentRequest.getTimestamp()))
                .aaName(aaNameExtractor.apply(consentRequest.getConsentDetail().getCustomer().getId()))
                .customerId(consentRequest.getConsentDetail().getCustomer().getId())
                .consentStartDate(strToTimeStamp.apply(consentRequest.getConsentDetail().getConsentStart()))
                .consentEndDate(strToTimeStamp.apply(consentRequest.getConsentDetail().getConsentExpiry()))
                .consentMode(ConsentMode.valueOf(consentRequest.getConsentDetail().getConsentMode()))
                .consentTypes(String.join(",", consentRequest.getConsentDetail().getConsentTypes()))
                .fiTypes(String.join(",", consentRequest.getConsentDetail().getFiTypes()))
                .dataConsumerId(consentRequest.getConsentDetail().getDataConsumer().getId())
                .purposeJson(consentRequest.getConsentDetail().getPurpose())
                .dataFilterJson(consentRequest.getConsentDetail().getDataFilter())
                .dataLifeUnit(consentRequest.getConsentDetail().getDataLife().getUnit())
                .dataLifeValue(consentRequest.getConsentDetail().getDataLife().getValue())
                .fiDateRangeFrom(strToTimeStamp.apply(consentRequest.getConsentDetail().getFIDataRange().getFrom()))
                .fiDateRangeTo(strToTimeStamp.apply(consentRequest.getConsentDetail().getFIDataRange().getTo()))
                .fetchType(FetchType.valueOf(consentRequest.getConsentDetail().getFetchType()))
                .frequencyUnit(consentRequest.getConsentDetail().getFrequency().getUnit())
                .frequencyValue(consentRequest.getConsentDetail().getFrequency().getValue())
                .build();
        consentRequestDTORepository.save(consentRequestDTO);
    }

    @Override
    public void updateConsentRequest(String consentHandleId, String consentId) {
        Optional<ConsentRequestDTO> optionalConsentRequestDTO = consentRequestDTORepository.findById(consentHandleId);
        if (optionalConsentRequestDTO.isPresent() && optionalConsentRequestDTO.get().getConsentId() == null) {
            optionalConsentRequestDTO.get().setConsentId(consentId);
            consentRequestDTORepository.save(optionalConsentRequestDTO.get());
        }
    }

    @Override
    public Optional<SignedConsentDTO> findSignedConsent(String consentId) {
        return repoSignedConsent.findByConsentId(consentId);
    }

    @Override
    public Mono<Optional<ConsentRequestDTO>> findRequestByConsentHandle(String consentHandle) {
        return Mono.fromCallable(() -> consentRequestDTORepository.findById(consentHandle))
                .subscribeOn(dbBlockingCallSchedulerConfig.getScheduler());
    }

    @Override
    public Optional<ConsentRequestDTO> findRequestByConsentId(String consentId) {
        return consentRequestDTORepository.findByConsentId(consentId);
    }

    @Override
    public void logConsentNotification(ConsentNotificationLog consentNotificationLog) {
        Optional<ConsentStateDTO> optionalConsentState = consentStateRepository.findById(consentNotificationLog.getConsentHandle());
        ConsentStateDTO consentStateDTO = optionalConsentState.orElseGet(ConsentStateDTO::new);
        consentStateDTO.setConsentHandle(consentNotificationLog.getConsentHandle());
        consentStateDTO.setConsentId(consentNotificationLog.getConsentId());
        consentStateDTO.setConsentStatus(consentNotificationLog.getConsentState());
        consentStateDTO.setTxnId(consentNotificationLog.getTxnId());
        consentStateDTO.setNotifierId(consentNotificationLog.getNotifierId());

        consentNotificationLogRepository.save(consentNotificationLog);
        final ConsentStateDTO savedConsentStateDTO = consentStateRepository.save(consentStateDTO);
        log.debug("Saved consentState after consentNotification:{}", savedConsentStateDTO);
        updateConsentRequest(consentNotificationLog.getConsentHandle(), consentNotificationLog.getConsentId());
    }

    @Override
    public ConsentNotification getConsentNotification(String consentHandle) {
        Optional<ConsentNotificationLog> optionalConsentNotificationLog = consentNotificationLogRepository.findConsentNotificationLogByConsentHandle(consentHandle);
        if (optionalConsentNotificationLog.isEmpty())
            return null;
        ConsentNotificationLog consentNotificationLog = optionalConsentNotificationLog.get();
        ConsentNotification consentNotification = new ConsentNotification();
        consentNotification.setVer(consentNotificationLog.getVersion());
        final String timestampString = Functions.timestampToStr.apply(consentNotificationLog.getNotificationTimestamp());
        consentNotification.setTimestamp(timestampString);
        consentNotification.setTxnid(consentNotificationLog.getTxnId());
        consentNotification.setNotifier(new Notifier(consentNotificationLog.getNotifierType(), consentNotificationLog.getNotifierId()));
        ConsentStatusNotification consentStatusNotification = new ConsentStatusNotification();
        consentStatusNotification.setConsentHandle(consentNotificationLog.getConsentHandle());
        consentStatusNotification.setConsentStatus(consentNotificationLog.getConsentState());
        consentStatusNotification.setConsentId(consentNotificationLog.getConsentId());
        consentNotification.setConsentStatusNotification(consentStatusNotification);
        return consentNotification;
    }

    @Override
    public void saveConsentState(ConsentStateDTO consentStateDTO) {
        consentStateRepository.save(consentStateDTO);
    }

    @Override
    public Optional<ConsentStateDTO> getConsentStateByHandle(String consentHandle) {
        return consentStateRepository.findById(consentHandle);
    }

    @Override
    public Mono<Optional<ConsentStateDTO>> consentStateByHandle(String consentHandle) {
        return Mono.fromCallable(() -> consentStateRepository.findById(consentHandle))
                .subscribeOn(dbBlockingCallSchedulerConfig.getScheduler());
    }

    @Override
    public ConsentStateDTO getConsentStateById(String consentId) {
        return consentStateRepository.findByConsentId(consentId).orElse(null);
    }

    @Override
    public ConsentStateDTO getConsentStateByTxnId(String txnId) {
        return consentStateRepository.findByTxnId(txnId).orElse(null);
    }

    @Override
    public Mono<ConsentStateDTO> consentStateByTxnId(String txnId){
        return Mono.fromCallable(() -> consentStateRepository.findByTxnId(txnId).orElse(null))
                .subscribeOn(dbBlockingCallSchedulerConfig.getScheduler());
    }

    @Override
    public ConsentStateDTO updateConsentState(ConsentStateDTO consentStateDTO) {
        return consentStateRepository.save(consentStateDTO);
    }

    @Override
    public void saveSignedConsent(SignedConsentDTO signedConsentDTO) {
        repoSignedConsent.save(signedConsentDTO);
    }
}
