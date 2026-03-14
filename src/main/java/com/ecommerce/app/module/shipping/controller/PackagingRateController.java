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

    // Supplies PackagingType enum values
    @ModelAttribute("packagingTypes")
    public PackagingType[] populatePackagingTypes() {
        return PackagingType.values();
    }

    /**
     * CREATE: Shows the form for a new rate.
     */
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("packagingRate", new PackagingRate());
        model.addAttribute("vendors", vendorprofileService.findAll());
        return "admin/shipping/packagingrate/create_package_rate";
    }

    /**
     * CREATE/UPDATE: Saves the rate.
     */
    @GetMapping("list")
    public String list(Model model) {
        model.addAttribute("rates", service.getAll());
        return "admin/shipping/packagingrate/packegeratelist";
    }

    @PostMapping("/save")
    public String save(Model model, @Valid @ModelAttribute("packagingRate") PackagingRate packagingRate,
            BindingResult result,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("packagingRate", new PackagingRate());
            model.addAttribute("vendors", vendorprofileService.findAll());
            return "admin/shipping/packagingrate/create_package_rate";
        }
        try {
            service.save(packagingRate);
            redirect.addFlashAttribute("success", "Saved successfully!");
            return "redirect:/packagingrates/list";
        } catch (UniqueConstraintViolationException e) {
            result.rejectValue("packagingType", "error.rate", e.getMessage());

            model.addAttribute("packagingRate", new PackagingRate());
            model.addAttribute("vendors", vendorprofileService.findAll());

            return "admin/shipping/packagingrate/create_package_rate";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, PackagingRate packagingRate) {
        model.addAttribute("packagingRate", service.getById(id));
        model.addAttribute("vendors", vendorprofileService.findAll());
        return "admin/shipping/packagingrate/create_package_rate";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.delete(id);  // will attempt delete
            redirect.addFlashAttribute("success", "Deleted successfully!");
        } catch (DataIntegrityViolationException ex) {
            // Handle foreign key or integrity constraint violation
            redirect.addFlashAttribute("error",
                    "Cannot delete this record because it is linked with other data.");
        } catch (Exception ex) {
            // Catch other unexpected exceptions
            redirect.addFlashAttribute("error",
                    "An error occurred while deleting the record.");
        }
        return "redirect:/packagingrates/list";
    }
}
