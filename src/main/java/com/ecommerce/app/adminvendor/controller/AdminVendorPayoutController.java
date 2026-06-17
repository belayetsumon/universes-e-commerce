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
        model.addAttribute("status", VendorPayoutStatusEnum.values());
        try {
            List<Map<String, Object>> payouts = vendorPayoutService.AllVendorPayouts(
                    vendorCode,
                    status,
                    requestedFrom,
                    requestedTo
            );
            model.addAttribute("list", payouts);
        } catch (Exception e) {
            // 2026-04-22: Keep legacy payout reporting aligned with newer finance error messaging.
            model.addAttribute("errorMessage", "Runtime error while loading vendor payout requests: " + e.getMessage());
            model.addAttribute("list", List.of());
        }
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
        try {
            Optional<VendorPayout> optionalPayout = vendorPayoutRepository.findById(payoutId);

            if (optionalPayout.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Payout not found!");
                return "redirect:/admin/payouts/list";
            }

            VendorPayout payout = optionalPayout.get();

            if (payout.getStatus() != VendorPayoutStatusEnum.REQUESTED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Only requested payouts can be processed.");
                return "redirect:/admin/payouts/list";
            }

            // 2026-04-22: Standardize admin payout processing messages with the newer finance pages.
            if ("PROCESSING".equalsIgnoreCase(statusStr)) {
                vendorFinanceService.approvePayout(payoutId, ref, note);
            } else if ("PAID".equalsIgnoreCase(statusStr)) {
                vendorFinanceService.PaymentSent(payoutId, ref, note);
            } else if ("CANCELLED".equalsIgnoreCase(statusStr)) {
                vendorFinanceService.cancelPayout(payoutId, note);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid payout status: " + statusStr);
                return "redirect:/admin/payouts/list";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Payout processed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Runtime error while processing payout: " + e.getMessage());
        }
        return "redirect:/admin/payouts/list";
    }

}
