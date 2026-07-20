package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudConfigurationRequest;
import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import com.ecommerce.app.module.fraud.repository.FraudConfigurationRepository;
import com.ecommerce.app.module.fraud.services.FraudConfigurationService;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudConfigurationService implements FraudConfigurationService {

    private final FraudConfigurationRepository fraudConfigurationRepository;

    public DefaultFraudConfigurationService(FraudConfigurationRepository fraudConfigurationRepository) {
        this.fraudConfigurationRepository = fraudConfigurationRepository;
    }

    @Override
    @Transactional
    public void save(FraudConfigurationRequest request) {
        FraudConfiguration configuration = fraudConfigurationRepository.findByConfigKey(request.getConfigKey())
                .orElseGet(FraudConfiguration::new);
        configuration.setConfigKey(request.getConfigKey());
        configuration.setConfigValue(request.getConfigValue());
        configuration.setDescription(request.getDescription());
        configuration.setActive(request.isActive());
        fraudConfigurationRepository.save(configuration);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findValue(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        return fraudConfigurationRepository.findByConfigKeyAndActiveTrue(key.trim())
                .map(FraudConfiguration::getConfigValue);
    }

    @Override
    @Transactional(readOnly = true)
    public int getInt(String key, int defaultValue) {
        return findValue(key).map(value -> parseInt(value, defaultValue)).orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMoney(String key, BigDecimal defaultValue) {
        return findValue(key).map(value -> parseMoney(value, defaultValue)).orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean getBoolean(String key, boolean defaultValue) {
        return findValue(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }

    private BigDecimal parseMoney(String value, BigDecimal defaultValue) {
        try {
            return new BigDecimal(value.trim());
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }
}
