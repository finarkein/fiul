package io.finarkein.fiul.consent.repo;

import io.finarkein.fiul.consent.model.CreateConsentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreateConsentStateRepository extends JpaRepository<CreateConsentState, String> {
    Optional<CreateConsentState> findByConsentHandle(String consentHandle);
}
