package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudConfiguration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FraudConfigurationRepository extends JpaRepository<FraudConfiguration, Long>, JpaSpecificationExecutor<FraudConfiguration> {

    Optional<FraudConfiguration> findByConfigKey(String configKey);

    Optional<FraudConfiguration> findByConfigKeyAndActiveTrue(String configKey);

    List<FraudConfiguration> findByActiveTrueOrderByConfigKeyAsc();

    boolean existsByConfigKey(String configKey);
}
