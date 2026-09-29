package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.model.CodRiskProfile;
import com.ecommerce.app.module.fraud.repository.CodRiskProfileRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Inserts one device profile in an isolated transaction for conflict retry. */
@Service
public class CodRiskProfileDeviceCreator {

    private final CodRiskProfileRepository repository;

    public CodRiskProfileDeviceCreator(CodRiskProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CodRiskProfile create(String canonicalDeviceIdentifier) {
        CodRiskProfile profile = new CodRiskProfile();
        profile.setDeviceIdentifier(canonicalDeviceIdentifier);
        return repository.saveAndFlush(profile);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<CodRiskProfile> findCommitted(String canonicalDeviceIdentifier) {
        return repository.findByDeviceIdentifier(canonicalDeviceIdentifier);
    }
}
