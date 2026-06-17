package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.ReferralRewards.model.RewardTransaction;
import com.ecommerce.app.module.ReferralRewards.model.RewardTransactionStatus;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardAccountService {

    @Autowired
    private RewardAccountRepository rewardAccountRepository;

    @Autowired
    private RewardTransactionRepository rewardTransactionRepository;

    @Autowired
    private PromotionNotificationService promotionNotificationService;

    @Transactional
    public RewardAccount ensureRewardAccount(Users user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        return rewardAccountRepository.findByUsers(user).orElseGet(() -> {
            RewardAccount newAccount = new RewardAccount();
            newAccount.setUsers(user);
            newAccount.setBalance(BigDecimal.ZERO);
            return rewardAccountRepository.save(newAccount);
        });
    }

    @Transactional
    public void creditBalance(Users user, BigDecimal amount, String description) {
        creditBalance(user, amount, description, TransactionType.CREDIT, null, "MANUAL_CREDIT", null, null);
    }

    @Transactional
    public void creditBalance(Users user, BigDecimal amount, String description, TransactionType type,
            LocalDateTime expiryDate, String sourceType, String sourceReference, Integer levelNumber) {
        creditBalance(user, amount, description, type, expiryDate, sourceType, sourceReference, levelNumber, null, null);
    }

    @Transactional
    public RewardTransaction creditBalance(Users user, BigDecimal amount, String description, TransactionType type,
            LocalDateTime expiryDate, String sourceType, String sourceReference, Integer levelNumber,
            String orderId, String idempotencyKey) {
        if (user == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid credit amount or user.");
        }

        String normalizedIdempotencyKey = trimToNull(idempotencyKey);
        if (normalizedIdempotencyKey != null) {
            Optional<RewardTransaction> existing = rewardTransactionRepository.findByIdempotencyKey(normalizedIdempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        RewardAccount rewardAccount = ensureRewardAccount(user);
        BigDecimal balanceBefore = safeMoney(rewardAccount.getBalance());
        BigDecimal balanceAfter = balanceBefore.add(amount);

        rewardAccount.setBalance(balanceAfter);
        rewardAccountRepository.save(rewardAccount);

        RewardTransaction txn = new RewardTransaction();
        txn.setRewardAccount(rewardAccount);
        txn.setUsers(user);
        txn.setAmount(amount);
        txn.setBalanceBefore(balanceBefore);
        txn.setBalanceAfter(balanceAfter);
        txn.setDescription(description);
        txn.setType(type);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setExpiryDate(expiryDate);
        txn.setSourceType(sourceType);
        txn.setSourceReference(sourceReference);
        txn.setOrderId(orderId);
        txn.setIdempotencyKey(normalizedIdempotencyKey);
        txn.setLevelNumber(levelNumber);
        txn.setStatus(RewardTransactionStatus.AVAILABLE);
        RewardTransaction saved = rewardTransactionRepository.save(txn);
        promotionNotificationService.recordInApp(
                user,
                resolveCreditEvent(sourceType),
                "Reward points credited: " + amount,
                "transactionId=" + saved.getId() + ", sourceType=" + sourceType + ", sourceReference=" + sourceReference
        );
        return saved;
    }

    @Transactional
    public boolean debitBalance(Users user, BigDecimal amount, String description, TransactionType type,
            String sourceType, String sourceReference) {
        return debitBalanceTransaction(user, amount, description, type, sourceType, sourceReference, null, null) != null;
    }

    @Transactional
    public RewardTransaction debitBalanceTransaction(Users user, BigDecimal amount, String description, TransactionType type,
            String sourceType, String sourceReference, String orderId, String idempotencyKey) {
        if (user == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String normalizedIdempotencyKey = trimToNull(idempotencyKey);
        if (normalizedIdempotencyKey != null) {
            Optional<RewardTransaction> existing = rewardTransactionRepository.findByIdempotencyKey(normalizedIdempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        RewardAccount rewardAccount = ensureRewardAccount(user);
        BigDecimal balanceBefore = safeMoney(rewardAccount.getBalance());
        if (balanceBefore.compareTo(amount) < 0) {
            return null;
        }
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        rewardAccount.setBalance(balanceAfter);
        rewardAccountRepository.save(rewardAccount);

        RewardTransaction txn = new RewardTransaction();
        txn.setRewardAccount(rewardAccount);
        txn.setUsers(user);
        txn.setAmount(amount.negate());
        txn.setBalanceBefore(balanceBefore);
        txn.setBalanceAfter(balanceAfter);
        txn.setType(type);
        txn.setDescription(description);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRedeemed(true);
        txn.setSourceType(sourceType);
        txn.setSourceReference(sourceReference);
        txn.setOrderId(orderId);
        txn.setIdempotencyKey(normalizedIdempotencyKey);
        txn.setStatus(RewardTransactionStatus.REDEEMED);
        RewardTransaction saved = rewardTransactionRepository.save(txn);
        promotionNotificationService.recordInApp(
                user,
                "PROMOTION_REWARD_REDEEMED",
                "Reward points redeemed: " + amount,
                "transactionId=" + saved.getId() + ", sourceType=" + sourceType + ", sourceReference=" + sourceReference
        );
        return saved;
    }

    @Transactional
    public void expireRewardTransaction(RewardTransaction transaction) {
        if (transaction == null || transaction.isExpired()) {
            return;
        }

        RewardAccount rewardAccount = transaction.getRewardAccount();
        if (rewardAccount == null) {
            return;
        }

        BigDecimal rewardAmount = transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount();
        if (rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balanceBefore = safeMoney(rewardAccount.getBalance());
            BigDecimal deduction = balanceBefore.min(rewardAmount);
            rewardAccount.setBalance(balanceBefore.subtract(deduction));
            rewardAccountRepository.save(rewardAccount);
        }

        transaction.setExpired(true);
        transaction.setRedeemed(true);
        transaction.setStatus(RewardTransactionStatus.EXPIRED);
        rewardTransactionRepository.save(transaction);
        promotionNotificationService.recordInApp(
                rewardAccount.getUsers(),
                "PROMOTION_REWARD_EXPIRED",
                "Reward points expired: " + rewardAmount,
                "transactionId=" + transaction.getId()
        );
    }

    private String resolveCreditEvent(String sourceType) {
        if (sourceType == null) {
            return "PROMOTION_REWARD_EARNED";
        }
        String normalized = sourceType.trim().toUpperCase();
        return switch (normalized) {
            case "CASHOUT_REJECTED" -> "PROMOTION_CASHOUT_REJECTED";
            case "CASHBACK_RELEASED", "CASHBACK" -> "PROMOTION_CASHBACK_RELEASED";
            case "REFERRAL", "REFERRAL_BONUS" -> "PROMOTION_REFERRAL_QUALIFIED";
            default -> "PROMOTION_REWARD_EARNED";
        };
    }

    private BigDecimal safeMoney(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
