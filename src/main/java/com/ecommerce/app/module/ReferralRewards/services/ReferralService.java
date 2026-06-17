/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.services.UsersService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ReferralService {

    private static final int MAX_COMMISSION_LEVEL = 10;

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private UsersService usersService;

    @Autowired
    private RewardAccountService rewardAccountService;

    @Autowired
    private LavelRateSettingsService lavelRateSettingsService;

    @Autowired
    private ReferralRewardService referralRewardService;

    @Autowired
    private PromotionFraudService promotionFraudService;

    @Transactional
    public Referral createReferralProfile(Users user, Users referrer) {
        return createReferralProfile(user, referrer, false);
    }

    @Transactional
    public Referral createReferralProfile(Users user, Users referrer, boolean rewardGranted) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }

        Referral referral = referralRepository.findByUsers(user).orElseGet(Referral::new);
        if (referral.getId() == null) {
            referral.setUsers(user);
            referral.setReferralCode(generateUniqueReferralCode());
        } else if (referral.getReferralCode() == null || referral.getReferralCode().isBlank()) {
            referral.setReferralCode(generateUniqueReferralCode());
        }

        if (promotionFraudService.flagSelfReferralIfMatched(user, referrer)) {
            referrer = null;
            rewardGranted = false;
        }

        referral.setReferredUser(referrer);
        referral.setRewardGranted(referral.isRewardGranted() || rewardGranted);

        rewardAccountService.ensureRewardAccount(user);
        return referralRepository.save(referral);
    }

    @Transactional
    public Referral createReferralProfileAndGrantSignupReward(Users user, Users referrer) {
        Referral referral = createReferralProfile(user, referrer, false);

        if (user != null && user.getId() != null && referrer != null && !referral.isRewardGranted()) {
            referralRewardService.grantReferralReward(user.getId());
            return referralRepository.findByUsers(user).orElse(referral);
        }

        return referral;
    }

    public Users resolveReferrerByCode(String referralCode) {
        if (referralCode == null || referralCode.isBlank()) {
            return null;
        }

        return referralRepository.findByReferralCode(referralCode.trim())
                .map(Referral::getUsers)
                .orElse(null);
    }

    public void handleReferralReward(Users customer, BigDecimal amount) {
        if (customer == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        referralRepository.findByUsers(customer).ifPresent(referral -> {
            Users referrer = referral.getReferredUser();
            if (referrer == null) {
                return;
            }

            rewardAccountService.creditBalance(
                    referrer,
                    amount,
                    "Referral reward from user #" + customer.getId(),
                    TransactionType.REWARD,
                    null,
                    "REFERRAL_REWARD",
                    "USER:" + customer.getId(),
                    null
            );
        });
    }

    public void distributeCommission(Users buyer, BigDecimal commissionPool) {
        distributeCommission(buyer, commissionPool, null);
    }

    public void distributeCommission(Users buyer, BigDecimal commissionPool, Long orderId) {
        if (buyer == null || commissionPool == null || commissionPool.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        int level = 1;
        Users currentChild = buyer;
        Set<Long> visitedUserIds = new HashSet<>();

        while (level <= MAX_COMMISSION_LEVEL) {
            if (currentChild.getId() != null && !visitedUserIds.add(currentChild.getId())) {
                break;
            }

            Optional<Referral> currentReferralOpt = referralRepository.findByUsers(currentChild);
            if (currentReferralOpt.isEmpty()) {
                break;
            }

            Users parent = currentReferralOpt.get().getReferredUser();
            if (parent == null) {
                break;
            }

            BigDecimal rate = getRate(level);
            BigDecimal commission = commissionPool.multiply(rate).setScale(2, RoundingMode.HALF_UP);

            if (commission.compareTo(BigDecimal.ZERO) > 0) {
                String sourceReference = orderId != null ? "ORDER:" + orderId : "BUYER:" + buyer.getId();
                String orderReference = orderId == null ? null : String.valueOf(orderId);
                String idempotencyKey = orderId == null || parent.getId() == null
                        ? null
                        : "LEVEL_COMMISSION:ORDER:" + orderId + ":LEVEL:" + level + ":USER:" + parent.getId();
                rewardAccountService.creditBalance(
                        parent,
                        commission,
                        "Level " + level + " commission from buyer #" + buyer.getId(),
                        TransactionType.CREDIT,
                        null,
                        "LEVEL_COMMISSION",
                        sourceReference,
                        level,
                        orderReference,
                        idempotencyKey
                );
            }

            currentChild = parent;
            level++;
        }
    }

    private BigDecimal getRate(int level) {
        return lavelRateSettingsService.getCommissionRateForLevel(level);
    }

    private String generateUniqueReferralCode() {
        String referralCode;
        do {
            referralCode = usersService.generateReferralCode();
        } while (referralRepository.findByReferralCode(referralCode).isPresent());
        return referralCode;
    }
}
