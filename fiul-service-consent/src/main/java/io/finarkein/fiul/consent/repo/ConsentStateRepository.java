/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.repo;

import io.finarkein.fiul.consent.model.ConsentStateDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsentStateRepository extends JpaRepository<ConsentStateDTO, String> {
    Optional<ConsentStateDTO> findByConsentId(String consentId);
    Optional<ConsentStateDTO> findByTxnId(String txnId);
}
