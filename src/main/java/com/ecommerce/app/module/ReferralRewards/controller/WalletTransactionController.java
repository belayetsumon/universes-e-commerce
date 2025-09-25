/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/wallettransaction")
public class WalletTransactionController {

    @Autowired
    WalletTransactionRepository walletTransactionRepository;

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("list", walletTransactionRepository.findAll());
        return "admin/referral_rewards/wallettransaction-list";
    }

}
