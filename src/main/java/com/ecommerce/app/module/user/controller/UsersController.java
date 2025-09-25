/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.exception.ForeignKeyConstraintException;
import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.user.componant.UserValidator;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.RoleRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoginEventService;
import com.ecommerce.app.module.user.services.UsersService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Md Belayet Hossin
 */
@Controller
@RequestMapping("/users")
//@PreAuthorize("hasAuthority('users')")
public class UsersController {

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
    private ReferralRepository referralRepository;

    @Autowired
    UsersService usersService;

    @Autowired
    WalletRepository walletRepository;

    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("alluser", usersRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "user/allusers";
    }

    @RequestMapping("/userbystatus")
    public String userByStatus(Model model, @RequestParam(value = "status", required = false) Status status) {
        model.addAttribute("status", Status.values());
        // model.addAttribute("alluser", usersRepository.findByStatus(status));
        return "user/allusers_by_status";
    }

    @RequestMapping("/view/{uid}")
    public String view(Model model, @PathVariable Long uid) {
        model.addAttribute("users", usersRepository.findById(uid));
        return "user/view";
    }

    @RequestMapping("/registrations")
    public String registrations(Model model, Users users) {
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("status", Status.values());
        model.addAttribute("userTypes", UserType.values());

        return "user/registrations";
    }

    @RequestMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Long id, Users users) {
        model.addAttribute("users", usersRepository.findById(id).orElse(null));
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("status", Status.values());
        model.addAttribute("userTypes", UserType.values());
        return "user/registrations";
    }

    @RequestMapping("/save")
    //@Transactional
    public String save(Model model, @RequestParam(value = "password2", required = false) String password2, @Valid Users users, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("status", Status.values());
            model.addAttribute("userTypes", UserType.values());
            return "user/registrations";
        }

        // users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
        try {

            if (users.getPassword().isEmpty() && password2 != null && users.getId() != null) {

                users.setPassword(password2);
            } else {

                users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
            }

            usersRepository.save(users);
            return "redirect:/users/index";

        } catch (Exception e) {

            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("status", Status.values());
            model.addAttribute("userTypes", UserType.values());
            redirectAttributes.addFlashAttribute("message", e);
            model.addAttribute("message", e);
            return "user/registrations";
        }
    }

    @RequestMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Long id, Users users) {
        usersRepository.deleteById(id);
        return "redirect:/users/index";
    }

    @GetMapping("/deletewithexception/{id}")
    public String deletewithexception(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usersService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (ForeignKeyConstraintException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/users/index";
    }

    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("attribute", "value");
        model.addAttribute("logout", " You are successfully logout");
        return "user/login";
    }

    @RequestMapping("/logout")
    public String logout(Model model, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/";
    }

    @RequestMapping("/detailsinfo/{id}")
    public String details(Model model, @PathVariable Long id) {
        model.addAttribute("employee", usersRepository.findById(id));
        return "pims/details/details";
    }

    @RequestMapping("/uregistrations")
    public String uregistrations(Model model, Users users) {

        model.addAttribute("role", roleRepository.findAll());
        return "user/uregistrations";
    }

    @RequestMapping("/usave")
    public String usave(Model model, @Valid Users users, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("role", roleRepository.findAll());
            return "user/uregistrations";
        }
        users.setStatus(Status.Pending);
        users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
        usersRepository.save(users);
        redirectAttributes.addAttribute("success", " Congratulations you have successfully registered. please contact with system adminstrator.");
        return "redirect:users/uregistrations";
    }

    @RequestMapping("/frontRegistrationSave")
    public String frontUserSave(Model model, @Valid Users users, BindingResult bindingResult, RedirectAttributes redirectAttributes,
            @RequestParam(name = "parent", required = false) String parent,
            @RequestParam(name = "ref_code", required = false) String ref
    ) {
        // System.out.println("ref_code" + ref);
        //userValidator.validate(users, bindingResult);
        //   Users parents = usersRepository.findByReferralcode(parent);
//        if (parents == null) {
//
//            ObjectError cartItemListError;
//
//            cartItemListError = new ObjectError("parent", "Your referral code is invalid");
//
//            bindingResult.addError(cartItemListError);
//        }
        if (bindingResult.hasErrors()) {

            return "frontview/front-registration";
        }

        Set<Role> customerRole = new HashSet<Role>();
        Role role = roleRepository.findBySlug("customer");
        customerRole.add(role);

        users.setRole(customerRole);

        users.setUserType(UserType.customer);

        users.setStatus(Status.Active);

        users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));

        usersRepository.save(users);

        Users referringUsers = null;

        if (ref != null && !ref.isEmpty()) {
            Optional<Referral> referringReferral = referralRepository.findByReferralCode(ref);

            if (referringReferral.isPresent()) {
                referringUsers = referringReferral.get().getUsers();
            }
        }

        // Create referral code for this user
        Referral referral = new Referral();

        referral.setReferralCode(usersService.generateRefaraleCode());
        referral.setUsers(users);
        referral.setReferredUser(referringUsers);

        referralRepository.save(referral);
        Wallet wallet = new Wallet();

        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUsers(users);
        walletRepository.save(wallet);
        redirectAttributes.addFlashAttribute(
                "success", "Congratulations! You have successfully registered.");

        return "redirect:/public/member-login";
    }

}
