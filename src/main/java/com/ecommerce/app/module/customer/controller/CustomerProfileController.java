/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.user.model.*;
import com.ecommerce.app.module.user.ripository.*;
import com.ecommerce.app.module.user.services.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customer-profile")
@PreAuthorize("hasAuthority('customer')")
public class CustomerProfileController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("username", loggedUserService.activeUserName());
        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());

        return "customer/profile";
    }

}
