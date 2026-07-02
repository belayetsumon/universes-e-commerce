package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.CashbackPolicy;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CashbackPolicyStatus;
import com.ecommerce.app.module.ReferralRewards.repository.CashbackPolicyRepository;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
    private final ProductcategoryRepository productcategoryRepository;

    public CashbackPolicyController(CashbackPolicyRepository cashbackPolicyRepository,
                                    ProductcategoryRepository productcategoryRepository) {
        this.cashbackPolicyRepository = cashbackPolicyRepository;
        this.productcategoryRepository = productcategoryRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("policies", cashbackPolicyRepository.findAllForAdminList());
            addCategoryLookup(model);
        } catch (RuntimeException ex) {
            model.addAttribute("policies", Collections.emptyList());
            model.addAttribute("cashbackCategoryNamesById", Collections.emptyMap());
            model.addAttribute("errorMessage", "Runtime error while loading cashback policies: " + ex.getMessage());
        }
        return "admin/referral_rewards/cashback-policy-list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        if (!model.containsAttribute("policy")) {
            CashbackPolicy policy = new CashbackPolicy();
            LocalDateTime now = LocalDateTime.now();
            policy.setStatus(CashbackPolicyStatus.ACTIVE);
            policy.setStartDate(now);
            policy.setEndDate(now.plusDays(30));
            policy.setCurrency("BDT");
            model.addAttribute("policy", policy);
        }
        addFormOptions(model);
        return "admin/referral_rewards/cashback-policy-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("policy") CashbackPolicy policy,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        normalizeCategorySelection(policy);
        normalizeCurrency(policy);
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "admin/referral_rewards/cashback-policy-form";
        }

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
        Optional<CashbackPolicy> policy = cashbackPolicyRepository.findByIdWithCategories(id);
        if (policy.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Policy not found.");
            return "redirect:/cashback-policy/list";
        }
        model.addAttribute("policy", policy.get());
        addFormOptions(model);
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

    private void addFormOptions(Model model) {
        List<Productcategory> categories = productcategoryRepository.findAll();
        model.addAttribute("policyStatuses", Arrays.asList(CashbackPolicyStatus.values()));
        model.addAttribute("cashbackCategories", categories);
        model.addAttribute("cashbackCategoryNamesById", buildCategoryNameLookup(categories));
    }

    private void normalizeCategorySelection(CashbackPolicy policy) {
        Set<Long> selectedCategoryIds = policy.getCategoryIds();
        if (selectedCategoryIds == null || selectedCategoryIds.isEmpty()) {
            policy.setCategoryIds(new LinkedHashSet<>());
            return;
        }
        selectedCategoryIds.removeIf(java.util.Objects::isNull);
        policy.setCategoryIds(new LinkedHashSet<>(selectedCategoryIds));
    }

    private void normalizeCurrency(CashbackPolicy policy) {
        if (policy.getCurrency() != null) {
            policy.setCurrency(policy.getCurrency().trim().toUpperCase());
        }
    }

    private void addCategoryLookup(Model model) {
        model.addAttribute("cashbackCategoryNamesById", buildCategoryNameLookup(productcategoryRepository.findAll()));
    }

    private Map<Long, String> buildCategoryNameLookup(List<Productcategory> categories) {
        return categories.stream()
                .collect(Collectors.toMap(
                        Productcategory::getId,
                        Productcategory::getName,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
}
