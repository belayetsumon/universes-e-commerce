package com.ecommerce.app.module.communication.repository;

import com.ecommerce.app.module.communication.model.MessageJob;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageJobRepository extends JpaRepository<MessageJob, Long>, JpaSpecificationExecutor<MessageJob> {

    Optional<MessageJob> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("""
            select job
            from MessageJob job
            where job.scheduledAt <= :scheduledAt
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
}
