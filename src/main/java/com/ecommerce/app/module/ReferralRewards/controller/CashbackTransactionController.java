package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.CashbackTransactionRepository;
import java.util.Collections;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cashback")
public class CashbackTransactionController {

    private final CashbackTransactionRepository cashbackTransactionRepository;

    public CashbackTransactionController(CashbackTransactionRepository cashbackTransactionRepository) {
        this.cashbackTransactionRepository = cashbackTransactionRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("cashbacks", cashbackTransactionRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("cashbacks", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading cashback transactions: " + ex.getMessage());
        }
        return "admin/referral_rewards/cashback-transaction-list";
    }
}

