package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.repository.CustomerNotifications;
import com.ecommerce.app.module.ReferralRewards.repository.OrderIncentiveUsageRepository;
import com.ecommerce.app.module.ReferralRewards.repository.PromotionFraudFlagRepository;
import com.ecommerce.app.module.ReferralRewards.services.PromotionIncentiveReversalService;
import com.ecommerce.app.module.ReferralRewards.services.PromotionReportingService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionAdminController {

    private final OrderIncentiveUsageRepository orderIncentiveUsageRepository;
    private final CustomerNotifications customerNotificationsRepository;
    private final PromotionFraudFlagRepository promotionFraudFlagRepository;
    private final PromotionReportingService promotionReportingService;
    private final PromotionIncentiveReversalService promotionIncentiveReversalService;

    public PromotionAdminController(
            OrderIncentiveUsageRepository orderIncentiveUsageRepository,
            CustomerNotifications customerNotificationsRepository,
            PromotionFraudFlagRepository promotionFraudFlagRepository,
            PromotionReportingService promotionReportingService,
            PromotionIncentiveReversalService promotionIncentiveReversalService) {
        this.orderIncentiveUsageRepository = orderIncentiveUsageRepository;
        this.customerNotificationsRepository = customerNotificationsRepository;
        this.promotionFraudFlagRepository = promotionFraudFlagRepository;
        this.promotionReportingService = promotionReportingService;
        this.promotionIncentiveReversalService = promotionIncentiveReversalService;
    }

    @GetMapping("/order-incentives")
    public String orderIncentives(Model model) {
        model.addAttribute("usages", orderIncentiveUsageRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "admin/referral_rewards/order-incentive-usage-list";
    }

    @PostMapping("/order-incentives/{id}/reverse")
    public String reverseOrderIncentives(@PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        try {
            promotionIncentiveReversalService.reverseOrderIncentives(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Order incentives reversed successfully.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not reverse order incentives: " + ex.getMessage());
        }
        return "redirect:/admin/promotions/order-incentives";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("notifications", customerNotificationsRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "admin/referral_rewards/notification-log-list";
    }

    @GetMapping("/fraud-flags")
    public String fraudFlags(Model model) {
        model.addAttribute("flags", promotionFraudFlagRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
        return "admin/referral_rewards/fraud-flag-list";
    }

    @PostMapping("/fraud-flags/{id}/review")
    public String reviewFraudFlag(@PathVariable Long id,
            @RequestParam(defaultValue = "REVIEWED") String status,
            @RequestParam(required = false) String reviewedBy,
            RedirectAttributes redirectAttributes) {
        try {
            promotionFraudFlagRepository.findById(id).ifPresent(flag -> {
                flag.setStatus(status == null || status.isBlank() ? "REVIEWED" : status.trim().toUpperCase());
                flag.setReviewedBy(reviewedBy == null || reviewedBy.isBlank() ? "admin" : reviewedBy.trim());
                flag.setReviewedAt(LocalDateTime.now());
                promotionFraudFlagRepository.save(flag);
            });
            redirectAttributes.addFlashAttribute("successMessage", "Fraud flag updated.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not update fraud flag: " + ex.getMessage());
        }
        return "redirect:/admin/promotions/fraud-flags";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("summary", promotionReportingService.currentLiabilitySummary());
        return "admin/referral_rewards/promotion-report-dashboard";
    }
}
