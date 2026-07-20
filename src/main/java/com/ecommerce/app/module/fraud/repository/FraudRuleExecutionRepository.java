package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudRuleExecution;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudRuleExecutionRepository extends JpaRepository<FraudRuleExecution, Long> {

    List<FraudRuleExecution> findByAssessment_IdOrderByIdAsc(Long assessmentId);

    long countByRuleCodeAndMatchedTrue(String ruleCode);
}
