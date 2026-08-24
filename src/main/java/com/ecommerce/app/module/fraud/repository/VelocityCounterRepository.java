package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.VelocityCounter;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import java.time.LocalDateTime;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
              and counter.windowStartAt >= :windowStart
            """)
    long sumCountWithinWindow(
            @Param("scope") VelocityCounterScope scope,
            @Param("valueHash") String valueHash,
            @Param("windowStart") LocalDateTime windowStart
    );
}
