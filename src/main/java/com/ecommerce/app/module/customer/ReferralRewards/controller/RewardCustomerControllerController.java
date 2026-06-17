/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.RewardRedemptionRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customerreward")
public class RewardCustomerControllerController {

    @Autowired
    private RewardTransactionRepository rewardTransactionRepository;
    @Autowired
    private RewardRedemptionRepository rewardRedemptionRepository;
    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/rewards")
    public String rewardDashboard(Principal principal, Model model) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Calculate wallet balance excluding expired/redeemed points
        BigDecimal walletBalance = rewardTransactionRepository
                .sumAmountByUserAndExpiryDateAfterAndRedeemedFalse(user, LocalDateTime.now())
                .orElse(BigDecimal.ZERO);

        BigDecimal totalEarned = rewardTransactionRepository
                .sumAmountByUserAndAmountGreaterThanEqual(user, BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalRedeemed = rewardTransactionRepository
                .sumAmountByUserAndAmountLessThan(user, BigDecimal.ZERO)
                .map(BigDecimal::abs)
                .orElse(BigDecimal.ZERO);

//        List<Redemptions> redemptions = rewardRedemptionRepository.findAllByUsersOrderByRedeemedAtDesc(user);
        model.addAttribute("walletBalance", walletBalance);
        model.addAttribute("totalEarned", totalEarned);
        model.addAttribute("totalRedeemed", totalRedeemed);
//        model.addAttribute("redemptions", redemptions);

        return "customer/referral_rewards/reward-dashboard";
    }

}
