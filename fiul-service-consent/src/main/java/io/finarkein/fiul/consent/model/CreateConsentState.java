package io.finarkein.fiul.consent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsentState {

    @Id
    private String txnId;
    private boolean wasSuccessful;
    private String aaId;
    private String consentId;
    private String consentHandle;

//    TODO Add AAId, consentId, consentHandle for notification validation
}
