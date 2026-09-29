package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.fraud.dto.FraudConfigurationRequest;
import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import com.ecommerce.app.module.fraud.repository.FraudConfigurationRepository;
import com.ecommerce.app.module.fraud.services.FraudConfigurationService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Locale;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultFraudConfigurationService implements FraudConfigurationService {

    private final FraudConfigurationRepository fraudConfigurationRepository;
    private final Environment environment;

    public DefaultFraudConfigurationService(
            FraudConfigurationRepository fraudConfigurationRepository,
            Environment environment) {
        this.fraudConfigurationRepository = fraudConfigurationRepository;
        this.environment = environment;
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
        String cleanedKey = key.trim();
        String externalValue = clean(environment.getProperty(cleanedKey));
        if (externalValue == null) {
            externalValue = clean(environment.getProperty(toEnvironmentName(cleanedKey)));
        }
        if (externalValue != null) {
            return Optional.of(externalValue);
        }
        return fraudConfigurationRepository.findByConfigKeyAndActiveTrue(cleanedKey)
                .map(FraudConfiguration::getConfigValue)
                .map(this::clean)
                .filter(value -> value != null);
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

    private String toEnvironmentName(String key) {
        return key.replaceAll("[^A-Za-z0-9]", "_").toUpperCase(Locale.ROOT);
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
