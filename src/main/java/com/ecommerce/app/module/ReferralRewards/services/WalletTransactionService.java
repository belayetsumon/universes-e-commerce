/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class WalletTransactionService {

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private WalletService walletService;

    public boolean hasSufficientBalance(Wallet wallet, BigDecimal amount) {
        return wallet != null && wallet.getBalance() != null && wallet.getBalance().compareTo(amount) >= 0;
    }

    @Transactional
    public boolean deductFromWallet(Wallet wallet, BigDecimal amount, String description) {
        return deductFromWallet(wallet, amount, description, TransactionType.PURCHASE, "PURCHASE", null);
    }

    @Transactional
    public boolean deductFromWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type,
            String sourceType, String sourceReference) {
        if (wallet == null || wallet.getUsers() == null) {
            return false;
        }

        return walletService.debitWallet(wallet.getUsers(), amount, description, type, sourceType, sourceReference);
    }

    @Transactional
    public void creditWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type) {
        creditWallet(wallet, amount, description, type, null);
    }

    @Transactional
    public void creditWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type, LocalDateTime expiryDate) {
        creditWallet(wallet, amount, description, type, expiryDate, type.name(), null, null);
    }

    @Transactional
    public void creditWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type,
            LocalDateTime expiryDate, String sourceType, String sourceReference, Integer levelNumber) {
        if (wallet == null || wallet.getUsers() == null) {
            throw new IllegalArgumentException("Wallet user is required.");
        }

        walletService.creditWallet(wallet.getUsers(), amount, description, type, expiryDate, sourceType, sourceReference, levelNumber);
    }

    @Transactional
    public void expireRewardTransaction(WalletTransaction transaction) {
        walletService.expireRewardTransaction(transaction);
    }

    @Transactional
    public void expireOldRewards() {
        List<WalletTransaction> rewards = walletTransactionRepository.findExpiredUnredeemed(LocalDateTime.now());

        for (WalletTransaction txn : rewards) {
            walletService.expireRewardTransaction(txn);
        }
    }
}
