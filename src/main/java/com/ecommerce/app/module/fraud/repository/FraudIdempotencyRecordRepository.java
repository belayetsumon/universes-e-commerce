package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudIdempotencyRecord;
import com.ecommerce.app.module.fraud.model.FraudIdempotencyStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudIdempotencyRecordRepository extends JpaRepository<FraudIdempotencyRecord, Long> {

    Optional<FraudIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    Optional<FraudIdempotencyRecord> findByIdempotencyKeyAndOperationScope(String idempotencyKey, String operationScope);

    List<FraudIdempotencyRecord> findByStatusAndExpiresAtBefore(FraudIdempotencyStatus status, LocalDateTime expiresAt);
}
