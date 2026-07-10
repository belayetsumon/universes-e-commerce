/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.events.CommunicationRequestedEvent;
import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.services.ReferralService;
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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    ReferralService referralService;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    @RequestMapping("/registration")
    public String index(Model model,
            @RequestParam(name = "ref", required = false) String referralCode,
            HttpSession session,
            Users users) {

//        Users userId = new Users();
//        userId.setId(loggedUserService.activeUserid());
//
//        users.setParent(userId);
//
//        model.addAttribute("userId", userId.getId());
        String normalizedReferralCode = trimToNull(referralCode);
        if (normalizedReferralCode != null && session != null) {
            session.setAttribute("productShareReferralCode", normalizedReferralCode);
        }

        Object prefilledReferralCode = session == null ? null : session.getAttribute("productShareReferralCode");
        model.addAttribute("prefilledReferralCode", prefilledReferralCode instanceof String ? prefilledReferralCode : "");
        return "frontview/front-registration";
    }

    @RequestMapping("/customer_registration_save")
    public String registrationSave(Model model, @Valid Users users, BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam(name = "ref_code", required = false) String referralCode,
            HttpSession session
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

            model.addAttribute("prefilledReferralCode", trimToEmpty(referralCode));
            return "frontview/front-registration";
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

        Users referringUser = referralService.resolveReferrerByCode(resolveRegistrationReferralCode(referralCode, session));

//        if (logduser != null) {
//            Optional<Referral> referringReferral = referralRepository.findByUsers_Id(logduser);
//
//            if (referringReferral.isPresent()) {
//                referringUsers = referringReferral.get().getUsers();
//            }
//        }
        referralService.createReferralProfileAndGrantSignupReward(users, referringUser);
        applicationEventPublisher.publishEvent(
                CommunicationRequestedEvent.customer(
                    MessageEventType.CUSTOMER_REGISTERED,
                    users,
                    MessageChannel.EMAIL,
                    users.getEmail(),
                    Map.of("customerName", users.getFirstName())
                )
        );

        redirectAttributes.addFlashAttribute(
                "success", "Congratulations! You have successfully registered.");

        redirectAttributes.addFlashAttribute("success", " Congratulations you have successfully registered.");
        return "redirect:/public/member-login";
    }

    private String resolveRegistrationReferralCode(String submittedReferralCode, HttpSession session) {
        String normalizedSubmittedCode = trimToNull(submittedReferralCode);
        if (normalizedSubmittedCode != null) {
            return normalizedSubmittedCode;
        }
        if (session == null) {
            return null;
        }
        Object sharedProductReferralCode = session.getAttribute("productShareReferralCode");
        return sharedProductReferralCode instanceof String ? trimToNull((String) sharedProductReferralCode) : null;
    }

    private String trimToEmpty(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "" : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
