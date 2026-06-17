package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.dto.CheckoutIncentiveQuote;
import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.model.CouponRedemption;
import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardTransaction;
import com.ecommerce.app.module.ReferralRewards.model.OrderIncentiveUsage;
import com.ecommerce.app.module.ReferralRewards.model.RedemptionStatus;
import com.ecommerce.app.module.ReferralRewards.model.RedemptionType;
import com.ecommerce.app.module.ReferralRewards.model.RewardTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.OrderIncentiveUsageRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
import com.ecommerce.app.module.cart.model.CartItem;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.order.model.SalesOrder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutIncentiveService {

    private static final BigDecimal REWARD_POINT_TO_CURRENCY_RATE = new BigDecimal("0.01");

    @Autowired
    private CouponService couponService;

    @Autowired
    private GiftCardService giftCardService;

    @Autowired
    private RewardAccountRepository rewardAccountRepository;

    @Autowired
    private RedemptionService redemptionService;

    @Autowired
    private OrderIncentiveUsageRepository orderIncentiveUsageRepository;

    @Autowired
    private RewardTransactionRepository rewardTransactionRepository;

    @Transactional(readOnly = true)
    public CheckoutIncentiveQuote prepareQuote(Users user, List<CartItem> cartItems, BigDecimal grossPayable,
            String couponCode, BigDecimal rewardPointsToUse, String giftCardCode, BigDecimal giftCardAmount) {
        CheckoutIncentiveQuote quote = new CheckoutIncentiveQuote();

        BigDecimal normalizedGross = safeMoney(grossPayable);
        quote.setGrossPayable(normalizedGross);
        quote.setNetPayable(normalizedGross);
        quote.setCouponCode(trimToNull(couponCode));
        quote.setGiftCardCode(trimToNull(giftCardCode));

        BigDecimal orderSubtotal = cartItems == null
                ? BigDecimal.ZERO
                : cartItems.stream()
                        .map(item -> item.getItemTotal() != null ? item.getItemTotal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        String normalizedCouponCode = trimToNull(couponCode);
        if (normalizedCouponCode != null) {
            Coupon coupon = couponService.findValidCouponByCode(normalizedCouponCode, LocalDateTime.now())
                    .orElseThrow(() -> new IllegalArgumentException("Coupon code is invalid or unavailable."));
            couponService.validateCheckoutCoupon(coupon, user, orderSubtotal);
            BigDecimal couponDiscount = safeMoney(couponService.computeDiscount(coupon, orderSubtotal))
                    .min(normalizedGross);
            quote.setCoupon(coupon);
            quote.setCouponDiscount(couponDiscount);
            quote.setNetPayable(normalizedGross.subtract(couponDiscount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        }

        BigDecimal remainingAfterCoupon = quote.getNetPayable();
        BigDecimal normalizedRewardPoints = safeMoney(rewardPointsToUse).max(BigDecimal.ZERO);
        if (normalizedRewardPoints.compareTo(BigDecimal.ZERO) > 0
                && remainingAfterCoupon.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal availableRewardPoints = rewardAccountRepository.findByUsers(user)
                    .map(account -> account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);
            if (availableRewardPoints.compareTo(normalizedRewardPoints) < 0) {
                throw new IllegalArgumentException("Reward points balance is not enough for this checkout.");
            }

            BigDecimal maxRedeemablePoints = remainingAfterCoupon
                    .divide(REWARD_POINT_TO_CURRENCY_RATE, 2, RoundingMode.DOWN);
            BigDecimal actualRewardPointsUsed = normalizedRewardPoints.min(maxRedeemablePoints);
            BigDecimal rewardDiscount = actualRewardPointsUsed
                    .multiply(REWARD_POINT_TO_CURRENCY_RATE)
                    .setScale(2, RoundingMode.HALF_UP)
                    .min(remainingAfterCoupon);
            if (rewardDiscount.compareTo(BigDecimal.ZERO) > 0
                    && actualRewardPointsUsed.compareTo(BigDecimal.ZERO) > 0) {
                quote.setRewardPointsUsed(actualRewardPointsUsed);
                quote.setRewardDiscount(rewardDiscount);
                quote.setNetPayable(remainingAfterCoupon.subtract(rewardDiscount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            }
        }

        BigDecimal remainingAfterReward = quote.getNetPayable();
        String normalizedGiftCardCode = trimToNull(giftCardCode);
        if (normalizedGiftCardCode != null && remainingAfterReward.compareTo(BigDecimal.ZERO) > 0) {
            GiftCard giftCard = giftCardService.findUsableGiftCard(normalizedGiftCardCode)
                    .orElseThrow(() -> new IllegalArgumentException("Gift card code is invalid or unavailable."));
            BigDecimal requestedGiftAmount = safeMoney(giftCardAmount);
            if (requestedGiftAmount.compareTo(BigDecimal.ZERO) <= 0) {
                requestedGiftAmount = remainingAfterReward;
            }

            BigDecimal availableGiftBalance = giftCard.getBalance() == null ? BigDecimal.ZERO : giftCard.getBalance().setScale(2, RoundingMode.HALF_UP);
            BigDecimal appliedGiftAmount = requestedGiftAmount.min(availableGiftBalance).min(remainingAfterReward).max(BigDecimal.ZERO);
            if (appliedGiftAmount.compareTo(BigDecimal.ZERO) > 0) {
                quote.setGiftCard(giftCard);
                quote.setGiftCardUsed(appliedGiftAmount);
                quote.setNetPayable(remainingAfterReward.subtract(appliedGiftAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            }
        }

        return quote;
    }

    @Transactional
    public CheckoutIncentiveQuote applyQuoteToOrders(CheckoutIncentiveQuote quote, Users user, List<SalesOrder> orders) {
        if (quote == null || orders == null || orders.isEmpty()) {
            return quote;
        }

        Map<Long, BigDecimal> couponDiscountByOrder = allocateAcrossOrders(orders, quote.getCouponDiscount());
        Map<Long, BigDecimal> rewardDiscountByOrder = allocateAcrossOrders(orders, quote.getRewardDiscount());
        Map<Long, BigDecimal> rewardPointsByOrder = allocateAcrossOrders(orders, quote.getRewardPointsUsed());
        Map<Long, BigDecimal> giftCardUsedByOrder = allocateAcrossOrders(orders, quote.getGiftCardUsed());

        quote.getCouponDiscountByOrder().putAll(couponDiscountByOrder);
        quote.getRewardDiscountByOrder().putAll(rewardDiscountByOrder);
        quote.getRewardPointsByOrder().putAll(rewardPointsByOrder);
        quote.getGiftCardUsedByOrder().putAll(giftCardUsedByOrder);

        String orderReference = buildOrderReference(orders);
        String rewardIdempotencyKey = "CHECKOUT:" + orderReference + ":REWARD_POINTS";
        Long rewardTransactionId = null;
        if (quote.getRewardPointsUsed().compareTo(BigDecimal.ZERO) > 0) {
            boolean redeemed = redemptionService.redeemPoints(
                    user,
                    quote.getRewardPointsUsed(),
                    "ORDER_DISCOUNT",
                    "Applied reward points during checkout",
                    orderReference,
                    "CHECKOUT",
                    "REWARD_POINTS",
                    REWARD_POINT_TO_CURRENCY_RATE,
                    rewardIdempotencyKey
            );
            if (!redeemed) {
                throw new IllegalStateException("Reward points could not be redeemed for checkout.");
            }
            rewardTransactionId = rewardTransactionRepository.findByIdempotencyKey(rewardIdempotencyKey)
                    .map(RewardTransaction::getId)
                    .orElse(null);
        }

        CouponRedemption couponRedemption = null;
        if (quote.getCoupon() != null && quote.getCouponDiscount().compareTo(BigDecimal.ZERO) > 0) {
            String primaryOrderId = String.valueOf(orders.get(0).getId());
            couponRedemption = couponService.redeemCoupon(quote.getCoupon(), user, primaryOrderId, quote.getCouponDiscount());
        }

        Map<Long, Long> giftCardTransactionIdByOrder = new HashMap<>();
        for (SalesOrder order : orders) {
            BigDecimal netGrandTotal = safeMoney(order.getGrandTotal())
                    .subtract(safeMoney(couponDiscountByOrder.get(order.getId())))
                    .subtract(safeMoney(rewardDiscountByOrder.get(order.getId())))
                    .subtract(safeMoney(giftCardUsedByOrder.get(order.getId())));
            if (netGrandTotal.compareTo(BigDecimal.ZERO) < 0) {
                netGrandTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            order.setGrandTotal(netGrandTotal);

            BigDecimal orderCouponDiscount = safeMoney(couponDiscountByOrder.get(order.getId()));
            if (quote.getCoupon() != null && orderCouponDiscount.compareTo(BigDecimal.ZERO) > 0) {
                redemptionService.recordRedemption(
                        user,
                        RedemptionType.COUPON,
                        null,
                        orderCouponDiscount,
                        "BDT",
                        String.valueOf(order.getId()),
                        "CHECKOUT",
                        "COUPON:" + quote.getCoupon().getId(),
                        null,
                        RedemptionStatus.SUCCESS,
                        "Coupon applied during checkout: " + quote.getCouponCode()
                );
            }
        }

        if (quote.getGiftCard() != null && quote.getGiftCardUsed().compareTo(BigDecimal.ZERO) > 0) {
            for (SalesOrder order : orders) {
                BigDecimal requested = safeMoney(giftCardUsedByOrder.get(order.getId()));
                if (requested.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                GiftCardTransaction giftCardTransaction = giftCardService.applyToOrderWithTransaction(quote.getGiftCard(), user, order, requested);
                BigDecimal applied = giftCardTransaction == null
                        ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                        : giftCardTransaction.getAmountUsed();
                if (applied.compareTo(requested) != 0) {
                    throw new IllegalStateException("Gift card amount changed during checkout. Please try again.");
                }
                giftCardTransactionIdByOrder.put(order.getId(), giftCardTransaction.getId());

                redemptionService.recordRedemption(
                        user,
                        RedemptionType.GIFT_CARD,
                        null,
                        applied,
                        "BDT",
                        String.valueOf(order.getId()),
                        "CHECKOUT",
                        "GIFT_CARD:" + quote.getGiftCard().getId(),
                        null,
                        RedemptionStatus.SUCCESS,
                        "Gift card applied during checkout: " + quote.getGiftCardCode()
                );
            }
        }

        for (SalesOrder order : orders) {
            saveOrderIncentiveUsage(
                    order,
                    quote,
                    orderReference,
                    safeMoney(couponDiscountByOrder.get(order.getId())),
                    safeMoney(rewardPointsByOrder.get(order.getId())),
                    safeMoney(rewardDiscountByOrder.get(order.getId())),
                    safeMoney(giftCardUsedByOrder.get(order.getId())),
                    couponRedemption == null ? null : couponRedemption.getId(),
                    rewardTransactionId,
                    giftCardTransactionIdByOrder.get(order.getId())
            );
        }

        return quote;
    }

    private void saveOrderIncentiveUsage(SalesOrder order, CheckoutIncentiveQuote quote, String quoteReference,
            BigDecimal couponDiscount, BigDecimal rewardPointsUsed, BigDecimal rewardDiscount, BigDecimal giftCardUsed,
            Long couponRedemptionId, Long rewardTransactionId, Long giftCardTransactionId) {
        boolean hasIncentive = couponDiscount.compareTo(BigDecimal.ZERO) > 0
                || rewardPointsUsed.compareTo(BigDecimal.ZERO) > 0
                || rewardDiscount.compareTo(BigDecimal.ZERO) > 0
                || giftCardUsed.compareTo(BigDecimal.ZERO) > 0;
        if (!hasIncentive || order == null || order.getId() == null) {
            return;
        }

        String orderId = String.valueOf(order.getId());
        Optional<OrderIncentiveUsage> existing = orderIncentiveUsageRepository.findByOrderId(orderId);
        OrderIncentiveUsage usage = existing.orElseGet(OrderIncentiveUsage::new);
        usage.setOrderId(orderId);
        usage.setCouponId(quote.getCoupon() == null ? null : quote.getCoupon().getId());
        usage.setCouponCode(quote.getCouponCode());
        usage.setCouponDiscount(couponDiscount);
        usage.setRewardPointsUsed(rewardPointsUsed);
        usage.setRewardDiscount(rewardDiscount);
        usage.setRewardTransactionId(rewardTransactionId);
        usage.setGiftCardCode(quote.getGiftCardCode());
        usage.setGiftCardUsed(giftCardUsed);
        usage.setGiftCardTransactionId(giftCardTransactionId);
        usage.setQuoteReference(quoteReference);
        usage.setIncentiveStatus("SUCCESS");
        usage.setWalletUsed(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        usage.setWalletTransactionId(null);
        usage.setCashbackExpected(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        usage.setReferralBonusExpected(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        orderIncentiveUsageRepository.save(usage);
    }

    private Map<Long, BigDecimal> allocateAcrossOrders(List<SalesOrder> orders, BigDecimal totalToAllocate) {
        Map<Long, BigDecimal> allocations = new HashMap<>();
        if (orders == null || orders.isEmpty()) {
            return allocations;
        }

        BigDecimal normalizedTotalToAllocate = safeMoney(totalToAllocate);
        BigDecimal totalOrderAmount = orders.stream()
                .map(order -> safeMoney(order.getGrandTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (normalizedTotalToAllocate.compareTo(BigDecimal.ZERO) <= 0 || totalOrderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            orders.forEach(order -> allocations.put(order.getId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));
            return allocations;
        }

        if (normalizedTotalToAllocate.compareTo(totalOrderAmount) >= 0) {
            BigDecimal allocatedSoFar = BigDecimal.ZERO;
            for (int index = 0; index < orders.size(); index++) {
                SalesOrder order = orders.get(index);
                BigDecimal orderTotal = safeMoney(order.getGrandTotal());
                BigDecimal amount = index == orders.size() - 1
                        ? normalizedTotalToAllocate.subtract(allocatedSoFar).min(orderTotal)
                        : orderTotal;
                amount = safeMoney(amount);
                allocations.put(order.getId(), amount);
                allocatedSoFar = allocatedSoFar.add(amount);
            }
            return allocations;
        }

        BigDecimal allocatedSoFar = BigDecimal.ZERO;
        for (int index = 0; index < orders.size(); index++) {
            SalesOrder order = orders.get(index);
            BigDecimal orderTotal = safeMoney(order.getGrandTotal());
            BigDecimal amount;
            if (index == orders.size() - 1) {
                amount = normalizedTotalToAllocate.subtract(allocatedSoFar);
            } else {
                amount = orderTotal.multiply(normalizedTotalToAllocate)
                        .divide(totalOrderAmount, 2, RoundingMode.HALF_UP);
            }

            if (amount.compareTo(orderTotal) > 0) {
                amount = orderTotal;
            }
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                amount = BigDecimal.ZERO;
            }

            amount = safeMoney(amount);
            allocations.put(order.getId(), amount);
            allocatedSoFar = allocatedSoFar.add(amount);
        }

        return allocations;
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildOrderReference(List<SalesOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return "UNKNOWN";
        }
        return orders.stream()
                .map(order -> String.valueOf(order.getId()))
                .sorted()
                .reduce((left, right) -> left + "-" + right)
                .orElse("UNKNOWN");
    }
}
