package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudRule;
import com.ecommerce.app.module.fraud.model.FraudRuleType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FraudRuleRepository extends JpaRepository<FraudRule, Long>, JpaSpecificationExecutor<FraudRule> {

    Optional<FraudRule> findByUuid(String uuid);

    Optional<FraudRule> findByRuleCode(String ruleCode);

    List<FraudRule> findByActiveTrueOrderByPriorityAscIdAsc();

    List<FraudRule> findByRuleTypeAndActiveTrueOrderByPriorityAscIdAsc(FraudRuleType ruleType);

    Page<FraudRule> findByActive(boolean active, Pageable pageable);

    long countByActive(boolean active);

    boolean existsByRuleCode(String ruleCode);
}
