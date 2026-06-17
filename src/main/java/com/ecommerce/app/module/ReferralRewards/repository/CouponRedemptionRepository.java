package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.model.CouponRedemption;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    @Query("""
            SELECT cr
            FROM CouponRedemption cr
            JOIN FETCH cr.coupon c
            JOIN FETCH cr.user u
            ORDER BY cr.id DESC
            """)
    List<CouponRedemption> findAllForAdminList();

    List<CouponRedemption> findByUserOrderByIdDesc(Users user);

    boolean existsByCouponAndOrderId(Coupon coupon, String orderId);

    @Query("""
            SELECT COUNT(cr)
            FROM CouponRedemption cr
            WHERE cr.coupon = :coupon
            """)
    long countByCoupon(@Param("coupon") Coupon coupon);

    long countByCouponAndUser(Coupon coupon, Users user);
}
