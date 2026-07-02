/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.controller;

import com.ecommerce.app.module.ReferralRewards.model.CashOutRequest;
import com.ecommerce.app.module.ReferralRewards.enumvalue.CashOutStatus;
import com.ecommerce.app.module.ReferralRewards.model.TransactionType;
import com.ecommerce.app.module.ReferralRewards.repository.CashOutRequestRepository;
import com.ecommerce.app.module.ReferralRewards.services.PromotionNotificationService;
import com.ecommerce.app.module.ReferralRewards.services.RewardAccountService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
@RequestMapping({"/admin/cashouts", "/cashoutrequest"})
public class CashOutRequestController {

    private static final BigDecimal CASHOUT_CONVERSION_RATE = new BigDecimal("0.01");

    @Autowired
    private CashOutRequestRepository cashOutRequestRepository;

    @Autowired
    private RewardAccountService rewardAccountService;

    @Autowired
    private PromotionNotificationService promotionNotificationService;

    @GetMapping({"", "/", "/list"})
    public String list(Model model) {
        model.addAttribute("cashouts", cashOutRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "requestedAt", "id")));
        return "admin/referral_rewards/admin-cashout-view";
    }

    @PostMapping("/{id}/approve")
    @Transactional
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CashOutRequest request = cashOutRequestRepository.findById(id).orElseThrow();
        if (request.getStatus() != CashOutStatus.PENDING) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only pending cashout requests can be approved.");
            return "redirect:/admin/cashouts";
        }

        request.setStatus(CashOutStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        cashOutRequestRepository.save(request);
        promotionNotificationService.recordInApp(
                request.getUser(),
                "PROMOTION_CASHOUT_APPROVED",
                "Cashout request approved.",
                "cashoutId=" + request.getId() + ", amount=" + request.getAmount()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Cashout request approved.");
        return "redirect:/admin/cashouts";
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CashOutRequest request = cashOutRequestRepository.findById(id).orElseThrow();

        if (request.getStatus() == CashOutStatus.REJECTED) {
            redirectAttributes.addFlashAttribute("errorMessage", "This cashout request is already rejected.");
            return "redirect:/admin/cashouts";
        }
        if (request.getStatus() == CashOutStatus.PAID) {
            redirectAttributes.addFlashAttribute("errorMessage", "A paid cashout request cannot be rejected.");
            return "redirect:/admin/cashouts";
        }

        BigDecimal refundedPoints = request.getAmount()
                .divide(CASHOUT_CONVERSION_RATE, 2, RoundingMode.HALF_UP);
        rewardAccountService.creditBalance(
                request.getUser(),
                refundedPoints,
                "Cashout request rejected. Request #" + request.getId(),
                TransactionType.CREDIT,
                null,
                "CASHOUT_REJECTED",
                "CASHOUT:" + request.getId(),
                null
        );

        request.setStatus(CashOutStatus.REJECTED);
        request.setProcessedAt(LocalDateTime.now());
        cashOutRequestRepository.save(request);
        promotionNotificationService.recordInApp(
                request.getUser(),
                "PROMOTION_CASHOUT_REJECTED",
                "Cashout request rejected and reward points restored.",
                "cashoutId=" + request.getId() + ", amount=" + request.getAmount()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Cashout request rejected and customer balance restored.");
        return "redirect:/admin/cashouts";
    }

    @PostMapping("/{id}/mark-paid")
    @Transactional
    public String markPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        CashOutRequest request = cashOutRequestRepository.findById(id).orElseThrow();

        if (request.getStatus() != CashOutStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only approved cashout requests can be marked paid.");
            return "redirect:/admin/cashouts";
        }

        request.setStatus(CashOutStatus.PAID);
        request.setProcessedAt(LocalDateTime.now());
        cashOutRequestRepository.save(request);
        promotionNotificationService.recordInApp(
                request.getUser(),
                "PROMOTION_CASHOUT_PAID",
                "Cashout request paid.",
                "cashoutId=" + request.getId() + ", amount=" + request.getAmount()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Cashout request marked as paid.");
        return "redirect:/admin/cashouts";
    }

}
