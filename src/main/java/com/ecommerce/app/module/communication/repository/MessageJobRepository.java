package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.MessageJob;
import com.ecommerce.app.module.communication.model.MessageEventType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MessageJobRepository extends JpaRepository<MessageJob, Long>, JpaSpecificationExecutor<MessageJob> {

    Optional<MessageJob> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Transactional(readOnly = true)
    @Query("""
            select job
            from MessageJob job
            where job.scheduledAt <= :scheduledAt
              and job.eventType <> com.ecommerce.app.module.communication.model.MessageEventType.GUEST_CHECKOUT_OTP
              and job.eventType <> com.ecommerce.app.module.communication.model.MessageEventType.CUSTOMER_COD_OTP
              and (
                    job.status = com.ecommerce.app.module.communication.model.MessageStatus.QUEUED
                    or (
                        job.status = com.ecommerce.app.module.communication.model.MessageStatus.FAILED
                        and job.retryCount < :maxRetryCount
                    )
              )
            order by job.scheduledAt asc, job.id asc
            """)
    List<MessageJob> findRetryableJobs(
            @Param("scheduledAt") LocalDateTime scheduledAt,
            @Param("maxRetryCount") int maxRetryCount,
            Pageable pageable);

    @Query("""
            select job.id
            from MessageJob job
            where job.eventType in :eventTypes
            order by job.id asc
            """)
    List<Long> findCodOtpRetentionCandidateIds(
            @Param("eventTypes") Collection<MessageEventType> eventTypes,
            Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from MessageJob job
            where job.id in :ids
              and job.eventType in :eventTypes
            """)
    int deleteCodOtpRetentionCandidates(
            @Param("ids") Collection<Long> ids,
            @Param("eventTypes") Collection<MessageEventType> eventTypes);
}
