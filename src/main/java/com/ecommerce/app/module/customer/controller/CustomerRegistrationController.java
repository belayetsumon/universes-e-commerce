/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.componant.UserValidator;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.module.user.services.LoginEventService;
import com.ecommerce.app.module.user.services.UsersService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashSet;
import java.util.Optional;
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

    @Autowired
    UsersService usersService;

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    WalletRepository walletRepository;

    @RequestMapping("/registration")
    public String index(Model model, Users users) {

//        Users userId = new Users();
//        userId.setId(loggedUserService.activeUserid());
//
//        users.setParent(userId);
//
//        model.addAttribute("userId", userId.getId());
        return "customer/customer_registration";
    }

    @RequestMapping("/customer_registration_save")
    public String registrationSave(Model model, @Valid Users users, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Principal principal
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

//        users.setReferralcode(output);
        usersRepository.save(users);

        Optional<Users> referringUsers = usersRepository.findByEmail(principal.getName());

//        if (logduser != null) {
//            Optional<Referral> referringReferral = referralRepository.findByUsers_Id(logduser);
//
//            if (referringReferral.isPresent()) {
//                referringUsers = referringReferral.get().getUsers();
//            }
//        }
        // Create referral code for this user
        Referral referral = new Referral();

        referral.setReferralCode(usersService.generateRefaraleCode());
        referral.setUsers(users);
        referral.setReferredUser(referringUsers.get());
        referral.setRewardGranted(true);

        referralRepository.save(referral);

        Wallet wallet = new Wallet();

        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsers(users);
        walletRepository.save(wallet);

        redirectAttributes.addFlashAttribute(
                "success", "Congratulations! You have successfully registered.");

        redirectAttributes.addFlashAttribute("success", " Congratulations you have successfully registered.");
        return "redirect:/customer-team/index";
    }

}
