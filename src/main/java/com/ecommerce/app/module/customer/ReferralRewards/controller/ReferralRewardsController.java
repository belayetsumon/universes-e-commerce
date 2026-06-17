/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.repository.CashOutRequestRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
import com.ecommerce.app.module.ReferralRewards.services.RedemptionService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/referralrewards")
public class ReferralRewardsController {

    @Autowired
    RedemptionService redemptionService;

    @Autowired
    GiftCardRepository giftCardRepository;

    @Autowired
    CashOutRequestRepository cashOutRequestRepository;
    @Autowired
    RewardAccountRepository rewardAccountRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    RewardTransactionRepository rewardTransactionRepository;

    @Autowired
    ReferralRepository referralRepository;

    @RequestMapping("/dashbords")
    public String deshbords(Model model, Principal principal) {

        Optional<Users> users = usersRepository.findByEmail(principal.getName());
        if (users.isEmpty()) {
            return "redirect:/login";
        }
        Users currentUser = users.get();

        BigDecimal walletBalance = users
                .flatMap(user -> rewardAccountRepository.findByUsers(user)
                .map(com.ecommerce.app.module.ReferralRewards.model.RewardAccount::getBalance)
                .map(balance -> balance != null ? balance : BigDecimal.ZERO)
                )
                .orElse(BigDecimal.ZERO);

        model.addAttribute("walletBalance", walletBalance);

        Optional<BigDecimal> sumCreditsByUser = rewardTransactionRepository.sumCreditsByUser(currentUser);

        model.addAttribute("totalCredits", sumCreditsByUser.orElse(BigDecimal.ZERO));

        Long totalref = referralRepository.countByReferredUser(currentUser);

        model.addAttribute("totalref", totalref);
        Optional<Referral> referral = referralRepository.findByUsers(currentUser);

        model.addAttribute("referral_code", referral.map(Referral::getReferralCode).orElse(""));

        return "customer/referral_rewards/customer_referrallist_dashbords";
    }

}
