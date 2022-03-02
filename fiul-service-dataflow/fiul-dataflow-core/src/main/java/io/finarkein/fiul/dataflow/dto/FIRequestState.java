/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.dto;

import io.finarkein.api.aa.notification.FIStatusResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "FI_REQUEST_STATE",
        indexes = {
                @Index(name = "FIReqState_Idx1", columnList = "sessionId, sessionStatus"),
                @Index(name = "FIReqState_Idx1", columnList = "sessionId, consentHandleId"),
                @Index(name = "FIReqState_Idx2", columnList = "sessionStatus")
        })
public class FIRequestState {

    @Id
    @Column(length = 36)
    protected String sessionId;

    @Column(length = 36)
    protected String consentHandleId;
    protected String notifierId;
    protected boolean fiRequestSuccessful;
    protected String sessionStatus;
    protected String txnId;
    protected String aaId;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    protected Timestamp fiRequestSubmittedOn;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    protected Timestamp notificationTimestamp;

    @Column(columnDefinition = "text")
    @Convert(converter = Converter.OfFIStatusResponseDTOSet.class)
    protected Set<FIStatusResponseDTO> fiStatusResponse;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    protected Timestamp updatedOn;

    @PrePersist
    protected void onCreate() {
        updatedOn = Timestamp.from(Instant.now());
    }

    @PreUpdate
    protected void onUpdate() {
        updatedOn = Timestamp.from(Instant.now());
    }

    public void updateFiStatusResponse(List<FIStatusResponse> fiStatusResponseInput) {
        final Map<String, FIStatusResponseDTO> inputFIStatusResponseDTOMap = Optional
                .ofNullable(fiStatusResponseInput)
                .map(inputStatusResponse -> inputStatusResponse
                        .stream()
                        .map(FIStatusResponseDTO::from)
                        .collect(Collectors.toMap(FIStatusResponseDTO::getFipID, Function.identity())))
                .orElse(null);

        if (fiStatusResponse != null) {
            if (inputFIStatusResponseDTOMap != null) {
                fiStatusResponse.forEach(savedDto -> {
                    final var inputDto = inputFIStatusResponseDTOMap.remove(savedDto.getFipID());
                    if (inputDto != null) {
                        final var accounts = savedDto.getAccounts();
                        if (accounts != null) {
                            accounts.removeAll(inputDto.getAccounts());
                            accounts.addAll(inputDto.getAccounts());
                        } else
                            savedDto.setAccounts(inputDto.getAccounts());
                    }
                });
                fiStatusResponse.addAll(inputFIStatusResponseDTOMap.values());
            }
        } else if (inputFIStatusResponseDTOMap != null) {
            fiStatusResponse = new HashSet<>(inputFIStatusResponseDTOMap.values());
        }
    }
}
