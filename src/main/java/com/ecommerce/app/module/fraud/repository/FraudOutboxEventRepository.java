package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudOutboxEvent;
import com.ecommerce.app.module.fraud.model.FraudOutboxStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudOutboxEventRepository extends JpaRepository<FraudOutboxEvent, Long> {

    Optional<FraudOutboxEvent> findByIdempotencyKey(String idempotencyKey);

    List<FraudOutboxEvent> findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            FraudOutboxStatus status, LocalDateTime nextAttemptAt);

    List<FraudOutboxEvent> findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
            Collection<FraudOutboxStatus> statuses, LocalDateTime nextAttemptAt);

    List<FraudOutboxEvent> findByAggregateTypeAndAggregateIdOrderByIdDesc(String aggregateType, Long aggregateId);
}
