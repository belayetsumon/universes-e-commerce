/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.customer.controller;

import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.componant.UserValidator;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.module.user.services.LoginEventService;
import jakarta.validation.Valid;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customer_registration")
public class CustomerRegistrationController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserValidator userValidator;

    @Autowired
    LoginEventService loginEventService;

    @RequestMapping("/registration")
    public String index(Model model, Users users) {

        Users userId = new Users();
        userId.setId(loggedUserService.activeUserid());

        users.setParent(userId);
        
        model.addAttribute("userId", userId.getId());

        return "customer/customer_registration";
    }

    @RequestMapping("/customer_registration_save")
    public String registrationSave(Model model, @Valid Users users, BindingResult bindingResult, RedirectAttributes redirectAttributes

    ) {

    // userValidator.validate(users, bindingResult);

//       Users parents = usersRepository.findByReferralcode(users.getParent().getId().toString());
//
//        if (parents == null) {
//
//            ObjectError cartItemListError;
//
//            cartItemListError = new ObjectError("parent", "Your referral code is invalid");
//
//            bindingResult.addError(cartItemListError);
//        }

        if (bindingResult.hasErrors()) {

             return "customer/customer_registration";
        }

        Set<Role> customerRole = new HashSet<Role>();
        Role role = roleRepository.findBySlug("customer");
        customerRole.add(role);

        users.setRole(customerRole);

        users.setUserType(UserType.customer);

        users.setStatus(Status.Active);
        users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));

       // users.setParent(parents);

        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 10; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();

        users.setReferralcode(output);

        usersRepository.save(users);

        redirectAttributes.addFlashAttribute("success", " Congratulations you have successfully registered.");
        return "redirect:/customer-team/index";
    }

}
