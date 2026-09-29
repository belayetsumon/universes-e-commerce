/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.customer.dto.CustomerRegistrationForm;
import com.ecommerce.app.module.customer.services.CustomerRegistrationException;
import com.ecommerce.app.module.customer.services.CustomerRegistrationService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customerregister")
public class RegisterCustomerController {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private CustomerRegistrationService customerRegistrationService;

    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(required = false) String ref, Model model) {
        model.addAttribute("users", new CustomerRegistrationForm());
        model.addAttribute("prefilledReferralCode", ref == null ? "" : ref.trim());
        return "frontview/front-registration";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("users") CustomerRegistrationForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String ref,
            Model model,
            RedirectAttributes redirect) {
        if (!bindingResult.hasErrors()) {
            try {
                customerRegistrationService.register(form, ref);
            } catch (CustomerRegistrationException ex) {
                if (ex.getField() == null || ex.getField().isBlank()) {
                    bindingResult.reject("registration.failed", ex.getMessage());
                } else {
                    bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
                }
            } catch (IllegalArgumentException ex) {
                bindingResult.rejectValue("mobile", "invalid", ex.getMessage());
            } catch (RuntimeException ex) {
                bindingResult.reject("registration.failed", "Registration could not be completed. Please try again.");
            }
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("prefilledReferralCode", ref == null ? "" : ref.trim());
            return "frontview/front-registration";
        }
        redirect.addFlashAttribute("success", "Congratulations! You have successfully registered.");
        return "redirect:/public/member-login";
    }

    @GetMapping("/verify")
    public String legacyEmailVerificationRedirect(@RequestParam(required = false) String token, RedirectAttributes redirect) {
        return "redirect:/login";
    }

    @GetMapping("/wallet")
    public String wallet(Principal principal, Model model) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

//        Wallet wallet = user.getWallet();
//        if (wallet == null) {
//            model.addAttribute("walletBalance", BigDecimal.ZERO);
//            model.addAttribute("transactions", Collections.emptyList());
//        } else {
//            List<WalletTransaction> transactions = walletTransactionRepository
//                    .findByWallet_UsersOrderByCreatedAtDesc(user);
//
//            model.addAttribute("walletBalance", wallet.getBalance()); // Assuming Wallet has getBalance()
//            model.addAttribute("transactions", transactions);
//        }
        return "wallet";
    }

    @GetMapping("/wallets")
    public String wallets(Model model, Principal principal) {
        Users user = usersRepository.findByEmail(principal.getName()).orElseThrow();

        List<WalletTransaction> txs = walletTransactionRepository.findByWallet_Users_Id(user.getId());

        BigDecimal balance = txs.stream()
                .map(t -> {
                    if ("CREDIT".equalsIgnoreCase(t.getType().toString())) {
                        return t.getAmount();
                    } else {
                        return t.getAmount().negate();
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("transactions", txs);
        model.addAttribute("balance", balance);

        return "wallet";
    }

    @GetMapping("/admin/referrals")
    @PreAuthorize("hasAnyAuthority('admin', 'ROLE_ADMIN')")
    public String referralStats(Model model) {
        List<Referral> referrals = referralRepository.findAll();
        model.addAttribute("referrals", referrals);
        return "admin/referrals";
    }

//Verify referral code exists
//    public void registerUser(SignupDto dto) {
//        // Create new user and set basic info
//        Users newUser = new Users();
//        newUser.setEmail(dto.getEmail());
//        // set other fields from dto...
//
//        if (dto.getReferralCode() != null && !dto.getReferralCode().isBlank()) {
//            Users referrer = usersRepository.findByReferralCode(dto.getReferralCode());
//            if (referrer != null) {
//                newUser.setReferredBy(referrer);
//                // Save new user first (with referrer info)
//                usersRepository.save(newUser);
//
//                // Add reward transaction to referrer's wallet
//                WalletTransaction reward = new WalletTransaction();
//                reward.setUsers(referrer);
//                reward.setAmount(BigDecimal.valueOf(50)); // fixed reward points
//                reward.setDescription("Referral signup reward for " + newUser.getEmail());
//                reward.setCreatedAt(LocalDateTime.now());
//                reward.setExpiryDate(LocalDateTime.now().plusMonths(6));
//                reward.setExpired(false);
//                reward.setRedeemed(false);
//                walletTransactionRepository.save(reward);
//
//                // Send email notification to referrer
//                Map<String, Object> vars = new HashMap<>();
//                vars.put("userName", referrer.getName());
//                vars.put("refereeEmail", newUser.getEmail());
//                vars.put("rewardPoints", 50);
//
//                emailService.sendHtmlEmail(
//                        referrer.getEmail(),
//                        "New Referral Signup - Reward Credited",
//                        "email/newReferralReward",
//                        vars);
//
//                // Optional SMS notification
//                smsService.sendSms(
//                        referrer.getMobile(),
//                        "You earned 50 reward points for referring " + newUser.getEmail());
//            } else {
//                // Referral code invalid, just save the user without referrer
//                usersRepository.save(newUser);
//            }
//        } else {
//            // No referral code, save user normally
//            usersRepository.save(newUser);
//        }
//    }
}
