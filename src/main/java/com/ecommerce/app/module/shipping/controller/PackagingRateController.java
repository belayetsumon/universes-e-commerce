/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.shipping.controller;

import com.ecommerce.app.exception.UniqueConstraintViolationException;
import com.ecommerce.app.module.shipping.model.PackagingRate;
import com.ecommerce.app.module.shipping.model.PackagingType;
import com.ecommerce.app.module.shipping.services.PackagingRateService;
import com.ecommerce.app.vendor.services.VendorprofileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/packagingrates")
public class PackagingRateController {

    @Autowired
    private PackagingRateService service;

    @Autowired
    VendorprofileService vendorprofileService;

    @ModelAttribute("packagingTypes")
    public PackagingType[] populatePackagingTypes() {
        return PackagingType.values();
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("packagingRate", new PackagingRate());
        model.addAttribute("vendors", vendorprofileService.findAll());
        return "admin/shipping/packagingrate/create_package_rate";
    }

    @GetMapping("list")
    public String list(Model model) {
        try {
            model.addAttribute("rates", service.getAll());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while loading packaging rates: " + e.getMessage());
            model.addAttribute("rates", java.util.List.of());
        }
        return "admin/shipping/packagingrate/packegeratelist";
    }

    @PostMapping("/save")
    public String save(Model model, @Valid @ModelAttribute("packagingRate") PackagingRate packagingRate,
            BindingResult result,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("packagingRate", packagingRate);
            model.addAttribute("vendors", vendorprofileService.findAll());
            return "admin/shipping/packagingrate/create_package_rate";
        }
        try {
            service.save(packagingRate);
            redirect.addFlashAttribute("successMessage", "Packaging rate saved successfully!");
            return "redirect:/packagingrates/list";
        } catch (UniqueConstraintViolationException e) {
            result.rejectValue("packagingType", "error.rate", e.getMessage());
            model.addAttribute("packagingRate", packagingRate);
            model.addAttribute("vendors", vendorprofileService.findAll());
            return "admin/shipping/packagingrate/create_package_rate";
        } catch (Exception e) {
            // 2026-04-22: Show older packaging form runtime failures with the new standard message key.
            model.addAttribute("errorMessage", "Runtime error while saving packaging rate: " + e.getMessage());
            model.addAttribute("packagingRate", packagingRate);
            model.addAttribute("vendors", vendorprofileService.findAll());
            return "admin/shipping/packagingrate/create_package_rate";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, PackagingRate packagingRate) {
        try {
            model.addAttribute("packagingRate", service.getById(id));
        } catch (Exception e) {
            model.addAttribute("packagingRate", new PackagingRate());
            model.addAttribute("errorMessage", "Runtime error while loading packaging rate: " + e.getMessage());
        }
        model.addAttribute("vendors", vendorprofileService.findAll());
        return "admin/shipping/packagingrate/create_package_rate";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("successMessage", "Packaging rate deleted successfully!");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("errorMessage",
                    "Cannot delete this record because it is linked with other data.");
        } catch (Exception ex) {
            redirect.addFlashAttribute("errorMessage",
                    "Runtime error while deleting packaging rate: " + ex.getMessage());
        }
        return "redirect:/packagingrates/list";
    }
}
