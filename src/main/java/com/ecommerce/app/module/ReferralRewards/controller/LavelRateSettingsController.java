/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.LavelRateSettings;
import com.ecommerce.app.module.ReferralRewards.model.LevelEnum;
import com.ecommerce.app.module.ReferralRewards.repository.LavelRateSettingsRepository;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/lavelratesettings")
public class LavelRateSettingsController {

    @Autowired
    LavelRateSettingsRepository lavelRateSettingsRepository;

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("list", lavelRateSettingsRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
        return "admin/referral_rewards/lavel_rate_settings_list";
    }

    @GetMapping("/create")
    public String createForm(Model model, LavelRateSettings lavelRateSettings) {
        model.addAttribute("levels", LevelEnum.values());
        return "admin/referral_rewards/lavel_rate_settings_form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute LavelRateSettings lavelRateSettings,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("levels", LevelEnum.values());
            return "admin/referral_rewards/lavel_rate_settings_form";
        }

        lavelRateSettingsRepository.save(lavelRateSettings);
        redirectAttributes.addFlashAttribute("successMessage", "Saved successfully!");
        return "redirect:/lavelratesettings/list";
    }

    // Show form for edit
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<LavelRateSettings> optional = lavelRateSettingsRepository.findById(id);
        if (optional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Record not found!");
            return "redirect:/lavelratesettings/list";
        }

        model.addAttribute("lavelRateSettings", optional.get());
        model.addAttribute("levels", LevelEnum.values());
        return "admin/referral_rewards/lavel_rate_settings_form";
    }

    // Delete (POST)
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (lavelRateSettingsRepository.existsById(id)) {

            lavelRateSettingsRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage", "Deleted successfully!");

        } else {

            redirectAttributes.addFlashAttribute("errorMessage", "Record not found!");

        }
        return "redirect:/lavelratesettings/list";
    }
}
