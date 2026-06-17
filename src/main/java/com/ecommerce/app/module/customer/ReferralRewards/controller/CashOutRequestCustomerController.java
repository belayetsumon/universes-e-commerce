/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.CashOutRequest;
import com.ecommerce.app.module.ReferralRewards.repository.CashOutRequestRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.security.Principal;
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
@RequestMapping("/cashoutcustomerrequest")
public class CashOutRequestCustomerController {

    @Autowired
    private CashOutRequestRepository cashOutRequestRepository;
    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/list")
    public String list(Model model, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return "redirect:/login";
        }

        Users user = usersRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("requests", cashOutRequestRepository.findByUserOrderByRequestedAtDesc(user));
        return "customer/referral_rewards/cash-out-request-list";
    }

}
