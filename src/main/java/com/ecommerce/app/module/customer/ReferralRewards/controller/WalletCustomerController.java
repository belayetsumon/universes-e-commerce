/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.CashOutRequest;
import com.ecommerce.app.module.ReferralRewards.model.CashOutStatus;
import com.ecommerce.app.module.ReferralRewards.model.CustomerCashOutPaymentMethod;
import com.ecommerce.app.module.ReferralRewards.repository.CashOutRequestRepository;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.repository.RewardAccountRepository;
import com.ecommerce.app.module.ReferralRewards.services.RedemptionService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    private static final BigDecimal CASHOUT_CONVERSION_RATE = new BigDecimal("0.01");

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private RewardAccountRepository rewardAccountRepository;

    @Autowired
    RedemptionService redemptionService;

    @Autowired
    GiftCardRepository giftCardRepository;

    @Autowired
    CashOutRequestRepository cashOutRequestRepository;

    @GetMapping("/wallet")
    public String viewWallet(Model model, Principal principal) {
        Optional<Users> user = resolvePrincipalUser(principal);
        if (user.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("wallet", resolveWalletBalance(user.get()));

        return "customer/referral_rewards/wallet";
    }

    @GetMapping("/wallet/cashout")
    public String cashOutForm(Model model, Principal principal) {
        Optional<Users> user = resolvePrincipalUser(principal);
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("wallet", resolveWalletBalance(user.get()));
        model.addAttribute("paymentMethods", CustomerCashOutPaymentMethod.values());
        return "customer/referral_rewards/cash_out_request";
    }

    @PostMapping("/wallet/generate-giftcard")
    public String generateGiftCard(@RequestParam BigDecimal points, Principal principal, RedirectAttributes redirect) {
        Users user = usersRepository.findByEmail(principal.getName()).orElseThrow();

//        BigDecimal availablePoints = walletTransactionRepository
//                .sumAmountByWalletAndExpiryDateAfterAndRedeemedFalse(
//                        user.getId(), LocalDateTime.now()
//                ).orElse(BigDecimal.ZERO);
//
//        if (points.compareTo(availablePoints) > 0) {
//            redirect.addFlashAttribute("error", "Insufficient points.");
//            return "redirect:/wallet";
//        }
        BigDecimal value = points.multiply(CASHOUT_CONVERSION_RATE);

        // 1. Redeem points
        boolean redeemed = redemptionService.redeemPoints(user, points, "GIFTCARD", "Points converted to gift card");
        if (!redeemed) {
            redirect.addFlashAttribute("error", "Insufficient wallet balance for gift card conversion.");
            return "redirect:/customerwallet/wallet";
        }

        // 2. Create gift card
//        GiftCard card = new GiftCard();
//        card.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
//        card.setValue(value);
//        card.setIssuedTo(user);
//        card.setCreatedAt(LocalDateTime.now());
//        card.setRedeemed(false);
//        giftCardRepository.save(card);
//        redirect.addFlashAttribute("message", "Gift card created: " + card.getCode());
        return "redirect:/customerwallet/wallet";
    }

    @PostMapping("/wallet/cashout")
    @Transactional
    public String cashOutPoints(@RequestParam BigDecimal points,
            @RequestParam CustomerCashOutPaymentMethod paymentMethod,
            Principal principal,
            RedirectAttributes redirect) {
        Users user = usersRepository.findByEmail(principal.getName()).orElseThrow();

        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            redirect.addFlashAttribute("error", "Cash out points must be greater than zero.");
            return "redirect:/customerwallet/wallet/cashout";
        }

        BigDecimal cashAmount = points.multiply(CASHOUT_CONVERSION_RATE).setScale(2, RoundingMode.HALF_UP);

        boolean redeemed = redemptionService.redeemPoints(
                user,
                points,
                "CASHOUT",
                "Cash out to " + paymentMethod.name() + " payment method"
        );
        if (!redeemed) {
            redirect.addFlashAttribute("error", "Insufficient wallet balance for cash out.");
            return "redirect:/customerwallet/wallet/cashout";
        }

        CashOutRequest cashOut = new CashOutRequest();
        cashOut.setUser(user);
        cashOut.setAmount(cashAmount);
        cashOut.setPaymentMethod(paymentMethod);
        cashOut.setStatus(CashOutStatus.PENDING);
        cashOut.setRequestedAt(LocalDateTime.now());
        cashOutRequestRepository.save(cashOut);

        redirect.addFlashAttribute(
                "message",
                "Cash out request submitted successfully. Payout amount: " + cashAmount + "."
        );
        return "redirect:/cashoutcustomerrequest/list";
    }

    private Optional<Users> resolvePrincipalUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return Optional.empty();
        }
        return usersRepository.findByEmail(principal.getName());
    }

    private BigDecimal resolveWalletBalance(Users user) {
        return rewardAccountRepository.findByUsers(user)
                .map(com.ecommerce.app.module.ReferralRewards.model.RewardAccount::getBalance)
                .orElse(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

}
