package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicy;
import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicyStatus;
import com.ecommerce.app.module.ReferralRewards.repository.CashbackPolicyRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cashback-policy")
public class CashbackPolicyController {

    private final CashbackPolicyRepository cashbackPolicyRepository;

    public CashbackPolicyController(CashbackPolicyRepository cashbackPolicyRepository) {
        this.cashbackPolicyRepository = cashbackPolicyRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("policies", cashbackPolicyRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("policies", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading cashback policies: " + ex.getMessage());
        }
        return "admin/referral_rewards/cashback-policy-list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        if (!model.containsAttribute("policy")) {
            CashbackPolicy policy = new CashbackPolicy();
            policy.setStatus(CashbackPolicyStatus.ACTIVE);
            policy.setStartDate(LocalDateTime.now().minusDays(1));
            policy.setEndDate(LocalDateTime.now().plusDays(30));
            model.addAttribute("policy", policy);
        }
        model.addAttribute("policyStatuses", Arrays.asList(CashbackPolicyStatus.values()));
        return "admin/referral_rewards/cashback-policy-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute CashbackPolicy policy, RedirectAttributes redirectAttributes) {
        try {
            cashbackPolicyRepository.save(policy);
            redirectAttributes.addFlashAttribute("successMessage", "Cashback policy saved successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while saving policy: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("policy", policy);
            return "redirect:/cashback-policy/create";
        }
        return "redirect:/cashback-policy/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CashbackPolicy> policy = cashbackPolicyRepository.findById(id);
        if (policy.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Policy not found.");
            return "redirect:/cashback-policy/list";
        }
        model.addAttribute("policy", policy.get());
        model.addAttribute("policyStatuses", Arrays.asList(CashbackPolicyStatus.values()));
        return "admin/referral_rewards/cashback-policy-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!cashbackPolicyRepository.existsById(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Policy not found.");
                return "redirect:/cashback-policy/list";
            }
            cashbackPolicyRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Policy deleted successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while deleting policy: " + ex.getMessage());
        }
        return "redirect:/cashback-policy/list";
    }
}

