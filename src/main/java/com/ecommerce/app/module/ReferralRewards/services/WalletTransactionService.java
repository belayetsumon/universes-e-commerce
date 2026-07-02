package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletTransactionService {

    private final WalletService walletService;

    public WalletTransactionService(WalletService walletService) {
        this.walletService = walletService;
    }

    public boolean hasSufficientBalance(Wallet wallet, BigDecimal amount) {
        if (wallet == null || wallet.getBalance() == null || amount == null) {
            return false;
        }
        return wallet.getBalance().compareTo(amount) >= 0;
    }

    @Transactional
    public boolean deductFromWallet(Wallet wallet, BigDecimal amount, String description) {
        return deductFromWallet(wallet, amount, description, TransactionType.PURCHASE, "PURCHASE", null);
    }

    @Transactional
    public boolean deductFromWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type,
            String sourceType, String sourceReference) {
        if (wallet == null || wallet.getUser() == null) {
            return false;
        }

        return walletService.debitWallet(wallet.getUser(), amount, description, type, sourceType, sourceReference);
    }

    @Transactional
    public void creditWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type) {
        creditWallet(wallet, amount, description, type, null);
    }

    @Transactional
    public void creditWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type, LocalDateTime expiryDate) {
        creditWallet(wallet, amount, description, type, expiryDate, type == null ? null : type.name(), null, null);
    }

    @Transactional
    public void creditWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type,
            LocalDateTime expiryDate, String sourceType, String sourceReference, Integer levelNumber) {
        if (wallet == null || wallet.getUser() == null) {
            throw new IllegalArgumentException("Wallet user is required.");
        }

        walletService.creditWallet(wallet.getUser(), amount, description, type, expiryDate, sourceType, sourceReference, levelNumber);
    }

    @Transactional
    public void expireRewardTransaction(WalletTransaction transaction) {
        walletService.expireRewardTransaction(transaction);
    }

    @Transactional
    public void expireOldRewards() {
        // WalletTransaction no longer stores expiry/redeemed flags. Kept for scheduler compatibility.
    }
}
