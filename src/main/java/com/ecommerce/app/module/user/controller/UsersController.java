/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.exception.ForeignKeyConstraintException;
import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.user.componant.UserValidator;
import com.ecommerce.app.module.user.dto.AdminUserPasswordForm;
import com.ecommerce.app.module.user.model.LoginHistory;
import com.ecommerce.app.module.user.model.Role;
import com.ecommerce.app.module.user.model.Status;
import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.LoginHistoryRepository;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    LoginHistoryRepository loginHistoryRepository;

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

    @Autowired
    ReferralService referralService;

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
    public String view(Model model, @PathVariable Long uid, RedirectAttributes redirectAttributes) {
        Users user = usersRepository.findById(uid).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/users/index";
        }
        model.addAttribute("users", user);
        return "user/view";
    }

    @GetMapping("/login-history")
    public String loginHistory(Model model) {
        model.addAttribute("historyEntries", loginHistoryRepository.findAllForAdminList());
        model.addAttribute("historyTitle", "All User Login History");
        model.addAttribute("historySubtitle", "Recent login, logout, failed attempt, and session activity records across all users.");
        model.addAttribute("backUrl", "/users/index");
        model.addAttribute("backLabel", "Back to Users");
        model.addAttribute("selectedUser", null);
        return "user/login_history";
    }

    @GetMapping("/login-history/{uid}")
    public String userLoginHistory(Model model, @PathVariable Long uid, RedirectAttributes redirectAttributes) {
        Users user = usersRepository.findById(uid).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/users/index";
        }

        List<LoginHistory> historyEntries = loginHistoryRepository.findByUserIdForAdminList(uid);
        model.addAttribute("historyEntries", historyEntries);
        model.addAttribute("historyTitle", "Login History");
        model.addAttribute("historySubtitle", "Detailed login activity for " + buildDisplayName(user) + ".");
        model.addAttribute("backUrl", "/users/view/" + uid);
        model.addAttribute("backLabel", "Back to Profile");
        model.addAttribute("selectedUser", user);
        return "user/login_history";
    }

    @GetMapping("/change-password/{id}")
    public String changePasswordForm(Model model, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        Users user = usersRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/users/index";
        }

        model.addAttribute("users", user);
        model.addAttribute("passwordForm", new AdminUserPasswordForm());
        return "user/change_password";
    }

    @PostMapping("/change-password/{id}")
    public String changePassword(
            Model model,
            @PathVariable Long id,
            @Valid @ModelAttribute("passwordForm") AdminUserPasswordForm passwordForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Users user = usersRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/users/index";
        }

        if (!bindingResult.hasFieldErrors("confirmPassword")
                && passwordForm.getNewPassword() != null
                && !passwordForm.getNewPassword().equals(passwordForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "New password and confirmation password must match.");
        }

        if (!bindingResult.hasFieldErrors("newPassword")
                && passwordMatches(passwordForm.getNewPassword(), user.getPassword())) {
            bindingResult.rejectValue("newPassword", "same", "Choose a new password that is different from the current one.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", user);
            return "user/change_password";
        }

        user.setPassword(bCryptPasswordEncoder.encode(passwordForm.getNewPassword()));
        usersRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Password updated successfully for " + user.getFirstName() + ".");
        return "redirect:/users/view/" + user.getId();
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

        Users referringUsers = referralService.resolveReferrerByCode(ref);
        referralService.createReferralProfileAndGrantSignupReward(users, referringUsers);
        redirectAttributes.addFlashAttribute(
                "success", "Congratulations! You have successfully registered.");

        return "redirect:/public/member-login";
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return bCryptPasswordEncoder.matches(rawPassword, storedPassword);
        }

        return rawPassword.equals(storedPassword);
    }

    private String buildDisplayName(Users user) {
        if (user == null) {
            return "Unknown User";
        }

        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail().trim();
        }

        if (user.getMobile() != null && !user.getMobile().isBlank()) {
            return user.getMobile().trim();
        }

        return "User #" + user.getId();
    }

}
