/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.services.WalletService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/wallet")
public class WalletTopUpController {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private WalletService walletService;

    // Show wallet top-up form
    @GetMapping("/top-up")
    public String showTopUpForm(Model model) {
        model.addAttribute("topUpAmount", 0);
        return "wallet-top-up";
    }

    // Handle top-up form submission
    @PostMapping("/top-up")
    public String processTopUp(@RequestParam BigDecimal amount, Principal principal, RedirectAttributes redirect) {
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirect.addFlashAttribute("error", "Amount must be greater than zero.");
            return "redirect:/wallet/top-up";
        }

        // Find user
        Users user = usersRepository.findByEmail(principal.getName()).orElseThrow();

        // Credit wallet
        walletService.creditWallet(user, amount, "Manual wallet top-up");

        // Add success message
        redirect.addFlashAttribute("message", "Wallet topped up successfully by $" + amount);
        return "redirect:/wallet";
    }

}
