package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.dto.FraudConfigurationRequest;
import java.math.BigDecimal;
import java.util.Optional;

public interface FraudConfigurationService {

    void save(FraudConfigurationRequest request);

    Optional<String> findValue(String key);

    int getInt(String key, int defaultValue);

    BigDecimal getMoney(String key, BigDecimal defaultValue);

    boolean getBoolean(String key, boolean defaultValue);
}
