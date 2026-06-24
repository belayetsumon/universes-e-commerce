package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.ShippingRule;
import com.ecommerce.app.module.shipping.model.ShippingRuleAction;
import com.ecommerce.app.module.shipping.services.ShippingLocationService;
import com.ecommerce.app.module.shipping.services.ShippingRuleService;
import com.ecommerce.app.vendor.services.VendorprofileService;
import jakarta.validation.Valid;
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
@RequestMapping("/admin/shipping-rules")
public class ShippingRuleController {

    private final ShippingRuleService service;
    private final VendorprofileService vendorprofileService;
    private final ShippingLocationService locationService;

    public ShippingRuleController(ShippingRuleService service, VendorprofileService vendorprofileService,
            ShippingLocationService locationService) {
        this.service = service;
        this.vendorprofileService = vendorprofileService;
        this.locationService = locationService;
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("rules", service.getAll());
        return "admin/shipping/rules/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("rule", new ShippingRule());
        populate(model);
        return "admin/shipping/rules/form";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        ShippingRule rule = service.getById(id);
        if (rule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shipping rule not found.");
            return "redirect:/admin/shipping-rules/list";
        }
        model.addAttribute("rule", rule);
        populate(model);
        return "admin/shipping/rules/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("rule") ShippingRule rule,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populate(model);
            return "admin/shipping/rules/form";
        }
        service.save(rule);
        redirectAttributes.addFlashAttribute("successMessage", "Shipping rule saved successfully.");
        return "redirect:/admin/shipping-rules/list";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Shipping rule deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete shipping rule.");
        }
        return "redirect:/admin/shipping-rules/list";
    }

    private void populate(Model model) {
        model.addAttribute("vendors", vendorprofileService.findAll());
        model.addAttribute("districts", locationService.getActiveLocations());
        model.addAttribute("actions", ShippingRuleAction.values());
    }
}
