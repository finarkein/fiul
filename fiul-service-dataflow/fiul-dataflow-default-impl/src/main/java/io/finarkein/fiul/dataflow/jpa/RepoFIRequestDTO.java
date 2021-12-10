/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.jpa;

import io.finarkein.fiul.dataflow.dto.FIRequestDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepoFIRequestDTO extends JpaRepository<FIRequestDTO, FIRequestDTO.Key> {

    Optional<FIRequestDTO> findBySessionIdAndConsentHandleId(String sessionId, String consentHandleId);
}
