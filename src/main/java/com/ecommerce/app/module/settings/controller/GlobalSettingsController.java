/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.settings.controller;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/admin/settings")
//@PreAuthorize("hasAuthority('admin')")
public class GlobalSettingsController {

    @Autowired
    private GlobalSettingsService globalSettingsService;

    @GetMapping
    public String settingsPage(Model model) {
        model.addAttribute("settings", globalSettingsService.getActiveSettings());
        return "admin/settings/global-settings";
    }

    @PostMapping("/update")
    public String updateSettings(
            @Valid @ModelAttribute("settings") GlobalSettings settings,
            BindingResult bindingResult,
            @RequestParam(name = "siteLogoFile", required = false) MultipartFile siteLogoFile,
            @RequestParam(name = "faviconFile", required = false) MultipartFile faviconFile,
            @RequestParam(name = "ogImageFile", required = false) MultipartFile ogImageFile,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/settings/global-settings";
        }

        try {
            globalSettingsService.updateSettings(settings, siteLogoFile, faviconFile, ogImageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Global settings updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update settings: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    @PostMapping("/images/delete")
    public String deleteImage(
            @RequestParam("type") GlobalSettingsService.SettingsImageType imageType,
            RedirectAttributes redirectAttributes
    ) {
        try {
            globalSettingsService.deleteImage(imageType);
            redirectAttributes.addFlashAttribute("successMessage", imageType.getLabel() + " deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete image: " + e.getMessage());
        }

        return "redirect:/admin/settings#images";
    }

    // Consolidated single-section update methods
    @PostMapping("/basic")
    public String updateBasic(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.BASIC, "Basic", redirectAttributes);
    }

    @PostMapping("/seo")
    public String updateSeo(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.SEO, "SEO", redirectAttributes);
    }

    @PostMapping("/store")
    public String updateStore(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.STORE, "Store", redirectAttributes);
    }

    @PostMapping("/payment")
    public String updatePayment(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.PAYMENT, "Payment", redirectAttributes);
    }

    @PostMapping("/delivery")
    public String updateDelivery(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.DELIVERY, "Delivery", redirectAttributes);
    }

    @PostMapping("/order")
    public String updateOrder(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.ORDER, "Order", redirectAttributes);
    }

    @PostMapping("/social")
    public String updateSocial(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.SOCIAL, "Social", redirectAttributes);
    }

    @PostMapping("/policy")
    public String updatePolicy(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.POLICY, "Policy", redirectAttributes);
    }

    @PostMapping("/maintenance")
    public String updateMaintenance(@ModelAttribute GlobalSettings settings, RedirectAttributes redirectAttributes) {
        return updateSettingsSection(settings, GlobalSettingsService.SettingsSection.MAINTENANCE, "Maintenance", redirectAttributes);
    }

    // Helper method to eliminate duplicate code
    private String updateSettingsSection(GlobalSettings settings,
            GlobalSettingsService.SettingsSection section,
            String sectionName,
            RedirectAttributes redirectAttributes) {
        try {
            globalSettingsService.updateSettingsField(settings, section);
            redirectAttributes.addFlashAttribute("successMessage", sectionName + " settings updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update " + sectionName.toLowerCase() + " settings: " + e.getMessage());
        }
        return "redirect:/admin/settings#" + sectionName.toLowerCase();
    }
}
