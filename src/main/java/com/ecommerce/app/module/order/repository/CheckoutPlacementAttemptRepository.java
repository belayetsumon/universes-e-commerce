package com.ecommerce.app.module.order.repository;

import com.ecommerce.app.module.order.model.CheckoutPlacementAttempt;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckoutPlacementAttemptRepository extends JpaRepository<CheckoutPlacementAttempt, Long> {

    Optional<CheckoutPlacementAttempt> findByActorScopeHashAndRequestKeyHash(
            String actorScopeHash,
            String requestKeyHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select attempt
            from CheckoutPlacementAttempt attempt
            where attempt.actorScopeHash = :actorScopeHash
              and attempt.requestKeyHash = :requestKeyHash
            """)
    Optional<CheckoutPlacementAttempt> findForUpdate(
            @Param("actorScopeHash") String actorScopeHash,
            @Param("requestKeyHash") String requestKeyHash
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CheckoutPlacementAttempt attempt
            set attempt.status = :failedStatus,
                attempt.lockedUntil = null,
                attempt.failureReason = :failureReason,
                attempt.updatedAt = :updatedAt
            where attempt.status = :processingStatus
              and attempt.lockedUntil is not null
              and attempt.lockedUntil < :staleBefore
            """)
    int markStaleProcessingAttemptsFailed(
            @Param("processingStatus") com.ecommerce.app.module.order.model.CheckoutPlacementAttemptStatus processingStatus,
            @Param("failedStatus") com.ecommerce.app.module.order.model.CheckoutPlacementAttemptStatus failedStatus,
            @Param("staleBefore") LocalDateTime staleBefore,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("failureReason") String failureReason
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from CheckoutPlacementAttempt attempt
            where attempt.status in :terminalStatuses
              and attempt.updatedAt < :retentionCutoff
            """)
    int deleteTerminalAttemptsBefore(
            @Param("terminalStatuses") Collection<com.ecommerce.app.module.order.model.CheckoutPlacementAttemptStatus> terminalStatuses,
            @Param("retentionCutoff") LocalDateTime retentionCutoff
    );
}
