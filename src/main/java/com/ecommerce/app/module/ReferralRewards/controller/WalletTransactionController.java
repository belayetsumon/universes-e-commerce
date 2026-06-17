/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.RewardTransactionRepository;
import java.util.Collections;
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
@RequestMapping("/wallettransaction")
public class WalletTransactionController {

    @Autowired
    RewardTransactionRepository rewardTransactionRepository;

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("list", rewardTransactionRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("list", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading wallet transactions: " + ex.getMessage());
        }
        return "admin/referral_rewards/wallettransaction-list";
    }

}
