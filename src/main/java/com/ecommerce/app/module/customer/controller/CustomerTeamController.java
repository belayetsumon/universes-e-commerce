/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customer-team")
@PreAuthorize("hasAuthority('customer')")
public class CustomerTeamController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    ReferralRepository referralRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model, Principal principal) {

        Optional<Users> users = usersRepository.findByEmail(principal.getName());

        List< Referral> refeList = referralRepository.findAllByReferredUser(users.get());

        model.addAttribute("refeList", refeList);
        return "customer/team";
    }

}
