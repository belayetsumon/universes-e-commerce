/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.adminvendor.controller;

import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.VendorPayoutStatusEnum;
import com.ecommerce.app.vendor.repository.VendorPayoutRepository;
import com.ecommerce.app.vendor.services.VendorFinanceService;
import com.ecommerce.app.vendor.services.VendorPayoutService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/admin/payouts")
public class AdminVendorPayoutController {

    @Autowired
    private VendorPayoutRepository payoutRepo;
    @Autowired
    private VendorFinanceService vendorFinanceService;

    @Autowired
    VendorPayoutService vendorPayoutService;

    @Autowired
    VendorPayoutRepository vendorPayoutRepository;

    @GetMapping("/list")
    public String list(Model model,
            @RequestParam(name = "vendorCode", required = false) String vendorCode,
            @RequestParam(name = "status", required = false) VendorPayoutStatusEnum status,
            @RequestParam(name = "requestedFrom", required = false) String requestedFrom,
            @RequestParam(name = "requestedTo", required = false) String requestedTo
    ) {
        List<Map<String, Object>> payouts = vendorPayoutService.AllVendorPayouts(
                vendorCode,
                status,
                requestedFrom,
                requestedTo
        );
        model.addAttribute("status", VendorPayoutStatusEnum.values());
        model.addAttribute("list", payouts);
        return "admin/vendor/payout/payout-list";
    }

    @PostMapping("/process")
    public String approvePayout(
            @RequestParam(name = "id", required = false) Long payoutId,
            @RequestParam(name = "ref", required = false) String ref,
            @RequestParam(name = "note", required = false) String note,
            @RequestParam(name = "statusStr", required = false) String statusStr,
            RedirectAttributes redirectAttributes
    ) {
        Optional<VendorPayout> optionalPayout = vendorPayoutRepository.findById(payoutId);

        if (optionalPayout.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Payout not found!");
            return "redirect:/admin/payouts/list";
        }

        VendorPayout payout = optionalPayout.get();

        if (payout.getStatus() != VendorPayoutStatusEnum.REQUESTED) {
            redirectAttributes.addFlashAttribute("message", "Only requested payouts can be Process!");
            return "redirect:/admin/payouts/list";
        }

        // ✅ Set payout details
        if ("PROCESSING".equalsIgnoreCase(statusStr)) {

            payout.setAdminNote(note);
            payout.setProcessedAt(LocalDateTime.now());
            payout.setStatus(VendorPayoutStatusEnum.PROCESSING);
            vendorPayoutRepository.save(payout);
        } else if ("PAID".equalsIgnoreCase(statusStr)) {
            payout.setPayoutReference(ref);
            payout.setAdminNote(note);
            payout.setPaidAt(LocalDateTime.now());
            payout.setStatus(VendorPayoutStatusEnum.PAID);
            vendorPayoutRepository.save(payout);

        } else if ("CANCELLED".equalsIgnoreCase(statusStr)) {
            payout.setAdminNote(note);
            payout.setProcessedAt(LocalDateTime.now());
            payout.setStatus(VendorPayoutStatusEnum.CANCELLED);
            vendorPayoutRepository.save(payout);
        } else {
            throw new IllegalArgumentException("Invalid action: " + statusStr);
        }
        redirectAttributes.addFlashAttribute("message", "Payout approved successfully!");
        return "redirect:/admin/payouts/list";
    }

}
