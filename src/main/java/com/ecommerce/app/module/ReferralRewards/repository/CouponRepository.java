package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @Query("""
            SELECT c
            FROM Coupon c
            WHERE c.deleted IS NULL OR c.deleted = false
            ORDER BY c.id DESC
            """)
    List<Coupon> findAllForAdminList();

    @Query("""
            SELECT c
            FROM Coupon c
            WHERE c.status = :status
              AND (c.deleted IS NULL OR c.deleted = false)
              AND (c.startDate IS NULL OR c.startDate <= :now)
              AND (c.expiryDate IS NULL OR c.expiryDate > :now)
            ORDER BY c.id DESC
            """)
    List<Coupon> findActiveCoupons(@Param("status") CouponStatus status, @Param("now") LocalDateTime now);
}
