package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.Coupon;
import com.ecommerce.app.module.ReferralRewards.model.CouponStatus;
import com.ecommerce.app.module.ReferralRewards.model.CouponType;
import com.ecommerce.app.module.ReferralRewards.repository.CouponRepository;
import java.time.LocalDateTime;
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
@RequestMapping("/coupon")
public class CouponController {

    private final CouponRepository couponRepository;

    public CouponController(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("coupons", couponRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("coupons", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading coupons: " + ex.getMessage());
        }
        return "admin/referral_rewards/coupon-list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        if (!model.containsAttribute("coupon")) {
            Coupon coupon = new Coupon();
            coupon.setStatus(CouponStatus.ACTIVE);
            coupon.setType(CouponType.FIXED);
            coupon.setExpiryDate(LocalDateTime.now().plusDays(30));
            model.addAttribute("coupon", coupon);
        }
        model.addAttribute("couponTypes", Arrays.asList(CouponType.values()));
        model.addAttribute("couponStatuses", Arrays.asList(CouponStatus.values()));
        return "admin/referral_rewards/coupon-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Coupon coupon, RedirectAttributes redirectAttributes) {
        try {
            couponRepository.save(coupon);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon saved successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Coupon code must be unique.");
            redirectAttributes.addFlashAttribute("coupon", coupon);
            return "redirect:/coupon/create";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while saving coupon: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("coupon", coupon);
            return "redirect:/coupon/create";
        }
        return "redirect:/coupon/list";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Coupon> coupon = couponRepository.findById(id);
        if (coupon.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Coupon not found.");
            return "redirect:/coupon/list";
        }
        model.addAttribute("coupon", coupon.get());
        model.addAttribute("couponTypes", Arrays.asList(CouponType.values()));
        model.addAttribute("couponStatuses", Arrays.asList(CouponStatus.values()));
        return "admin/referral_rewards/coupon-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!couponRepository.existsById(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Coupon not found.");
                return "redirect:/coupon/list";
            }
            couponRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Coupon deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete coupon because it is linked to redemptions.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while deleting coupon: " + ex.getMessage());
        }
        return "redirect:/coupon/list";
    }
}

