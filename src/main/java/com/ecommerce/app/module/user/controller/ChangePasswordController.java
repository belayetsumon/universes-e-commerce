/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Md Belayet Hossin
 */
@Controller

@RequestMapping("/changepassword")
@PreAuthorize("hasAuthority('changepassword')")
public class ChangePasswordController {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

 
    @RequestMapping(value = {"","/", "/index","/changepassword"})

    public String changepassword(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Users users = usersRepository.findByGovernmentId(auth.getName());
        

     //   model.addAttribute("user_name", users.getName());

        return "/pims/users/change_password";
    }

    @RequestMapping("/update")

    public String update(@RequestParam(required = false, name = "governmentId") String governmentId,
            @RequestParam(required = false, name = "confirmPassword") String confirmPassword, Model model, RedirectAttributes redirectAttributes) {
        Users users = new Users();
     //   users = usersRepository.findByGovernmentId(governmentId);
        users.setPassword(bCryptPasswordEncoder.encode(confirmPassword));
        usersRepository.save(users);
        model.addAttribute("changePasswordSucess", " Your password has been changed successfully");
        return "/pims/users/change_password";
    }

}
