package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.model.CodRiskProfile;
import com.ecommerce.app.module.fraud.repository.CodRiskProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class CodRiskProfileDeviceResolverTest {

    @Test
    void returnsConcurrentInsertWinnerAfterUniqueConflict() {
        CodRiskProfileRepository repository = mock(CodRiskProfileRepository.class);
        CodRiskProfileDeviceCreator creator = mock(CodRiskProfileDeviceCreator.class);
        CodRiskProfile winner = new CodRiskProfile();
        when(repository.findByDeviceIdentifier("device-hash")).thenReturn(Optional.empty());
        when(creator.create("device-hash"))
                .thenThrow(new DataIntegrityViolationException("duplicate device profile"));
        when(creator.findCommitted("device-hash")).thenReturn(Optional.of(winner));
        CodRiskProfileDeviceResolver resolver = new CodRiskProfileDeviceResolver(repository, creator);

        CodRiskProfile resolved = resolver.findOrCreate("device-hash");

        assertSame(winner, resolved);
        verify(creator).create("device-hash");
    }

    @Test
    void returnsExistingProfileWithoutInsert() {
        CodRiskProfileRepository repository = mock(CodRiskProfileRepository.class);
        CodRiskProfileDeviceCreator creator = mock(CodRiskProfileDeviceCreator.class);
        CodRiskProfile existing = new CodRiskProfile();
        when(repository.findByDeviceIdentifier("device-hash")).thenReturn(Optional.of(existing));
        CodRiskProfileDeviceResolver resolver = new CodRiskProfileDeviceResolver(repository, creator);

        assertSame(existing, resolver.findOrCreate("device-hash"));
    }
}
