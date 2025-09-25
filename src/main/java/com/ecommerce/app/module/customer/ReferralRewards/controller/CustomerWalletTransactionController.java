/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customerwallettransaction")
public class CustomerWalletTransactionController {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    WalletTransactionRepository walletTransactionRepository;

    @RequestMapping("/list")
    public String list(Model model, Principal principal) {
        // Fetch the user by email from principal
        Users user = usersRepository.findByEmail(principal.getName()).get();
        if (user == null) {
            // Handle user not found scenario (redirect to login page or error page)
            return "redirect:/login";
        }

        // Fetch wallet by user
        Wallet wallet = walletRepository.findByUsers(user).orElse(null);

        // Fetch transactions if wallet exists, else empty list
        List<WalletTransaction> txns = (wallet != null)
                ? walletTransactionRepository.findByWallet(wallet)
                : List.of();

        model.addAttribute("wallet", wallet);
        model.addAttribute("list", txns);
        return "customer/referral_rewards/wallettransaction-list";
    }

}
