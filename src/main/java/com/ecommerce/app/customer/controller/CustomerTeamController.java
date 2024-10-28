/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.customer.controller;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import java.util.List;
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

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());
        
        List<Users> usersList = usersRepository.findByParent(userId);
        
         model.addAttribute("alluser",usersList);
        return "customer/team";
    }

}
