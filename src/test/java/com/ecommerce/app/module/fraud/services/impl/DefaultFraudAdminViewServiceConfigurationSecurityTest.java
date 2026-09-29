package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.dto.FraudConfigurationRequest;
import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.repository.FraudCaseRepository;
import com.ecommerce.app.module.fraud.repository.FraudConfigurationRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.repository.FraudEvidenceRepository;
import com.ecommerce.app.module.fraud.repository.FraudReviewHistoryRepository;
import com.ecommerce.app.module.fraud.repository.FraudRuleExecutionRepository;
import com.ecommerce.app.module.fraud.repository.FraudRuleRepository;
import com.ecommerce.app.module.fraud.repository.FraudSignalRepository;
import com.ecommerce.app.module.fraud.repository.VendorRiskProfileRepository;
import com.ecommerce.app.module.fraud.services.FraudAuditService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultFraudAdminViewServiceConfigurationSecurityTest {

    private FraudConfigurationRepository repository;
    private DefaultFraudAdminViewService service;

    @BeforeEach
    void setUp() {
        repository = mock(FraudConfigurationRepository.class);
        when(repository.save(any(FraudConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service = new DefaultFraudAdminViewService(
                mock(FraudAssessmentRepository.class),
                mock(FraudCaseRepository.class),
                mock(FraudRuleRepository.class),
                mock(FraudBlocklistRepository.class),
                repository,
                mock(FraudSignalRepository.class),
                mock(FraudRuleExecutionRepository.class),
                mock(FraudReviewHistoryRepository.class),
                mock(FraudEvidenceRepository.class),
                mock(FraudEventLogRepository.class),
                mock(VendorRiskProfileRepository.class),
                mock(FraudAuditService.class)
        );
    }

    @Test
    void storedSensitiveValueNeverEntersTheBrowserForm() {
        FraudConfiguration configuration = secretConfiguration();

        FraudConfigurationRequest request = service.toConfigurationRequest(configuration);

        assertNull(request.getConfigValue());
        assertTrue(request.isSensitive());
        assertTrue(request.isValueConfigured());
        assertEquals(configuration.getConfigKey(), request.getConfigKey());
    }

    @Test
    void blankSensitiveEditPreservesExistingSecretAndReplacementIsExplicit() {
        FraudConfiguration configuration = secretConfiguration();
        when(repository.findById(9L)).thenReturn(Optional.of(configuration));
        FraudConfigurationRequest preserve = editRequest(configuration.getConfigKey());

        service.saveConfiguration(9L, preserve);

        assertEquals("existing-webhook-secret", configuration.getConfigValue());

        FraudConfigurationRequest replace = editRequest(configuration.getConfigKey());
        replace.setConfigValue(" replacement-secret ");
        service.saveConfiguration(9L, replace);

        assertEquals("replacement-secret", configuration.getConfigValue());
    }

    @Test
    void explicitSensitiveClearDeactivatesFailClosedAndKeyRenameIsRejected() {
        FraudConfiguration configuration = secretConfiguration();
        when(repository.findById(9L)).thenReturn(Optional.of(configuration));
        FraudConfigurationRequest clear = editRequest(configuration.getConfigKey());
        clear.setClearConfigValue(true);

        service.saveConfiguration(9L, clear);

        assertEquals("", configuration.getConfigValue());
        assertFalse(configuration.isActive());

        FraudConfigurationRequest rename = editRequest("fraud.webhook.other.secret");
        assertThrows(FraudValidationException.class, () -> service.saveConfiguration(9L, rename));
    }

    @Test
    void newConfigurationStillRequiresAValue() {
        when(repository.findByConfigKey("fraud.cod.max_amount")).thenReturn(Optional.empty());
        FraudConfigurationRequest request = editRequest("fraud.cod.max_amount");

        assertThrows(FraudValidationException.class, () -> service.saveConfiguration(null, request));
    }

    private FraudConfiguration secretConfiguration() {
        FraudConfiguration configuration = new FraudConfiguration();
        configuration.setId(9L);
        configuration.setConfigKey("fraud.webhook.partner.secret");
        configuration.setConfigValue("existing-webhook-secret");
        configuration.setDescription("Partner HMAC secret");
        configuration.setActive(true);
        return configuration;
    }

    private FraudConfigurationRequest editRequest(String key) {
        FraudConfigurationRequest request = new FraudConfigurationRequest();
        request.setConfigKey(key);
        request.setActive(true);
        return request;
    }
}
