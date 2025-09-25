/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ReferralService {

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private UsersRepository usersRepository;

    public void handleReferralReward(Users customer, BigDecimal amount) {

        Optional<Referral> referralOpt = referralRepository.findByUsers(customer);

        if (referralOpt.isPresent()) {
            Referral referral = referralOpt.get();
            Users referrer = referral.getReferredUser();
            System.out.println("gdfgdfgdfgfd" + referrer.getId());
            // Reward 5% of order total
            //  double rewardAmount = order.getGrandTotal() * 0.05;
            BigDecimal rewardAmount = amount;

            // Load or create wallet
            Wallet wallet = walletRepository.findByUsers(referrer)
                    .orElseGet(() -> {
                        Wallet w = new Wallet();
                        w.setUsers(referrer);
                        w.setBalance(BigDecimal.ZERO);
                        return walletRepository.save(w);
                    });

            // Credit wallet
            wallet.setBalance(wallet.getBalance().add(rewardAmount));
            walletRepository.save(wallet);

            // Save transaction
            WalletTransaction tx = new WalletTransaction();
            tx.setWallet(wallet);
            tx.setAmount(rewardAmount);
            tx.setDescription("Referral reward from user: " + ", Order ID: ");
            tx.setType(TransactionType.CREDIT);
            tx.setUsers(referrer);
            tx.setCreatedAt(LocalDateTime.now());
            walletTransactionRepository.save(tx);
        }
    }

    public void distributeCommission(Users buyer, BigDecimal orderAmount) {
        int level = 1;
        Users currentChild = buyer;

        while (level <= 8) {
            Optional<Referral> parentReferralOpt = referralRepository.findByUsers(currentChild);

            if (parentReferralOpt.isEmpty()) {
                break; // no parent, stop
            }

            Referral parentReferral = parentReferralOpt.get();
            Users parent = parentReferral.getReferredUser(); // get parent user

            if (parent == null) {
                break;
            }

            BigDecimal rate = getRate(level);
            BigDecimal commission = orderAmount.multiply(rate);

            Wallet wallet = walletRepository.findByUsers(parent)
                    .orElseGet(() -> {
                        Wallet w = new Wallet();
                        w.setUsers(parent);
                        w.setBalance(BigDecimal.ZERO);
                        return walletRepository.save(w);
                    });

            wallet.setBalance(wallet.getBalance().add(commission));
            walletRepository.save(wallet);

            WalletTransaction tx = new WalletTransaction();
            tx.setWallet(wallet);
            tx.setUsers(parent);
            tx.setAmount(commission);
            tx.setDescription("Level " + level + " commission from purchase by ");
            tx.setCreatedAt(LocalDateTime.now());
            tx.setType(TransactionType.CREDIT);
            walletTransactionRepository.save(tx);

            System.out.println("Level " + level + ": " + " got " + commission);

            // move up to next parent
            currentChild = parent;
            level++;
        }
    }

    private BigDecimal getRate(int level) {
        return switch (level) {
            case 1 ->
                new BigDecimal("0.10");
            case 2 ->
                new BigDecimal("0.05");
            case 3 ->
                new BigDecimal("0.04");
            case 4 ->
                new BigDecimal("0.03");
            case 5 ->
                new BigDecimal("0.02");
            case 6 ->
                new BigDecimal("0.01");
            case 7 ->
                new BigDecimal("0.01");
            case 8 ->
                new BigDecimal("0.005");
            default ->
                BigDecimal.ZERO;
        };
    }
}
