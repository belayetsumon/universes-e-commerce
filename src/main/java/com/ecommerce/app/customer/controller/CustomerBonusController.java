/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.customer.controller;

import com.ecommerce.app.model.Profile;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.ripository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.order.repository.SalesOrderRepository;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customer-bonus")
@PreAuthorize("hasAuthority('customer')")
public class CustomerBonusController {

    @Autowired
    ProfileRepository profileRepository;



    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @RequestMapping(value = {"", "/", "/index", "dashboards"})
    public String index(Model model, Profile profile) {

        model.addAttribute("username", loggedUserService.activeUserName());
        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());
      

   

      

        return "customer/bonus";
    }

}
