package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicy;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackPolicyStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CashbackPolicyRepository extends JpaRepository<CashbackPolicy, Long> {

    @Query("""
            SELECT DISTINCT p
            FROM CashbackPolicy p
            LEFT JOIN FETCH p.categoryIds
            ORDER BY p.id DESC
            """)
    List<CashbackPolicy> findAllForAdminList();

    @Query("""
            SELECT p
            FROM CashbackPolicy p
            LEFT JOIN FETCH p.categoryIds
            WHERE p.id = :id
            """)
    Optional<CashbackPolicy> findByIdWithCategories(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT p
            FROM CashbackPolicy p
            LEFT JOIN FETCH p.categoryIds
            WHERE p.status = :status
              AND (p.startDate IS NULL OR p.startDate <= :now)
              AND (p.endDate IS NULL OR p.endDate >= :now)
            ORDER BY p.id DESC
            """)
    List<CashbackPolicy> findActivePolicies(@Param("status") CashbackPolicyStatus status, @Param("now") LocalDateTime now);
}
