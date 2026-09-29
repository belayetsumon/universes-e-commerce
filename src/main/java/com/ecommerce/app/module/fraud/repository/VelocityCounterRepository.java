package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.VelocityCounter;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface VelocityCounterRepository extends JpaRepository<VelocityCounter, Long> {

    Optional<VelocityCounter> findByCounterScopeAndCounterValueHashAndWindowStartAtAndWindowEndAt(
            VelocityCounterScope counterScope, String counterValueHash, LocalDateTime windowStartAt, LocalDateTime windowEndAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select counter
            from VelocityCounter counter
            where counter.counterScope = :scope
              and counter.counterValueHash = :valueHash
              and counter.windowStartAt = :windowStart
              and counter.windowEndAt = :windowEnd
            """)
    Optional<VelocityCounter> findBucketForUpdate(
            @Param("scope") VelocityCounterScope scope,
            @Param("valueHash") String valueHash,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd
    );

    long countByCounterScopeAndCounterValueHashAndWindowEndAtAfter(
            VelocityCounterScope counterScope, String counterValueHash, LocalDateTime after);

    @Query("""
            select coalesce(sum(counter.counterCount), 0)
            from VelocityCounter counter
            where counter.counterScope = :scope
              and counter.counterValueHash = :valueHash
              and counter.windowEndAt > :windowStart
            """)
    long sumCountWithinWindow(
            @Param("scope") VelocityCounterScope scope,
            @Param("valueHash") String valueHash,
            @Param("windowStart") LocalDateTime windowStart
    );

    @Query("""
            select coalesce(sum(counter.counterCount), 0)
            from VelocityCounter counter
            where counter.counterScope = :scope
              and counter.counterValueHash = :valueHash
              and counter.windowEndAt > :windowStart
              and counter.windowStartAt < :currentBucketStart
            """)
    long sumCountBeforeCurrentBucketWithinWindow(
            @Param("scope") VelocityCounterScope scope,
            @Param("valueHash") String valueHash,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("currentBucketStart") LocalDateTime currentBucketStart
    );

    @Transactional(readOnly = true)
    @Query("""
            select counter.id
            from VelocityCounter counter
            where counter.windowEndAt < :cutoff
            order by counter.id
            """)
    List<Long> findRetentionCandidateIds(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from VelocityCounter counter
            where counter.id in :ids
              and counter.windowEndAt < :cutoff
            """)
    int deleteRetentionCandidates(
            @Param("ids") Collection<Long> ids,
            @Param("cutoff") LocalDateTime cutoff
    );
}
