/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.dataflow.easy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder(builderClassName = "Builder")
@IdClass(KeyMaterialDataKey.Key.class)
@Table(name = "FI_KM_DATA_KEY")
@Cache(region = "keyMaterialDataKeyCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class KeyMaterialDataKey {

    @Id
    String consentId;

    @Id
    String sessionId;

    String consentHandleId;

    @Column(columnDefinition = "TEXT", nullable = false, updatable = false)
    String encryptedKey;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        @Id
        String consentId;

        @Id
        String sessionId;
    }
}
