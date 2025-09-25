/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.CashOutRequest;
import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.model.Wallet;
import com.ecommerce.app.module.ReferralRewards.repository.CashOutRequestRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletRepository;
import com.ecommerce.app.module.ReferralRewards.repository.WalletTransactionRepository;
import com.ecommerce.app.module.ReferralRewards.services.RedemptionService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customerwallet")
public class WalletCustomerController {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    RedemptionService redemptionService;

    @Autowired
    GiftCardRepository giftCardRepository;

    @Autowired
    CashOutRequestRepository cashOutRequestRepository;

    @GetMapping("/wallet")
    public String viewWallet(Model model, Principal principal) {
        // Fetch the user by email from principal
        Optional<Users> user = usersRepository.findByEmail(principal.getName());
        if (user.get() == null) {
            // Handle user not found scenario (redirect to login page or error page)
            return "redirect:/login";
        }

        // Fetch wallet by user
        BigDecimal wBlance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        Wallet wallet = walletRepository.findByUsers(user.get()).orElse(null);

        if (wallet != null) {
            // Handle user not found scenario (redirect to login page or error page)
            model.addAttribute("wallet", wallet.getBalance());
        }

        model.addAttribute("wallet", wBlance);

        return "customer/referral_rewards/wallet";
    }

    @PostMapping("/wallet/generate-giftcard")
    public String generateGiftCard(@RequestParam BigDecimal points, Principal principal, RedirectAttributes redirect) {
        Users user = usersRepository.findByEmail(principal.getName()).orElseThrow();

        BigDecimal availablePoints = walletTransactionRepository
                .sumAmountByWalletAndExpiryDateAfterAndRedeemedFalse(
                        user.getWallet(), LocalDateTime.now()
                ).orElse(BigDecimal.ZERO);

        if (points.compareTo(availablePoints) > 0) {
            redirect.addFlashAttribute("error", "Insufficient points.");
            return "redirect:/wallet";
        }

        BigDecimal conversionRate = new BigDecimal("0.01"); // 1 point = $0.01
        BigDecimal value = points.multiply(conversionRate);

        // 1. Redeem points
        redemptionService.redeemPoints(user, points, "GIFTCARD", "Points converted to gift card");

        // 2. Create gift card
        GiftCard card = new GiftCard();
        card.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        card.setValue(value);
        card.setIssuedTo(user);
        card.setCreatedAt(LocalDateTime.now());
        card.setRedeemed(false);
        giftCardRepository.save(card);

        redirect.addFlashAttribute("message", "Gift card created: " + card.getCode());
        return "redirect:/wallet";
    }

    @PostMapping("/wallet/cashout")
    public String cashOutPoints(@RequestParam BigDecimal points, Principal principal, RedirectAttributes redirect) {
        Users user = usersRepository.findByEmail(principal.getName()).orElseThrow();

        BigDecimal availablePoints = walletTransactionRepository
                .sumAmountByWalletAndExpiryDateAfterAndRedeemedFalse(
                        user.getWallet(), LocalDateTime.now()
                ).orElse(BigDecimal.ZERO);

        if (points.compareTo(availablePoints) > 0) {
            redirect.addFlashAttribute("error", "Insufficient reward points.");
            return "redirect:/wallet";
        }

        BigDecimal conversionRate = new BigDecimal("0.01"); // 1 point = $0.01
        BigDecimal cashAmount = points.multiply(conversionRate);

        // 1. Redeem points
        redemptionService.redeemPoints(user, points, "CASHOUT", "Cash out to payment method");

        // 2. Create cashout request
        CashOutRequest cashOut = new CashOutRequest();
        cashOut.setUser(user);
        cashOut.setAmount(cashAmount);
        cashOut.setRequestedAt(LocalDateTime.now());
//        cashOut.setStatus("PENDING");
        cashOutRequestRepository.save(cashOut);

        // 3. Notify admin or trigger payment process
        redirect.addFlashAttribute("message", "Cash out requested: $" + cashAmount);
        return "redirect:/wallet";
    }

}
