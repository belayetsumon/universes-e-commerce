/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.RewardAccount;
import com.ecommerce.app.module.ReferralRewards.model.RewardTransaction;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
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
    private RewardTransactionRepository rewardTransactionRepository;
    @Autowired
    private RewardAccountRepository rewardAccountRepository;

    @Autowired
    private RewardAccountService rewardAccountService;

    @Autowired
    private RewardTransactionService rewardTransactionService;

    @Transactional
    public void processSignupReferral(Users newUser) {
//        referralRepository.findByReferredUser(newUser).ifPresent(referral -> {
//            if (!referral.isRewardGranted()) {
//                Users referrer = referral.getUsers();
//                Wallet wallet = referrer.getWallet();
//
//                if (creditReward(wallet, SIGNUP_REWARD, "Referral signup reward for " + newUser.getEmail())) {
//                    referral.setRewardGranted(true);
//                    referralRepository.save(referral);
//                }
//            }
//        });
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
    public boolean isUnderMonthlyRewardLimit(RewardAccount rewardAccount, BigDecimal rewardAmount) {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay();

        BigDecimal totalThisMonth = Optional.ofNullable(
                rewardTransactionRepository.sumAmountByRewardAccountAndTypeAndCreatedAtAfter(
                        rewardAccount, TransactionType.REWARD, startOfMonth)
        ).orElse(BigDecimal.ZERO);

        return totalThisMonth.add(rewardAmount).compareTo(MONTHLY_REWARD_LIMIT) <= 0;
    }

    @Transactional
    public boolean creditReward(RewardAccount rewardAccount, BigDecimal amount, String description) {
        if (!isUnderMonthlyRewardLimit(rewardAccount, amount)) {
            return false;
        }

        rewardTransactionService.creditRewardAccount(
                rewardAccount,
                amount,
                description,
                TransactionType.REWARD,
                LocalDateTime.now().plusMonths(REWARD_EXPIRY_MONTHS),
                "REFERRAL_REWARD",
                null,
                null
        );
        return true;
    }

    public void updateRewardBalance(RewardAccount rewardAccount) {
        if (rewardAccount != null) {
            rewardAccountRepository.save(rewardAccount);
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    @Transactional
    public void expireRewards() {
        LocalDateTime now = LocalDateTime.now();
        List<RewardTransaction> expiredRewards = rewardTransactionRepository
                .findByExpiryDateBeforeAndRedeemedFalseAndType(now, TransactionType.REWARD);

        for (RewardTransaction txn : expiredRewards) {
            rewardTransactionService.expireRewardTransaction(txn);
        }
    }

    public void grantReferralReward(Long userId) {
        Optional<Referral> referralOpt = referralRepository.findByUsers_Id(userId);
        if (referralOpt.isPresent()) {
            Referral referral = referralOpt.get();
            Users referrer = referral.getReferredUser();

            if (referrer != null && !referral.isRewardGranted()) {
                BigDecimal rewardPoints = BigDecimal.valueOf(100);
                rewardAccountService.creditBalance(
                        referrer,
                        rewardPoints,
                        "Referral reward for user ID " + userId,
                        TransactionType.REWARD,
                        LocalDateTime.now().plusMonths(REWARD_EXPIRY_MONTHS),
                        "SIGNUP_REFERRAL",
                        "USER:" + userId,
                        null
                );

                referral.setRewardGranted(true);
                referralRepository.save(referral);
            }
        }
    }
}
