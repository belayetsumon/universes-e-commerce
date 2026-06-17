/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.ReferralRepository;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@RequestMapping("/referral")
public class ReferralController {

    @Autowired
    ReferralRepository referralRepository;

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("referrals", referralRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("referrals", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading referrals: " + ex.getMessage());
        }
        return "admin/referral_rewards/referrallist";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<com.ecommerce.app.module.ReferralRewards.model.Referral> optionalReferral = referralRepository.findById(id);
            if (optionalReferral.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Referral record not found.");
                return "redirect:/referral/list";
            }

            referralRepository.delete(optionalReferral.get());
            redirectAttributes.addFlashAttribute("successMessage", "Referral deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete referral because it is linked to other records.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while deleting referral: " + ex.getMessage());
        }
        return "redirect:/referral/list";
    }
}
