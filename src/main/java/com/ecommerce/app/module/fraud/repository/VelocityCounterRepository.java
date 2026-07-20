package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.VelocityCounter;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VelocityCounterRepository extends JpaRepository<VelocityCounter, Long> {

    Optional<VelocityCounter> findByCounterScopeAndCounterValueHashAndWindowStartAtAndWindowEndAt(
            VelocityCounterScope counterScope, String counterValueHash, LocalDateTime windowStartAt, LocalDateTime windowEndAt);

    long countByCounterScopeAndCounterValueHashAndWindowEndAtAfter(
            VelocityCounterScope counterScope, String counterValueHash, LocalDateTime after);
}
