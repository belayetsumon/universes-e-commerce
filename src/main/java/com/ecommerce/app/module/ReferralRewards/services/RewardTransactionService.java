package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.ReferralRewards.model.RewardTransaction;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardTransactionService {

    @Autowired
    private RewardTransactionRepository rewardTransactionRepository;

    @Autowired
    private RewardAccountService rewardAccountService;

    public boolean hasSufficientBalance(RewardAccount rewardAccount, BigDecimal amount) {
        return rewardAccount != null
                && rewardAccount.getBalance() != null
                && rewardAccount.getBalance().compareTo(amount) >= 0;
    }

    @Transactional
    public boolean deductFromRewardAccount(RewardAccount rewardAccount, BigDecimal amount, String description) {
        return deductFromRewardAccount(rewardAccount, amount, description, TransactionType.REDEMPTION, "REWARD_REDEMPTION", null);
    }

    @Transactional
    public boolean deductFromRewardAccount(RewardAccount rewardAccount, BigDecimal amount, String description, TransactionType type,
            String sourceType, String sourceReference) {
        if (rewardAccount == null || rewardAccount.getUsers() == null) {
            return false;
        }

        return rewardAccountService.debitBalance(rewardAccount.getUsers(), amount, description, type, sourceType, sourceReference);
    }

    @Transactional
    public RewardTransaction deductFromRewardAccountWithLedger(RewardAccount rewardAccount, BigDecimal amount, String description, TransactionType type,
            String sourceType, String sourceReference, String orderId, String idempotencyKey) {
        if (rewardAccount == null || rewardAccount.getUsers() == null) {
            return null;
        }

        return rewardAccountService.debitBalanceTransaction(
                rewardAccount.getUsers(),
                amount,
                description,
                type,
                sourceType,
                sourceReference,
                orderId,
                idempotencyKey
        );
    }

    @Transactional
    public void creditRewardAccount(RewardAccount rewardAccount, BigDecimal amount, String description, TransactionType type) {
        creditRewardAccount(rewardAccount, amount, description, type, null);
    }

    @Transactional
    public void creditRewardAccount(RewardAccount rewardAccount, BigDecimal amount, String description, TransactionType type, LocalDateTime expiryDate) {
        creditRewardAccount(rewardAccount, amount, description, type, expiryDate, type.name(), null, null);
    }

    @Transactional
    public void creditRewardAccount(RewardAccount rewardAccount, BigDecimal amount, String description, TransactionType type,
            LocalDateTime expiryDate, String sourceType, String sourceReference, Integer levelNumber) {
        if (rewardAccount == null || rewardAccount.getUsers() == null) {
            throw new IllegalArgumentException("Reward account user is required.");
        }

        rewardAccountService.creditBalance(rewardAccount.getUsers(), amount, description, type, expiryDate, sourceType, sourceReference, levelNumber);
    }

    @Transactional
    public RewardTransaction creditRewardAccountWithLedger(RewardAccount rewardAccount, BigDecimal amount, String description, TransactionType type,
            LocalDateTime expiryDate, String sourceType, String sourceReference, Integer levelNumber,
            String orderId, String idempotencyKey) {
        if (rewardAccount == null || rewardAccount.getUsers() == null) {
            throw new IllegalArgumentException("Reward account user is required.");
        }

        return rewardAccountService.creditBalance(
                rewardAccount.getUsers(),
                amount,
                description,
                type,
                expiryDate,
                sourceType,
                sourceReference,
                levelNumber,
                orderId,
                idempotencyKey
        );
    }

    @Transactional
    public void expireRewardTransaction(RewardTransaction transaction) {
        rewardAccountService.expireRewardTransaction(transaction);
    }

    @Transactional
    public void expireOldRewards() {
        List<RewardTransaction> rewards = rewardTransactionRepository.findExpiredUnredeemed(LocalDateTime.now());

        for (RewardTransaction txn : rewards) {
            rewardAccountService.expireRewardTransaction(txn);
        }
    }
}
