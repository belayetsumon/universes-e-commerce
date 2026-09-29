package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudIdempotencyRecord;
import com.ecommerce.app.module.fraud.model.FraudIdempotencyStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface FraudIdempotencyRecordRepository extends JpaRepository<FraudIdempotencyRecord, Long> {

    Optional<FraudIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    Optional<FraudIdempotencyRecord> findByIdempotencyKeyAndOperationScope(String idempotencyKey, String operationScope);

    List<FraudIdempotencyRecord> findByStatusAndExpiresAtBefore(FraudIdempotencyStatus status, LocalDateTime expiresAt);

    @Transactional(readOnly = true)
    @Query("""
            select record.id
            from FraudIdempotencyRecord record
            where record.expiresAt < :now
            order by record.id
            """)
    List<Long> findExpiredRetentionCandidateIds(@Param("now") LocalDateTime now, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from FraudIdempotencyRecord record
            where record.id in :ids
              and record.expiresAt < :now
            """)
    int deleteExpiredRetentionCandidates(
            @Param("ids") Collection<Long> ids,
            @Param("now") LocalDateTime now
    );
}
