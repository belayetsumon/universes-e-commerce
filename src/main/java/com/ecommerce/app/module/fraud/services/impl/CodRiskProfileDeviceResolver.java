package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.model.CodRiskProfile;
import com.ecommerce.app.module.fraud.repository.CodRiskProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Resolves the unique canonical device profile without a check-then-insert race. */
@Service
public class CodRiskProfileDeviceResolver {

    private final CodRiskProfileRepository repository;
    private final CodRiskProfileDeviceCreator creator;

    public CodRiskProfileDeviceResolver(
            CodRiskProfileRepository repository,
            CodRiskProfileDeviceCreator creator
    ) {
        this.repository = repository;
        this.creator = creator;
    }

    public CodRiskProfile findOrCreate(String canonicalDeviceIdentifier) {
        if (canonicalDeviceIdentifier == null || canonicalDeviceIdentifier.isBlank()) {
            throw new IllegalArgumentException("Canonical device identifier is required.");
        }
        return repository.findByDeviceIdentifier(canonicalDeviceIdentifier)
                .orElseGet(() -> createOrFindWinner(canonicalDeviceIdentifier));
    }

    private CodRiskProfile createOrFindWinner(String canonicalDeviceIdentifier) {
        try {
            return creator.create(canonicalDeviceIdentifier);
        } catch (DataIntegrityViolationException concurrentInsert) {
            // Refetch in a fresh transaction. MySQL's default REPEATABLE READ
            // could otherwise keep the caller's pre-conflict snapshot and hide
            // the row whose unique-key commit won the race.
            return creator.findCommitted(canonicalDeviceIdentifier)
                    .orElseThrow(() -> concurrentInsert);
        }
    }
}
