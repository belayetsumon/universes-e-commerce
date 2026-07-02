package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.enumvalue.RedemptionStatus;
import com.ecommerce.app.module.ReferralRewards.enumvalue.RedemptionType;
import com.ecommerce.app.module.ReferralRewards.model.Redemptions;
import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.ReferralRewards.model.RewardTransaction;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.RewardRedemptionRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RedemptionService {

    @Autowired
    private RewardAccountRepository rewardAccountRepository;

    @Autowired
    private RewardRedemptionRepository rewardRedemptionRepository;

    @Autowired
    private RewardTransactionService rewardTransactionService;

    @Transactional
    public void updateRewardBalance(RewardAccount rewardAccount) {
        if (rewardAccount != null) {
            rewardAccountRepository.save(rewardAccount);
        }
    }

    @Transactional
    public boolean redeemPoints(Users user, BigDecimal pointsToRedeem, String type, String details) {
        return redeemPoints(user, pointsToRedeem, type, details, null, null, null, null, null);
    }

    @Transactional
    public boolean redeemPoints(Users user, BigDecimal pointsToRedeem, String type, String details,
            String orderId, String sourceProgram, String sourceId, BigDecimal conversionRate, String idempotencyKey) {
        Optional<RewardAccount> optionalRewardAccount = rewardAccountRepository.findByUsers(user);
        if (optionalRewardAccount.isEmpty() || pointsToRedeem == null || pointsToRedeem.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        RewardAccount rewardAccount = optionalRewardAccount.get();
        RewardTransaction ledgerTransaction = rewardTransactionService.deductFromRewardAccountWithLedger(
                rewardAccount,
                pointsToRedeem,
                "Redeemed for " + type + ": " + details,
                TransactionType.REDEMPTION,
                "REDEMPTION",
                type,
                orderId,
                idempotencyKey
        );

        if (ledgerTransaction == null) {
            return false;
        }

        RedemptionType redemptionType = resolveRedemptionType(type);
        String effectiveSourceProgram = sourceProgram != null ? sourceProgram : "REDEMPTION";
        String effectiveSourceId = sourceId != null ? sourceId : type;
        if (orderId != null && effectiveSourceId != null) {
            Optional<Redemptions> existingRedemption = rewardRedemptionRepository.findFirstByRedemptionTypeAndOrderIdAndSourceProgramAndSourceId(
                    redemptionType,
                    orderId,
                    effectiveSourceProgram,
                    effectiveSourceId
            );
            if (existingRedemption.isPresent()) {
                return true;
            }
        }

        Redemptions redemption = new Redemptions();
        redemption.setUsers(user);
        redemption.setPointsUsed(pointsToRedeem);
        redemption.setRedemptionType(redemptionType);
        redemption.setDetails(details);
        redemption.setOrderId(orderId);
        redemption.setSourceProgram(effectiveSourceProgram);
        redemption.setSourceId(effectiveSourceId);
        redemption.setConversionRate(conversionRate);
        redemption.setAmount(conversionRate != null ? pointsToRedeem.multiply(conversionRate) : null);
        redemption.setCurrency(conversionRate != null ? "BDT" : null);
        redemption.setLedgerTransactionId(ledgerTransaction.getId());
        redemption.setStatus(RedemptionStatus.SUCCESS);
        redemption.setCompletedAt(java.time.LocalDateTime.now());
        rewardRedemptionRepository.save(redemption);

        return true;
    }

    @Transactional
    public Redemptions recordRedemption(Users user, RedemptionType redemptionType, BigDecimal pointsUsed,
            BigDecimal amount, String currency, String orderId, String sourceProgram, String sourceId,
            Long ledgerTransactionId, RedemptionStatus status, String details) {
        if (redemptionType != null && orderId != null && sourceProgram != null && sourceId != null) {
            Optional<Redemptions> existing = rewardRedemptionRepository.findFirstByRedemptionTypeAndOrderIdAndSourceProgramAndSourceId(
                    redemptionType,
                    orderId,
                    sourceProgram,
                    sourceId
            );
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        Redemptions redemption = new Redemptions();
        redemption.setUsers(user);
        redemption.setRedemptionType(redemptionType);
        redemption.setPointsUsed(pointsUsed);
        redemption.setAmount(amount);
        redemption.setCurrency(currency);
        redemption.setOrderId(orderId);
        redemption.setSourceProgram(sourceProgram);
        redemption.setSourceId(sourceId);
        redemption.setLedgerTransactionId(ledgerTransactionId);
        redemption.setStatus(status);
        redemption.setDetails(details);
        if (status == RedemptionStatus.SUCCESS || status == RedemptionStatus.REVERSED || status == RedemptionStatus.FAILED) {
            redemption.setCompletedAt(java.time.LocalDateTime.now());
        }
        return rewardRedemptionRepository.save(redemption);
    }

    private RedemptionType resolveRedemptionType(String type) {
        if (type == null || type.isBlank()) {
            return RedemptionType.REWARD_POINT;
        }

        String normalized = type.trim().toUpperCase();
        return switch (normalized) {
            case "COUPON" -> RedemptionType.COUPON;
            case "GIFTCARD", "GIFT_CARD" -> RedemptionType.GIFT_CARD;
            case "CASHBACK" -> RedemptionType.CASHBACK;
            case "WALLET" -> RedemptionType.WALLET;
            case "REFERRAL_BONUS" -> RedemptionType.REFERRAL_BONUS;
            default -> RedemptionType.REWARD_POINT;
        };
    }
}
