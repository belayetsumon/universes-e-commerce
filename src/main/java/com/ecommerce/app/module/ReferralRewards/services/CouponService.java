package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.model.CouponRedemption;
import com.ecommerce.app.module.ReferralRewards.model.CouponStatus;
import com.ecommerce.app.module.ReferralRewards.model.CouponType;
import com.ecommerce.app.module.ReferralRewards.repository.CouponRedemptionRepository;
import com.ecommerce.app.module.ReferralRewards.repository.CouponRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final SalesOrderRepository salesOrderRepository;

    public CouponService(CouponRepository couponRepository, CouponRedemptionRepository couponRedemptionRepository,
            SalesOrderRepository salesOrderRepository) {
        this.couponRepository = couponRepository;
        this.couponRedemptionRepository = couponRedemptionRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Coupon> findValidCouponByCode(String code, LocalDateTime now) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        LocalDateTime effectiveNow = now != null ? now : LocalDateTime.now();
        return couponRepository.findByCodeIgnoreCase(code.trim())
                .filter(c -> c.getStatus() == CouponStatus.ACTIVE)
                .filter(c -> c.getStartDate() == null || !c.getStartDate().isAfter(effectiveNow))
                .filter(c -> c.getExpiryDate() == null || c.getExpiryDate().isAfter(effectiveNow))
                .filter(c -> c.getUsageLimit() <= 0 || c.getTimesUsed() < c.getUsageLimit());
    }

    @Transactional(readOnly = true)
    public BigDecimal computeDiscount(Coupon coupon, BigDecimal orderSubtotal) {
        if (coupon == null || orderSubtotal == null || orderSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal subtotal = orderSubtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal minimumOrder = coupon.getMinimumOrder() == null ? BigDecimal.ZERO : coupon.getMinimumOrder();
        if (minimumOrder.compareTo(BigDecimal.ZERO) > 0 && subtotal.compareTo(minimumOrder) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal value = coupon.getValue() == null ? BigDecimal.ZERO : coupon.getValue();
        BigDecimal maxDiscount = coupon.getMaxDiscount() == null ? BigDecimal.ZERO : coupon.getMaxDiscount();

        if (coupon.getType() == CouponType.PERCENT) {
            BigDecimal percent = value.max(BigDecimal.ZERO).min(new BigDecimal("100"));
            BigDecimal discount = subtotal.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (maxDiscount.compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(maxDiscount);
            }
            return discount.min(subtotal).max(BigDecimal.ZERO);
        }

        BigDecimal discount = value.setScale(2, RoundingMode.HALF_UP);
        if (maxDiscount.compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(maxDiscount);
        }
        return discount.min(subtotal).max(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public void validateCheckoutCoupon(Coupon coupon, Users user, BigDecimal orderSubtotal) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon is required.");
        }
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        if (coupon.getPerUserUsageLimit() > 0
                && couponRedemptionRepository.countByCouponAndUser(coupon, user) >= coupon.getPerUserUsageLimit()) {
            throw new IllegalArgumentException("Coupon usage limit reached for this customer.");
        }

        if ((coupon.isFirstOrderOnly() || coupon.isNewCustomerOnly())
                && salesOrderRepository.countByCustomer(user) > 0) {
            throw new IllegalArgumentException("This coupon is available only for a customer's first order.");
        }

        if (computeDiscount(coupon, orderSubtotal).compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal minimumOrder = coupon.getMinimumOrder() == null ? BigDecimal.ZERO : coupon.getMinimumOrder();
            if (minimumOrder.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("Minimum order amount for this coupon is " + minimumOrder.setScale(2, RoundingMode.HALF_UP) + " BDT.");
            }
            throw new IllegalArgumentException("Coupon does not apply to this order.");
        }
    }

    @Transactional
    public CouponRedemption redeemCoupon(Coupon coupon, Users user, String orderId, BigDecimal discountAmount) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon is required.");
        }
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order id is required.");
        }
        BigDecimal normalizedDiscount = discountAmount == null ? BigDecimal.ZERO : discountAmount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        if (couponRedemptionRepository.existsByCouponAndOrderId(coupon, orderId)) {
            throw new IllegalStateException("Coupon already redeemed for this order.");
        }

        Coupon locked = couponRepository.findById(coupon.getId())
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found."));

        if (locked.getStatus() != CouponStatus.ACTIVE) {
            throw new IllegalArgumentException("Coupon is not active.");
        }
        if (locked.getStartDate() != null && locked.getStartDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Coupon is not active yet.");
        }
        if (locked.getExpiryDate() != null && !locked.getExpiryDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Coupon is expired.");
        }
        if (locked.getUsageLimit() > 0 && locked.getTimesUsed() >= locked.getUsageLimit()) {
            throw new IllegalArgumentException("Coupon usage limit reached.");
        }
        if (locked.getPerUserUsageLimit() > 0
                && couponRedemptionRepository.countByCouponAndUser(locked, user) >= locked.getPerUserUsageLimit()) {
            throw new IllegalArgumentException("Coupon usage limit reached for this customer.");
        }

        locked.setTimesUsed(locked.getTimesUsed() + 1);
        couponRepository.save(locked);

        CouponRedemption redemption = new CouponRedemption();
        redemption.setCoupon(locked);
        redemption.setUsers(user);
        redemption.setOrderId(orderId);
        redemption.setDiscountAmount(normalizedDiscount);
        return couponRedemptionRepository.save(redemption);
    }
}
