package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.CashbackTransactionRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer-cashback")
public class CustomerCashbackController {

    private final UsersRepository usersRepository;
    private final CashbackTransactionRepository cashbackTransactionRepository;

    public CustomerCashbackController(UsersRepository usersRepository, CashbackTransactionRepository cashbackTransactionRepository) {
        this.usersRepository = usersRepository;
        this.cashbackTransactionRepository = cashbackTransactionRepository;
    }

    @GetMapping("/list")
    public String list(Model model, Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("cashbacks", cashbackTransactionRepository.findByUserOrderByIdDesc(user));
        return "customer/referral_rewards/cashback-list";
    }
}

