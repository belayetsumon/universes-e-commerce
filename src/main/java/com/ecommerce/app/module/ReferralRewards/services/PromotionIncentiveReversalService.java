package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardStatus;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardTransaction;
import com.ecommerce.app.module.ReferralRewards.model.OrderIncentiveUsage;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.CouponRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardTransactionRepository;
import com.ecommerce.app.module.ReferralRewards.repository.OrderIncentiveUsageRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionIncentiveReversalService {

    private final OrderIncentiveUsageRepository orderIncentiveUsageRepository;
    private final CouponRepository couponRepository;
    private final GiftCardTransactionRepository giftCardTransactionRepository;
    private final GiftCardRepository giftCardRepository;
    private final RewardTransactionRepository rewardTransactionRepository;
    private final RewardAccountService rewardAccountService;

    public PromotionIncentiveReversalService(
            OrderIncentiveUsageRepository orderIncentiveUsageRepository,
            CouponRepository couponRepository,
            GiftCardTransactionRepository giftCardTransactionRepository,
            GiftCardRepository giftCardRepository,
            RewardTransactionRepository rewardTransactionRepository,
            RewardAccountService rewardAccountService) {
        this.orderIncentiveUsageRepository = orderIncentiveUsageRepository;
        this.couponRepository = couponRepository;
        this.giftCardTransactionRepository = giftCardTransactionRepository;
        this.giftCardRepository = giftCardRepository;
        this.rewardTransactionRepository = rewardTransactionRepository;
        this.rewardAccountService = rewardAccountService;
    }

    @Transactional
    public void reverseOrderIncentives(Long usageId, String reason) {
        OrderIncentiveUsage usage = orderIncentiveUsageRepository.findById(usageId)
                .orElseThrow(() -> new IllegalArgumentException("Order incentive usage not found."));

        if ("REVERSED".equalsIgnoreCase(usage.getIncentiveStatus())) {
            return;
        }

        reverseCouponUsage(usage);
        reverseRewardUsage(usage, reason);
        reverseGiftCardUsage(usage);

        usage.setIncentiveStatus("REVERSED");
        orderIncentiveUsageRepository.save(usage);
    }

    private void reverseCouponUsage(OrderIncentiveUsage usage) {
        if (usage.getCouponId() == null || safeAmount(usage.getCouponDiscount()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        couponRepository.findById(usage.getCouponId()).ifPresent(coupon -> {
            int nextTimesUsed = Math.max(0, coupon.getTimesUsed() - 1);
            coupon.setTimesUsed(nextTimesUsed);
            couponRepository.save(coupon);
        });
    }

    private void reverseRewardUsage(OrderIncentiveUsage usage, String reason) {
        if (usage.getRewardTransactionId() == null || safeAmount(usage.getRewardPointsUsed()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        rewardTransactionRepository.findById(usage.getRewardTransactionId()).ifPresent(transaction -> {
            Users user = transaction.getUsers();
            if (user == null) {
                return;
            }
            rewardAccountService.creditBalance(
                    user,
                    usage.getRewardPointsUsed(),
                    "Order incentive reversal for order #" + usage.getOrderId() + cleanReason(reason),
                    TransactionType.CREDIT,
                    null,
                    "ORDER_INCENTIVE_REVERSAL",
                    "ORDER:" + usage.getOrderId(),
                    null,
                    usage.getOrderId(),
                    "ORDER:" + usage.getOrderId() + ":REWARD_REVERSAL"
            );
        });
    }

    private void reverseGiftCardUsage(OrderIncentiveUsage usage) {
        if (usage.getGiftCardTransactionId() == null || safeAmount(usage.getGiftCardUsed()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        giftCardTransactionRepository.findById(usage.getGiftCardTransactionId()).ifPresent(transaction -> {
            GiftCard giftCard = transaction.getGiftCard();
            if (giftCard == null || giftCard.getId() == null) {
                return;
            }
            GiftCard locked = giftCardRepository.findById(giftCard.getId()).orElse(giftCard);
            BigDecimal currentBalance = safeAmount(locked.getBalance());
            locked.setBalance(currentBalance.add(safeAmount(transaction.getAmountUsed())));
            locked.setRedeemed(false);
            locked.setStatus(GiftCardStatus.ACTIVE);
            giftCardRepository.save(locked);
        });
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String cleanReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return "";
        }
        return ". Reason: " + reason.trim();
    }
}
