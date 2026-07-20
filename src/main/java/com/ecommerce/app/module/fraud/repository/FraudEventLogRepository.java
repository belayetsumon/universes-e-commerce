package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudEventLog;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FraudEventLogRepository extends JpaRepository<FraudEventLog, Long>, JpaSpecificationExecutor<FraudEventLog> {

    List<FraudEventLog> findByOrderIdOrderByEventTimeDesc(Long orderId);

    List<FraudEventLog> findByVendorIdOrderByEventTimeDesc(Long vendorId);

    Optional<FraudEventLog> findByIdempotencyKey(String idempotencyKey);

    long countByEventType(FraudEventType eventType);
}
