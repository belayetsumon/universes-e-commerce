package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import com.ecommerce.app.module.fraud.model.FraudModuleMode;
import com.ecommerce.app.module.fraud.repository.FraudConfigurationRepository;
import com.ecommerce.app.module.fraud.services.FraudAuditService;
import com.ecommerce.app.module.fraud.services.FraudModuleControlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudModuleControlService implements FraudModuleControlService {

    public static final String MODE_KEY = "fraud.module.mode";

    private final FraudConfigurationRepository repository;
    private final FraudAuditService auditService;

    public DefaultFraudModuleControlService(FraudConfigurationRepository repository, FraudAuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public FraudModuleMode currentMode() {
        return repository.findByConfigKey(MODE_KEY).map(configuration -> {
            if (!configuration.isActive()) {
                throw new IllegalStateException("Fraud module mode configuration must remain active.");
            }
            try {
                return FraudModuleMode.valueOf(configuration.getConfigValue().trim());
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid fraud module mode; fraud checks cannot be bypassed.", ex);
            }
        }).orElse(FraudModuleMode.ACTIVE);
    }

    @Override
    @Transactional
    public void changeMode(FraudModuleMode mode, String actor, String reason) {
        if (mode == null || actor == null || actor.isBlank() || reason == null || reason.isBlank()) {
            throw new FraudValidationException("Valid mode, administrator and change reason are required.");
        }
        FraudConfiguration configuration = repository.findByConfigKey(MODE_KEY).orElseGet(FraudConfiguration::new);
        FraudModuleMode previous = configuration.getConfigKey() == null ? FraudModuleMode.ACTIVE : currentMode();
        if (previous == mode) {
            return;
        }
        configuration.setConfigKey(MODE_KEY);
        configuration.setConfigValue(mode.name());
        configuration.setDescription("Operational mode for new fraud evaluation and enforcement.");
        configuration.setActive(true);
        FraudConfiguration saved = repository.save(configuration);
        auditService.record("FRAUD_MODULE_MODE", saved.getId(), FraudAction.MANUAL_REVIEW,
                "Fraud mode changed by " + actor.trim() + ": " + reason.trim(),
                "{\"previous\":\"" + previous.name() + "\",\"current\":\"" + mode.name() + "\"}");
    }
}
