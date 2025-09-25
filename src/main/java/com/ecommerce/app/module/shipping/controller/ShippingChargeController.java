/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.module.shipping.model.ShippingChargeRule;
import com.ecommerce.app.module.shipping.model.ShippingZoneTypeEnum;
import com.ecommerce.app.module.shipping.repository.ShippingChargeRuleRepository;
import com.ecommerce.app.module.shipping.services.ShippingChargeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/shippingcharge")
public class ShippingChargeController {

    @Autowired
    ShippingChargeService shippingChargeService;

    @Autowired
    private ShippingChargeRuleRepository repository;

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("list", repository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "shipping/shippingchargelist";
    }

    @GetMapping("/create")
    public String createForm(Model model, ShippingChargeRule shippingChargeRule) {

        model.addAttribute("zoneTypes", ShippingZoneTypeEnum.values());
        return "shipping/shipping_rule_form";
    }

    @PostMapping("/save")
    public String save(Model model, @Valid ShippingChargeRule rule, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {

            model.addAttribute("zoneTypes", ShippingZoneTypeEnum.values());
            return "shipping/shipping_rule_form";
        }
        repository.save(rule);
        redirectAttributes.addFlashAttribute("successMessage", "Shipping rule saved successfully!");
        return "redirect:/shippingcharge/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, ShippingChargeRule shippingChargeRule) {
        model.addAttribute("shippingChargeRule", repository.findById(id).orElseThrow());
        model.addAttribute("zoneTypes", ShippingZoneTypeEnum.values());
        return "shipping/shipping_rule_form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        repository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Shipping rule deleted successfully!");
        return "redirect:/shippingcharge/list";
    }

}
