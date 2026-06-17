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
import com.ecommerce.app.module.user.model.Users;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Transactional
    public Wallet ensureWallet(Users user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        return walletRepository.findByUsers(user).orElseGet(() -> {
            Wallet newWallet = new Wallet();
            newWallet.setUsers(user);
            newWallet.setBalance(BigDecimal.ZERO);
            return walletRepository.save(newWallet);
        });
    }

    @Transactional
    public void creditWallet(Users user, BigDecimal amount, String description) {
        creditWallet(user, amount, description, TransactionType.CREDIT, null, "MANUAL_CREDIT", null, null);
    }

    @Transactional
    public void creditWallet(Users user, BigDecimal amount, String description, TransactionType type,
            LocalDateTime expiryDate, String sourceType, String sourceReference, Integer levelNumber) {
        if (user == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid credit amount or user.");
        }

        Wallet wallet = ensureWallet(user);

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setUsers(user);
        txn.setAmount(amount);
        txn.setDescription(description);
        txn.setType(type);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setExpiryDate(expiryDate);
        txn.setSourceType(sourceType);
        txn.setSourceReference(sourceReference);
        txn.setLevelNumber(levelNumber);
        walletTransactionRepository.save(txn);
    }

    @Transactional
    public boolean debitWallet(Users user, BigDecimal amount, String description, TransactionType type,
            String sourceType, String sourceReference) {
        if (user == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        Wallet wallet = ensureWallet(user);
        if (wallet.getBalance().compareTo(amount) < 0) {
            return false;
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setUsers(user);
        txn.setAmount(amount.negate());
        txn.setType(type);
        txn.setDescription(description);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRedeemed(true);
        txn.setSourceType(sourceType);
        txn.setSourceReference(sourceReference);
        walletTransactionRepository.save(txn);
        return true;
    }

    @Transactional
    public void expireRewardTransaction(WalletTransaction transaction) {
        if (transaction == null || transaction.isExpired()) {
            return;
        }

        Wallet wallet = transaction.getWallet();
        if (wallet == null) {
            return;
        }

        BigDecimal rewardAmount = transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount();
        if (rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal deduction = wallet.getBalance().min(rewardAmount);
            wallet.setBalance(wallet.getBalance().subtract(deduction));
            walletRepository.save(wallet);
        }

        transaction.setExpired(true);
        transaction.setRedeemed(true);
        walletTransactionRepository.save(transaction);
    }
}
