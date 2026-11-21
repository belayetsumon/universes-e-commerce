package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import com.ecommerce.app.module.fraud.model.FraudModuleMode;
import com.ecommerce.app.module.fraud.repository.FraudConfigurationRepository;
import com.ecommerce.app.module.fraud.services.FraudAuditService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultFraudModuleControlServiceTest {

    private FraudConfigurationRepository repository;
    private FraudAuditService audit;
    private DefaultFraudModuleControlService service;

    @BeforeEach
    void setUp() {
        repository = org.mockito.Mockito.mock(FraudConfigurationRepository.class);
        audit = org.mockito.Mockito.mock(FraudAuditService.class);
        service = new DefaultFraudModuleControlService(repository, audit);
    }

    @Test
    void missingModeDefaultsToActive() {
        when(repository.findByConfigKey(DefaultFraudModuleControlService.MODE_KEY)).thenReturn(Optional.empty());
        assertEquals(FraudModuleMode.ACTIVE, service.currentMode());
    }

    @Test
    void invalidAndDisabledModesFailClosed() {
        FraudConfiguration setting = new FraudConfiguration();
        setting.setActive(true);
        setting.setConfigValue("INVALID");
        when(repository.findByConfigKey(DefaultFraudModuleControlService.MODE_KEY)).thenReturn(Optional.of(setting));
        assertThrows(IllegalStateException.class, () -> service.currentMode());

        setting.setConfigValue(FraudModuleMode.INACTIVE.name());
        setting.setActive(false);
        assertThrows(IllegalStateException.class, () -> service.currentMode());
    }

    @Test
    void savingModeAuditsTheTransition() {
        when(repository.findByConfigKey(DefaultFraudModuleControlService.MODE_KEY)).thenReturn(Optional.empty());
        when(repository.save(any(FraudConfiguration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.changeMode(FraudModuleMode.MONITOR_ONLY, "fraud-admin", "Testing rules");
        verify(repository).save(any(FraudConfiguration.class));
        verify(audit).record(
                org.mockito.ArgumentMatchers.eq("FRAUD_MODULE_MODE"),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.contains("Testing rules"),
                org.mockito.ArgumentMatchers.contains("MONITOR_ONLY"));
    }

    @Test
    void missingReasonIsRejected() {
        assertThrows(FraudValidationException.class,
                () -> service.changeMode(FraudModuleMode.INACTIVE, "fraud-admin", " "));
    }
}
