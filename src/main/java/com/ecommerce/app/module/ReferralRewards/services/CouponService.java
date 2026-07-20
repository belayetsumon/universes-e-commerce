package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.model.CouponRedemption;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponScope;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponRedemptionStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CouponType;
import com.ecommerce.app.module.ReferralRewards.repository.CouponRedemptionRepository;
import com.ecommerce.app.module.ReferralRewards.repository.CouponRepository;
import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.product.model.Manufacturer;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.vendor.model.Vendorprofile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
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
                .filter(c -> isUsable(c, effectiveNow))
                .filter(this::isCheckoutDiscountCoupon);
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

        if (coupon.getType() == CouponType.PERCENTAGE) {
            BigDecimal percent = value.max(BigDecimal.ZERO).min(new BigDecimal("100"));
            BigDecimal discount = subtotal.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (maxDiscount.compareTo(BigDecimal.ZERO) > 0) {
                discount = discount.min(maxDiscount);
            }
            return discount.min(subtotal).max(BigDecimal.ZERO);
        }

        if (coupon.getType() != CouponType.FIXED) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discount = value.setScale(2, RoundingMode.HALF_UP);
        if (maxDiscount.compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(maxDiscount);
        }
        return discount.min(subtotal).max(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public BigDecimal computeEligibleSubtotal(Coupon coupon, List<CartItem> cartItems) {
        if (coupon == null || cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        CouponScope scope = coupon.getScope() == null ? CouponScope.GLOBAL : coupon.getScope();
        if (scope == CouponScope.GLOBAL || scope == CouponScope.CUSTOMER || scope == CouponScope.CUSTOMER_GROUP || scope == CouponScope.CAMPAIGN) {
            return cartItems.stream()
                    .map(item -> safeMoney(item == null ? null : item.getItemTotal()))
                    .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
        }

        Set<String> targets = resolveScopeTargets(coupon);
        if (targets.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return cartItems.stream()
                .filter(item -> matchesCartScope(scope, item, targets))
                .map(item -> safeMoney(item == null ? null : item.getItemTotal()))
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public void validateCouponScope(Coupon coupon, Users user, List<CartItem> cartItems) {
        CouponScope scope = coupon == null || coupon.getScope() == null ? CouponScope.GLOBAL : coupon.getScope();
        if (scope == CouponScope.GLOBAL) {
            return;
        }

        Set<String> targets = resolveScopeTargets(coupon);
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("Coupon scope target is not configured.");
        }

        if (scope == CouponScope.CUSTOMER && !matchesCustomerScope(user, targets)) {
            throw new IllegalArgumentException("This coupon is not available for this customer.");
        }
        if (scope == CouponScope.CUSTOMER_GROUP && !matchesCustomerGroupScope(user, targets)) {
            throw new IllegalArgumentException("This coupon is not available for this customer group.");
        }
        if (scope == CouponScope.VENDOR || scope == CouponScope.PRODUCT || scope == CouponScope.CATEGORY || scope == CouponScope.BRAND) {
            if (computeEligibleSubtotal(coupon, cartItems).compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("This coupon does not apply to the selected cart items.");
            }
        }
    }

    @Transactional(readOnly = true, noRollbackFor = IllegalArgumentException.class)
    public void validateCheckoutCoupon(Coupon coupon, Users user, BigDecimal orderSubtotal) {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon is required.");
        }
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (isDeleted(coupon)) {
            throw new IllegalArgumentException("Coupon is unavailable.");
        }
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new IllegalArgumentException("Coupon is not active.");
        }
        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(now)) {
            throw new IllegalArgumentException("Coupon is not active yet.");
        }
        if (coupon.getExpiryDate() != null && !coupon.getExpiryDate().isAfter(now)) {
            throw new IllegalArgumentException("Coupon is expired.");
        }
        if (!hasUsageRemaining(coupon)) {
            throw new IllegalArgumentException("Coupon usage limit reached.");
        }
        if (!isCheckoutDiscountCoupon(coupon)) {
            throw new IllegalArgumentException("This coupon type is not supported at checkout yet.");
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
        if (coupon.getId() == null) {
            throw new IllegalArgumentException("Coupon id is required.");
        }
        BigDecimal normalizedDiscount = discountAmount == null ? BigDecimal.ZERO : discountAmount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        if (normalizedDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Coupon discount amount must be greater than zero.");
        }

        Coupon locked = couponRepository.findById(coupon.getId())
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found."));
        if (couponRedemptionRepository.existsByCouponAndOrderId(locked, orderId)) {
            throw new IllegalStateException("Coupon already redeemed for this order.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (isDeleted(locked)) {
            throw new IllegalArgumentException("Coupon is unavailable.");
        }
        if (locked.getStatus() != CouponStatus.ACTIVE) {
            throw new IllegalArgumentException("Coupon is not active.");
        }
        if (!isCheckoutDiscountCoupon(locked)) {
            throw new IllegalArgumentException("This coupon type is not supported at checkout yet.");
        }
        if (locked.getStartDate() != null && locked.getStartDate().isAfter(now)) {
            throw new IllegalArgumentException("Coupon is not active yet.");
        }
        if (locked.getExpiryDate() != null && !locked.getExpiryDate().isAfter(now)) {
            throw new IllegalArgumentException("Coupon is expired.");
        }
        if (!hasUsageRemaining(locked)) {
            throw new IllegalArgumentException("Coupon usage limit reached.");
        }
        if (locked.getPerUserUsageLimit() > 0
                && couponRedemptionRepository.countByCouponAndUser(locked, user) >= locked.getPerUserUsageLimit()) {
            throw new IllegalArgumentException("Coupon usage limit reached for this customer.");
        }

        long effectiveTimesUsed = effectiveTimesUsed(locked);
        locked.setTimesUsed(toIntUsageCount(effectiveTimesUsed + 1));
        couponRepository.save(locked);

        CouponRedemption redemption = new CouponRedemption();
        redemption.setCoupon(locked);
        redemption.setUser(user);
        redemption.setOrderId(orderId);
        redemption.setDiscountAmount(normalizedDiscount);
        redemption.setCurrency("BDT");
        redemption.setStatus(CouponRedemptionStatus.APPLIED);
        redemption.setIdempotencyKey("COUPON:" + locked.getId() + ":ORDER:" + orderId);
        redemption.setRedeemedAt(now);
        return couponRedemptionRepository.save(redemption);
    }

    private boolean isUsable(Coupon coupon, LocalDateTime now) {
        return coupon != null
                && !isDeleted(coupon)
                && coupon.getStatus() == CouponStatus.ACTIVE
                && (coupon.getStartDate() == null || !coupon.getStartDate().isAfter(now))
                && (coupon.getExpiryDate() == null || coupon.getExpiryDate().isAfter(now))
                && hasUsageRemaining(coupon);
    }

    private boolean isCheckoutDiscountCoupon(Coupon coupon) {
        return coupon != null
                && (coupon.getType() == CouponType.FIXED || coupon.getType() == CouponType.PERCENTAGE);
    }

    private boolean isDeleted(Coupon coupon) {
        return coupon != null && Boolean.TRUE.equals(coupon.getDeleted());
    }

    private boolean hasUsageRemaining(Coupon coupon) {
        if (coupon == null || coupon.getUsageLimit() <= 0) {
            return true;
        }
        return effectiveTimesUsed(coupon) < coupon.getUsageLimit();
    }

    private long effectiveTimesUsed(Coupon coupon) {
        if (coupon == null || coupon.getId() == null) {
            return 0L;
        }
        long storedTimesUsed = Math.max(0, coupon.getTimesUsed());
        long redeemedCount = couponRedemptionRepository.countByCoupon(coupon);
        return Math.max(storedTimesUsed, redeemedCount);
    }

    private int toIntUsageCount(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(0, value);
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private Set<String> resolveScopeTargets(Coupon coupon) {
        CouponScope scope = coupon == null || coupon.getScope() == null ? CouponScope.GLOBAL : coupon.getScope();
        String rawTargets = scope == CouponScope.VENDOR ? coupon.getVendorScope() : coupon.getCampaignScope();
        return parseScopeTargets(rawTargets);
    }

    private Set<String> parseScopeTargets(String value) {
        Set<String> targets = new HashSet<>();
        if (value == null || value.isBlank()) {
            return targets;
        }

        Arrays.stream(value.split("[,;|\\n\\r\\t]+"))
                .map(this::normalizeToken)
                .filter(token -> !token.isBlank())
                .forEach(targets::add);
        return targets;
    }

    private boolean matchesCartScope(CouponScope scope, CartItem item, Set<String> targets) {
        if (item == null || targets == null || targets.isEmpty()) {
            return false;
        }

        Product product = item.getProduct();
        switch (scope) {
            case VENDOR:
                return matchesVendor(item, product, targets);
            case PRODUCT:
                return matchesProduct(item, product, targets);
            case CATEGORY:
                return matchesCategory(product == null ? null : product.getProductcategory(), targets);
            case BRAND:
                return matchesManufacturer(product == null ? null : product.getManufacturer(), targets);
            default:
                return true;
        }
    }

    private boolean matchesVendor(CartItem item, Product product, Set<String> targets) {
        if (matchesAny(targets, item.getVendorId(), item.getVendorUuid())) {
            return true;
        }

        Vendorprofile vendor = product == null ? null : product.getVendorprofile();
        return vendor != null && matchesAny(targets,
                vendor.getId(),
                vendor.getUuid(),
                vendor.getVendorCode(),
                vendor.getCompanyName(),
                vendor.getEmail());
    }

    private boolean matchesProduct(CartItem item, Product product, Set<String> targets) {
        if (matchesAny(targets, item.getProductId(), item.getProductUuid())) {
            return true;
        }

        return product != null && matchesAny(targets,
                product.getId(),
                product.getUuid(),
                product.getSku(),
                product.getSlug(),
                product.getTitle());
    }

    private boolean matchesCategory(Productcategory category, Set<String> targets) {
        Productcategory current = category;
        while (current != null) {
            if (matchesAny(targets,
                    current.getId(),
                    current.getUuid(),
                    current.getSlug(),
                    current.getName())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private boolean matchesManufacturer(Manufacturer manufacturer, Set<String> targets) {
        return manufacturer != null && matchesAny(targets,
                manufacturer.getId(),
                manufacturer.getSlug(),
                manufacturer.getName());
    }

    private boolean matchesCustomerScope(Users user, Set<String> targets) {
        return user != null && matchesAny(targets,
                user.getId(),
                user.getUuid(),
                user.getEmail(),
                user.getMobile());
    }

    private boolean matchesCustomerGroupScope(Users user, Set<String> targets) {
        if (user == null) {
            return false;
        }
        if (user.getUserType() != null && targets.contains(normalizeToken(user.getUserType().name()))) {
            return true;
        }
        if (user.getRole() == null) {
            return false;
        }
        return user.getRole().stream()
                .anyMatch(role -> role != null && matchesAny(targets,
                role.getId(),
                role.getName(),
                role.getSlug()));
    }

    private boolean matchesAny(Set<String> targets, Object... values) {
        if (targets == null || targets.isEmpty() || values == null) {
            return false;
        }
        return Arrays.stream(values)
                .map(this::normalizeValue)
                .anyMatch(value -> !value.isBlank() && targets.contains(value));
    }

    private String normalizeValue(Object value) {
        return value == null ? "" : normalizeToken(String.valueOf(value));
    }

    private String normalizeToken(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ENGLISH);
    }
}
