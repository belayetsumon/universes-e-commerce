package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicy;
import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicyStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CashbackPolicyRepository extends JpaRepository<CashbackPolicy, Long> {

    @Query("""
            SELECT p
            FROM CashbackPolicy p
            ORDER BY p.id DESC
            """)
    List<CashbackPolicy> findAllForAdminList();

    @Query("""
            SELECT p
            FROM CashbackPolicy p
            WHERE p.status = :status
              AND (p.startDate IS NULL OR p.startDate <= :now)
              AND (p.endDate IS NULL OR p.endDate >= :now)
            ORDER BY p.id DESC
            """)
    List<CashbackPolicy> findActivePolicies(@Param("status") CashbackPolicyStatus status, @Param("now") LocalDateTime now);
}

