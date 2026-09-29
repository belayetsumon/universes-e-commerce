package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.FraudOutboxEvent;
import com.ecommerce.app.module.fraud.model.FraudOutboxStatus;
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

public interface FraudOutboxEventRepository extends JpaRepository<FraudOutboxEvent, Long> {

    Optional<FraudOutboxEvent> findByIdempotencyKey(String idempotencyKey);

    List<FraudOutboxEvent> findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            FraudOutboxStatus status, LocalDateTime nextAttemptAt);

    List<FraudOutboxEvent> findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByIdAsc(
            Collection<FraudOutboxStatus> statuses, LocalDateTime nextAttemptAt);

    List<FraudOutboxEvent> findByAggregateTypeAndAggregateIdOrderByIdDesc(String aggregateType, Long aggregateId);

    @Transactional(readOnly = true)
    @Query("""
            select event.id
            from FraudOutboxEvent event
            where event.status = :publishedStatus
              and event.publishedAt < :cutoff
            order by event.id
            """)
    List<Long> findPublishedRetentionCandidateIds(
            @Param("publishedStatus") FraudOutboxStatus publishedStatus,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable
    );

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from FraudOutboxEvent event
            where event.id in :ids
              and event.status = :publishedStatus
              and event.publishedAt < :cutoff
            """)
    int deletePublishedRetentionCandidates(
            @Param("ids") Collection<Long> ids,
            @Param("publishedStatus") FraudOutboxStatus publishedStatus,
            @Param("cutoff") LocalDateTime cutoff
    );
}
