package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.GiftCardTransactionRepository;
import java.util.Collections;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/giftcard-transaction")
public class GiftCardTransactionController {

    private final GiftCardTransactionRepository giftCardTransactionRepository;

    public GiftCardTransactionController(GiftCardTransactionRepository giftCardTransactionRepository) {
        this.giftCardTransactionRepository = giftCardTransactionRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("transactions", giftCardTransactionRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("transactions", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading gift card transactions: " + ex.getMessage());
        }
        return "admin/referral_rewards/giftcard-transaction-list";
    }
}

