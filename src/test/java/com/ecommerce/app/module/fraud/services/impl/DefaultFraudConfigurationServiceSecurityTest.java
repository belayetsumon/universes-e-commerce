package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import com.ecommerce.app.module.fraud.repository.FraudConfigurationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DefaultFraudConfigurationServiceSecurityTest {

    @Test
    void externalSecretOverridesDatabaseAndBlankDatabaseValueFailsClosed() {
        FraudConfigurationRepository repository = mock(FraudConfigurationRepository.class);
        MockEnvironment environment = new MockEnvironment()
                .withProperty("FRAUD_WEBHOOK_PARTNER_SECRET", " environment-secret ");
        DefaultFraudConfigurationService service = new DefaultFraudConfigurationService(
                repository, environment);

        assertEquals(
                "environment-secret",
                service.findValue("fraud.webhook.partner.secret").orElseThrow()
        );

        FraudConfiguration cleared = new FraudConfiguration();
        cleared.setConfigValue("   ");
        when(repository.findByConfigKeyAndActiveTrue("fraud.webhook.other.secret"))
                .thenReturn(Optional.of(cleared));

        assertTrue(service.findValue("fraud.webhook.other.secret").isEmpty());
    }
}
