/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.Referral;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.model.WalletTransaction;
import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.ReferralRewards.services.ReferralRewardService;
import com.ecommerce.app.module.ReferralRewards.services.WalletTransactionService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    ReferralRewardService referralRewardService;

    @Autowired
    WalletTransactionService walletTransactionService;

    @Autowired
    WalletRepository walletRepository;

    @GetMapping("/register")
    public String showRegisterForm(@RequestParam(required = false) String ref, Model model) {
        model.addAttribute("user", new Users());
        model.addAttribute("ref", ref);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute Users user, @RequestParam(required = false) String ref, RedirectAttributes redirect) {
        if (usersRepository.findByEmail(user.getEmail()).isPresent()) {
            redirect.addFlashAttribute("error", "Email already registered!");
            return "redirect:/register";
        }

        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
//        user.setVerified(false);
//        user.setEmailVerificationToken(UUID.randomUUID().toString());
        usersRepository.save(user);

        // Create referral code for this user
        Referral referral = new Referral();
        referral.setReferralCode(generateReferralCode());
        referral.setUsers(user);
        referralRepository.save(referral);

        // Link referral if ref param present
        if (ref != null && !ref.isEmpty()) {
            Optional<Referral> referringReferral = referralRepository.findByReferralCode(ref);
            if (referringReferral.isPresent() && referringReferral.get().getReferredUser() == null) {
                Referral r = referringReferral.get();
                r.setReferredUser(user);
                referralRepository.save(r);
            }
        }

        // TODO: Send verification email with token (user.getEmailVerificationToken())
        redirect.addFlashAttribute("message", "Registration successful! Please verify your email.");
        return "redirect:/login";
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirect) {
//        Optional<Users> userOpt = usersRepository.findByEmailVerificationToken(token);
//        if (userOpt.isPresent()) {
//            Users users = userOpt.get();
////            user.setVerified(true);
////            user.setEmailVerificationToken(null);
//            usersRepository.save(users);
//            redirect.addFlashAttribute("message", "Email verified! You can login now.");
//        } else {
//            redirect.addFlashAttribute("error", "Invalid verification token.");
//        }
        return "redirect:/login";
    }

    private String generateReferralCode() {
        // Generate a UUID, remove dashes, and take the first 6 uppercase characters
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6).toUpperCase();
    }

    @GetMapping("/wallet")
    public String wallet(Principal principal, Model model) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            model.addAttribute("walletBalance", BigDecimal.ZERO);
            model.addAttribute("transactions", Collections.emptyList());
        } else {
            List<WalletTransaction> transactions = walletTransactionRepository
                    .findByWallet_UsersOrderByCreatedAtDesc(user);

            model.addAttribute("walletBalance", wallet.getBalance()); // Assuming Wallet has getBalance()
            model.addAttribute("transactions", transactions);
        }

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
    public String referralStats(Model model) {
        List<Referral> referrals = referralRepository.findAll();
        model.addAttribute("referrals", referrals);
        return "admin/referrals";
    }

    @PostMapping("/order/complete")
    public String completeOrder(@RequestParam Long userId, @RequestParam BigDecimal total) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

//        Orders order = new Orders();
//        order.setUsers(user); // Set the User entity, not just the ID
////        order.setStatus("COMPLETED");
//        order.setCreatedAt(LocalDateTime.now());
//        order.setAmount(total);
//        orderRepository.save(order);
        referralRewardService.grantReferralReward(userId);

        return "redirect:/dashboard";
    }

    @PostMapping("/order/create")
    public String createOrder(@RequestParam double amount, Principal principal, RedirectAttributes redirect) {
        Users user = usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Fetch wallet linked to the user
        Wallet wallet = walletRepository.findByUsers(user).get();

        BigDecimal amountBD = BigDecimal.valueOf(amount);

        // Check if wallet has sufficient balance
        if (wallet.getBalance().compareTo(amountBD) < 0) {
            redirect.addFlashAttribute("error", "Insufficient wallet balance. Please add funds or choose another payment method.");
            return "redirect:/cart";
        }

        // Deduct wallet balance
        boolean deducted = walletTransactionService.deductFromWallet(wallet, amountBD, "Purchase order payment");

        if (!deducted) {
            redirect.addFlashAttribute("error", "Could not deduct wallet balance. Please try again.");
            return "redirect:/cart";
        }

//        // Create order and save (linking user)
//        Orders order = new Orders();
//        order.setUsers(user);
//        order.setAmount(amountBD);
////    order.setStatus("COMPLETED");
//        order.setCreatedAt(LocalDateTime.now());
//        orderRepository.save(order);
        redirect.addFlashAttribute("message", "Order placed successfully using wallet balance!");
        return "redirect:/orders";
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
