package com.ecommerce.app.module.order.repository;

import com.ecommerce.app.module.order.model.CheckoutPlacementAttempt;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
}
