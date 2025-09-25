/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customerreferral")
public class ReferralCustomerController {

    @Autowired
    ReferralRepository referralRepository;

    @Autowired
    private UsersRepository usersRepository;

    @RequestMapping("/list")
    public String list(Model model, Principal principal) {

        Users users = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List< Referral> refeList = referralRepository.findAllByReferredUser(users);

        model.addAttribute("list", refeList);

        return "customer/referral_rewards/customer_referrallist";
    }

}
