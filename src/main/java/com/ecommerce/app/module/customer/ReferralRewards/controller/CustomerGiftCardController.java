package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer-giftcard")
public class CustomerGiftCardController {

    private final UsersRepository usersRepository;
    private final GiftCardRepository giftCardRepository;

    public CustomerGiftCardController(UsersRepository usersRepository, GiftCardRepository giftCardRepository) {
        this.usersRepository = usersRepository;
        this.giftCardRepository = giftCardRepository;
    }

    @GetMapping("/list")
    public String list(Model model, Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("giftCards", giftCardRepository.findByIssuedTo(user));
        return "customer/referral_rewards/giftcard-list";
    }
}

