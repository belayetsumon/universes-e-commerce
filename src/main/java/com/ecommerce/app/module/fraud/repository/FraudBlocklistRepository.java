package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.FraudBlocklist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FraudBlocklistRepository extends JpaRepository<FraudBlocklist, Long>, JpaSpecificationExecutor<FraudBlocklist> {

    Optional<FraudBlocklist> findByUuid(String uuid);

    Optional<FraudBlocklist> findByBlockTypeAndHashedValueAndActiveTrue(FraudBlockType blockType, String hashedValue);

    List<FraudBlocklist> findByActiveTrueOrderByIdDesc();

    long countByActive(boolean active);

    boolean existsByBlockTypeAndHashedValueAndActiveTrue(FraudBlockType blockType, String hashedValue);
}
