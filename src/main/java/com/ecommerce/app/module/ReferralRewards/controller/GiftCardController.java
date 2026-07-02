package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.GiftCard;
import com.ecommerce.app.module.ReferralRewards.enumvalue.GiftCardStatus;
import com.ecommerce.app.module.ReferralRewards.repository.GiftCardRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/giftcard")
public class GiftCardController {

    private final GiftCardRepository giftCardRepository;

    public GiftCardController(GiftCardRepository giftCardRepository) {
        this.giftCardRepository = giftCardRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("giftCards", giftCardRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("giftCards", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading gift cards: " + ex.getMessage());
        }
        return "admin/referral_rewards/giftcard-list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        if (!model.containsAttribute("giftCard")) {
            GiftCard card = new GiftCard();
            card.setStatus(GiftCardStatus.ACTIVE);
            card.setInitialValue(new BigDecimal("0.00"));
            card.setBalance(new BigDecimal("0.00"));
            model.addAttribute("giftCard", card);
        }
        model.addAttribute("giftCardStatuses", Arrays.asList(GiftCardStatus.values()));
        return "admin/referral_rewards/giftcard-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute GiftCard giftCard, RedirectAttributes redirectAttributes) {
        try {
            giftCardRepository.save(giftCard);
            redirectAttributes.addFlashAttribute("successMessage", "Gift card saved successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gift card could not be saved (duplicate code or constraint error).");
            redirectAttributes.addFlashAttribute("giftCard", giftCard);
            return "redirect:/giftcard/create";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while saving gift card: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("giftCard", giftCard);
            return "redirect:/giftcard/create";
        }
        return "redirect:/giftcard/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<GiftCard> giftCard = giftCardRepository.findById(id);
        if (giftCard.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gift card not found.");
            return "redirect:/giftcard/list";
        }
        model.addAttribute("giftCard", giftCard.get());
        model.addAttribute("giftCardStatuses", Arrays.asList(GiftCardStatus.values()));
        return "admin/referral_rewards/giftcard-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!giftCardRepository.existsById(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Gift card not found.");
                return "redirect:/giftcard/list";
            }
            giftCardRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Gift card deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete gift card because it is linked to transactions.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while deleting gift card: " + ex.getMessage());
        }
        return "redirect:/giftcard/list";
    }
}

