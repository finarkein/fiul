/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.finarkein.api.aa.consent.ConsentMode;
import io.finarkein.api.aa.consent.FetchType;
import io.finarkein.api.aa.consent.request.ConsentRequest;
import io.finarkein.api.aa.exception.Errors;
import io.finarkein.api.aa.notification.ConsentNotification;
import io.finarkein.api.aa.notification.ConsentStatusNotification;
import io.finarkein.api.aa.notification.Notifier;
import io.finarkein.api.aa.util.Functions;
import io.finarkein.fiul.consent.FIUConsentRequest;
import io.finarkein.fiul.consent.model.ConsentNotificationLog;
import io.finarkein.fiul.consent.model.ConsentRequestDTO;
import io.finarkein.fiul.consent.model.ConsentRequestLog;
import io.finarkein.fiul.consent.model.ConsentState;
import io.finarkein.fiul.consent.repo.*;
import io.finarkein.fiul.consent.service.ConsentStore;
import io.finarkein.fiul.consent.service.PurposeFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.finarkein.api.aa.util.Functions.aaNameExtractor;
import static io.finarkein.api.aa.util.Functions.strToTimeStamp;

@Service
class ConsentStoreImpl implements ConsentStore {

    @Autowired
    private ConsentRequestDTORepository consentRequestDTORepository;

    @Autowired
    private ConsentStateRepository consentStateRepository;

    @Autowired
    private ConsentNotificationLogRepository consentNotificationLogRepository;

    @Autowired
    private ConsentRequestLogRepository consentRequestLogRepository;

    @Autowired
    private ConsentTemplateRepository consentTemplateRepository;

    @Autowired
    private PurposeFetcher purposeFetcher;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ConsentRequestLog logConsentRequest(FIUConsentRequest consentRequest) {
        try {
            var consentRequestLog = ConsentRequestLog.builder()
                    .version(consentRequest.getVer())
                    .txnId(consentRequest.getTxnid())
                    .timestamp(strToTimeStamp.apply(consentRequest.getTimestamp()))
                    .aaId(aaNameExtractor.apply(consentRequest.getConsentDetail().getCustomer().getId()))
                    .customerAAId(consentRequest.getConsentDetail().getCustomer().getId())
                    .consentDetail(consentRequest.getConsentDetail())
                    .build();
            return consentRequestLogRepository.save(consentRequestLog);
        } catch (Exception e) {
            throw Errors.InternalError.with(consentRequest.getTxnid(), e.getMessage(), e);
        }
    }

    @Override
    public void updateConsentRequestLog(ConsentRequestLog consentRequestLog) {
        consentRequestLogRepository.save(consentRequestLog);
    }

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
    public Optional<ConsentRequestDTO> findRequestByConsentHandle(String consentHandle) {
        return consentRequestDTORepository.findById(consentHandle);
    }

    @Override
    public Optional<ConsentRequestDTO> findRequestByConsentId(String consentId) {
        return consentRequestDTORepository.findByConsentId(consentId);
    }

    @Override
    public void logConsentNotification(ConsentNotificationLog consentNotificationLog) {
        var consentState = new ConsentState();
        consentState.setConsentHandle(consentNotificationLog.getConsentHandle());
        consentState.setConsentId(consentNotificationLog.getConsentId());
        consentState.setConsentStatus(consentNotificationLog.getConsentState());
        consentState.setTxnId(consentNotificationLog.getTxnId());

        consentNotificationLogRepository.save(consentNotificationLog);
        consentStateRepository.save(consentState);
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
    public Optional<ConsentState> getConsentStateByHandle(String consentHandle) {
        return consentStateRepository.findById(consentHandle);
    }

    @Override
    public ConsentState getConsentStateById(String consentId) {
        return consentStateRepository.findByConsentId(consentId).orElse(null);
    }
}
