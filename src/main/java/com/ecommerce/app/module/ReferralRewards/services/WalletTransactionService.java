/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
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
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    public boolean hasSufficientBalance(Wallet wallet, BigDecimal amount) {
        return wallet.getBalance().compareTo(amount) >= 0;
    }

    @Transactional
    public boolean deductFromWallet(Wallet wallet, BigDecimal amount, String description) {
        if (hasSufficientBalance(wallet, amount)) {
            // Update wallet balance
            BigDecimal updatedBalance = wallet.getBalance().subtract(amount);
            wallet.setBalance(updatedBalance);

            walletRepository.save(wallet);

            // Create transaction
            WalletTransaction txn = new WalletTransaction();
            txn.setWallet(wallet);
            txn.setAmount(amount.negate()); // Debit
            txn.setType(TransactionType.PURCHASE);
            txn.setDescription(description);
            txn.setCreatedAt(LocalDateTime.now());
            txn.setRedeemed(true); // Always redeemed for purchase

            walletTransactionRepository.save(txn);
            return true;
        }
        return false;
    }

    @Transactional
    public void creditWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type, LocalDateTime expiryDate) {
        BigDecimal updatedBalance = wallet.getBalance().add(amount);
        wallet.setBalance(updatedBalance);

        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setType(type);
        txn.setDescription(description);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setExpiryDate(expiryDate); // Nullable for non-reward types
        txn.setRedeemed(false);

        walletTransactionRepository.save(txn);
    }

    @Transactional
    public void creditWallet(Wallet wallet, BigDecimal amount, String description, TransactionType type) {
        creditWallet(wallet, amount, description, type, null);
    }

    @Transactional
    public void expireOldRewards() {
        List<WalletTransaction> rewards = walletTransactionRepository.findExpiredUnredeemed(LocalDateTime.now());

        for (WalletTransaction txn : rewards) {
            Wallet wallet = txn.getWallet();
            BigDecimal rewardAmount = txn.getAmount();

            if (wallet != null && rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentBalance = wallet.getBalance();  // already BigDecimal
                if (currentBalance.compareTo(rewardAmount) >= 0) {
                    BigDecimal updatedBalance = currentBalance.subtract(rewardAmount);
                    wallet.setBalance(updatedBalance);
                }
            }

            txn.setRedeemed(true);
            walletRepository.save(wallet);
            walletTransactionRepository.save(txn);
        }
    }
}
