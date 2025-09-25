/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.services.RedemptionService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customerredeem")
public class RedemptionCustomerController {

    @Autowired
    private RedemptionService redemptionService;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/redeem-points")
    public String showRedeemPage() {
        return "customer/referral_rewards/redeem-points";
    }

    @PostMapping("/save")
    public String redeemPoints(@RequestParam BigDecimal points,
            @RequestParam String type,
            @RequestParam String details,
            Principal principal,
            Model model) {
        // Fetch the user by email from principal, throw exception if not found
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Redeem points via service method
        boolean success = redemptionService.redeemPoints(user, points, type, details);

        if (success) {
            model.addAttribute("message", "Successfully redeemed points!");
        } else {
            model.addAttribute("error", "Insufficient reward points.");
        }

        return "customer/referral_rewards/redeem-points";
    }

}
