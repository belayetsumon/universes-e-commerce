package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.CouponRedemptionRepository;
import java.util.Collections;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/coupon-redemption")
public class CouponRedemptionController {

    private final CouponRedemptionRepository couponRedemptionRepository;

    public CouponRedemptionController(CouponRedemptionRepository couponRedemptionRepository) {
        this.couponRedemptionRepository = couponRedemptionRepository;
    }

    @GetMapping("/list")
    public String list(Model model) {
        try {
            model.addAttribute("redemptions", couponRedemptionRepository.findAllForAdminList());
        } catch (RuntimeException ex) {
            model.addAttribute("redemptions", Collections.emptyList());
            model.addAttribute("errorMessage", "Runtime error while loading coupon redemptions: " + ex.getMessage());
        }
        return "admin/referral_rewards/coupon-redemption-list";
    }
}

