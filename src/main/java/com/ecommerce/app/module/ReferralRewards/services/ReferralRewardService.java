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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ReferralRewardService {

    private static final BigDecimal SIGNUP_REWARD = new BigDecimal("50.00");
    private static final BigDecimal FIRST_ORDER_REWARD = new BigDecimal("100.00");
    private static final BigDecimal MONTHLY_REWARD_LIMIT = new BigDecimal("500.00");
    private static final int REWARD_EXPIRY_MONTHS = 6;

    @Autowired
    private ReferralRepository referralRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    WalletService walletService;

    @Transactional
    public void processSignupReferral(Users newUser) {
        referralRepository.findByReferredUser(newUser).ifPresent(referral -> {
            if (!referral.isRewardGranted()) {
                Users referrer = referral.getUsers();
                Wallet wallet = referrer.getWallet();

                if (creditReward(wallet, SIGNUP_REWARD, "Referral signup reward for " + newUser.getEmail())) {
                    referral.setRewardGranted(true);
                    referralRepository.save(referral);
                }
            }
        });
    }

    @Transactional
//    public void processFirstOrderReward(Orders order) {
//        Users user = order.getUsers();
//        List<Orders> orders = orderRepository.findByUsers_Id(user.getId());
//
//        if (orders.size() == 1 && !order.isRewardProcessed()) {
//            referralRepository.findByReferredUser(user).ifPresent(referral -> {
//                Users referrer = referral.getUsers();
//                Wallet wallet = referrer.getWallet();
//
//                if (creditReward(wallet, FIRST_ORDER_REWARD, "Referral first order reward for " + user.getEmail())) {
//                    order.setRewardProcessed(true);
//                    orderRepository.save(order);
//                }
//            });
//        }
//    }

    public boolean isUnderMonthlyRewardLimit(Wallet wallet, BigDecimal rewardAmount) {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay();

        BigDecimal totalThisMonth = Optional.ofNullable(
                walletTransactionRepository.sumAmountByWalletAndTypeAndCreatedAtAfter(
                        wallet, TransactionType.REWARD, startOfMonth)
        ).orElse(BigDecimal.ZERO);

        return totalThisMonth.add(rewardAmount).compareTo(MONTHLY_REWARD_LIMIT) <= 0;
    }

    @Transactional
    public boolean creditReward(Wallet wallet, BigDecimal amount, String description) {
        if (!isUnderMonthlyRewardLimit(wallet, amount)) {
            return false;
        }

        WalletTransaction txn = new WalletTransaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setDescription(description);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setExpiryDate(LocalDateTime.now().plusMonths(REWARD_EXPIRY_MONTHS));
        txn.setType(TransactionType.REWARD);
        txn.setRedeemed(false);

        walletTransactionRepository.save(txn);
        updateWalletBalance(wallet);
        return true;
    }

    public void updateWalletBalance(Wallet wallet) {
        BigDecimal balance = walletTransactionRepository
                .sumAmountByWalletAndExpiryDateAfterAndRedeemedFalse(wallet, LocalDateTime.now())
                .orElse(BigDecimal.ZERO);

        wallet.setBalance(balance); // Only if Wallet.balance is still Double
        walletRepository.save(wallet);
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    @Transactional
    public void expireRewards() {
        LocalDateTime now = LocalDateTime.now();
        List<WalletTransaction> expiredRewards = walletTransactionRepository
                .findByExpiryDateBeforeAndRedeemedFalseAndType(now, TransactionType.REWARD);

        for (WalletTransaction txn : expiredRewards) {
            txn.setRedeemed(true);
            walletTransactionRepository.save(txn);
            updateWalletBalance(txn.getWallet());
        }
    }

    public void grantReferralReward(Long userId) {
        Optional<Referral> referralOpt = referralRepository.findByReferredUser_Id(userId);
        if (referralOpt.isPresent()) {
            Referral referral = referralOpt.get();
            if (!referral.isRewardGranted()) {
                // Example: credit 100 points to the referrer’s wallet
                Users referrer = referral.getUsers();

                BigDecimal rewardPoints = BigDecimal.valueOf(100);
                walletService.creditWallet(referrer, rewardPoints, "Referral reward for user ID " + userId);

                referral.setRewardGranted(true);
                referralRepository.save(referral);
            }
        }
    }
}
