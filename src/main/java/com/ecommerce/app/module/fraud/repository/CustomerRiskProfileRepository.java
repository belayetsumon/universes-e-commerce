package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.CustomerRiskProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRiskProfileRepository extends JpaRepository<CustomerRiskProfile, Long> {

    Optional<CustomerRiskProfile> findByCustomerId(Long customerId);

    boolean existsByCustomerIdAndBlacklistedTrue(Long customerId);

    boolean existsByCustomerIdAndCodDisabledTrue(Long customerId);
}
