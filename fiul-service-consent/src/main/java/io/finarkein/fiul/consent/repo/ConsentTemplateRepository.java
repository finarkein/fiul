/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.consent.repo;

import io.finarkein.fiul.consent.model.ConsentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ConsentTemplateRepository extends JpaRepository<ConsentTemplate, String> {

    @Modifying(flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM ConsentTemplate WHERE id = :idValue")
    void deleteById(@Param("idValue") String idValue);
}