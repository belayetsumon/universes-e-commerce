package com.ecommerce.app.module.customer.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.dto.GiftCardPaymentForm;
import com.ecommerce.app.module.ReferralRewards.dto.GiftCardPurchaseForm;
import com.ecommerce.app.module.ReferralRewards.model.GiftCardPurchase;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import com.ecommerce.app.module.ReferralRewards.services.GiftCardPurchaseService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.order.model.PaymentMethod;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer-giftcard")
public class CustomerGiftCardController {

    private final UsersRepository usersRepository;
    private final GiftCardRepository giftCardRepository;
    private final GiftCardPurchaseService giftCardPurchaseService;

    public CustomerGiftCardController(
            UsersRepository usersRepository,
            GiftCardRepository giftCardRepository,
            GiftCardPurchaseService giftCardPurchaseService) {
        this.usersRepository = usersRepository;
        this.giftCardRepository = giftCardRepository;
        this.giftCardPurchaseService = giftCardPurchaseService;
    }

    @GetMapping("/list")
    public String list(Model model, Principal principal) {
        Optional<Users> currentUser = resolvePrincipalUser(principal);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }
        Users user = currentUser.get();
        model.addAttribute("giftCards", giftCardRepository.findByIssuedTo(user));
        model.addAttribute("purchases", giftCardPurchaseService.findPurchasesForCustomer(user));
        return "customer/referral_rewards/giftcard-list";
    }

    @GetMapping("/buy")
    public String buyForm(Model model, Principal principal) {
        if (resolvePrincipalUser(principal).isEmpty()) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("purchaseForm")) {
            model.addAttribute("purchaseForm", new GiftCardPurchaseForm());
        }
        addBuyFormAttributes(model);
        return "customer/referral_rewards/giftcard-buy";
    }

    @PostMapping("/buy")
    public String createPurchase(
            @Valid @ModelAttribute("purchaseForm") GiftCardPurchaseForm purchaseForm,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Users> currentUser = resolvePrincipalUser(principal);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addBuyFormAttributes(model);
            return "customer/referral_rewards/giftcard-buy";
        }

        try {
            GiftCardPurchase purchase = giftCardPurchaseService.createPendingPurchase(currentUser.get(), purchaseForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Gift card purchase created. Complete payment to activate the gift card."
            );
            return "redirect:/customer-giftcard/payment/" + purchase.getUuid();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            addBuyFormAttributes(model);
            return "customer/referral_rewards/giftcard-buy";
        }
    }

    @GetMapping("/payment/{uuid}")
    public String paymentForm(
            @PathVariable String uuid,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        Optional<Users> currentUser = resolvePrincipalUser(principal);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        try {
            GiftCardPurchase purchase = giftCardPurchaseService.getPurchaseForCustomer(uuid, currentUser.get());
            model.addAttribute("purchase", purchase);
            if (!model.containsAttribute("paymentForm")) {
                model.addAttribute("paymentForm", new GiftCardPaymentForm());
            }
            addPaymentFormAttributes(model);
            return "customer/referral_rewards/giftcard-payment";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customer-giftcard/list";
        }
    }

    @PostMapping("/payment/{uuid}")
    public String completePayment(
            @PathVariable String uuid,
            @Valid @ModelAttribute("paymentForm") GiftCardPaymentForm paymentForm,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        Optional<Users> currentUser = resolvePrincipalUser(principal);
        if (currentUser.isEmpty()) {
            return "redirect:/login";
        }

        GiftCardPurchase purchase;
        try {
            purchase = giftCardPurchaseService.getPurchaseForCustomer(uuid, currentUser.get());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customer-giftcard/list";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("purchase", purchase);
            addPaymentFormAttributes(model);
            return "customer/referral_rewards/giftcard-payment";
        }

        try {
            GiftCardPurchase paidPurchase = giftCardPurchaseService.completePayment(uuid, currentUser.get(), paymentForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment confirmed. Gift card "
                    + (paidPurchase.getGiftCard() != null ? paidPurchase.getGiftCard().getCode() : "")
                    + " is now active."
            );
            return "redirect:/customer-giftcard/list";
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("purchase", purchase);
            model.addAttribute("errorMessage", "Payment could not be confirmed because the reference is already used.");
            addPaymentFormAttributes(model);
            return "customer/referral_rewards/giftcard-payment";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("purchase", purchase);
            model.addAttribute("errorMessage", ex.getMessage());
            addPaymentFormAttributes(model);
            return "customer/referral_rewards/giftcard-payment";
        }
    }

    private void addBuyFormAttributes(Model model) {
        model.addAttribute("quickAmounts", List.of(
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("5000.00")
        ));
    }

    private void addPaymentFormAttributes(Model model) {
        model.addAttribute("paymentMethods", List.of(PaymentMethod.SSLCOMMERZ, PaymentMethod.BKASH));
    }

    private Optional<Users> resolvePrincipalUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return Optional.empty();
        }
        return usersRepository.findByEmail(principal.getName());
    }
}
