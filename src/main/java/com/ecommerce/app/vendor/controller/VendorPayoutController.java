/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.vendor.model.VendorPayout;
import com.ecommerce.app.vendor.model.VendorPayoutMethod;
import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorPayoutMethodRepository;
import com.ecommerce.app.vendor.repository.VendorPayoutRepository;
import com.ecommerce.app.vendor.repository.VendorTransactionRepository;
import com.ecommerce.app.vendor.services.VendorFinanceService;
import com.ecommerce.app.vendor.services.VendorPayoutMethodService;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
@RequestMapping("/vendor-payout")
public class VendorPayoutController {

    @Autowired
    private VendorFinanceService vendorFinanceService;
    @Autowired
    private VendorTransactionRepository transactionRepo;
    @Autowired
    private VendorUserContext vendorUserContext;

    @Autowired
    private VendorPayoutRepository vendorPayoutRepository;

    @Autowired
    VendorPayoutMethodRepository vendorPayoutMethodRepository;

    @Autowired
    VendorPayoutMethodService vendorPayoutMethodService;

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping("/list")
    public String list(Model model) {

        try {
            Long vId = vendorUserContext.getActiveVendor().getId();
            EnumMap<VendorTransactionStatusEnum, BigDecimal> balance = vendorFinanceService
                    .getVendorBalance(vId);

            model.addAttribute("pending", balance.get(VendorTransactionStatusEnum.PENDING));
            model.addAttribute("available", balance.get(VendorTransactionStatusEnum.AVAILABLE));
            model.addAttribute("paid", balance.get(VendorTransactionStatusEnum.PAID));
            model.addAttribute("vendorprofile", vendorUserContext.getActiveVendor());
            model.addAttribute("list", vendorPayoutRepository.findByVendor_IdOrderByIdDesc(vId));
        } catch (Exception e) {
            // 2026-04-22: Keep the older payout page stable with the same shared message keys.
            model.addAttribute("errorMessage", "Runtime error while loading payout list: " + e.getMessage());
            model.addAttribute("pending", BigDecimal.ZERO);
            model.addAttribute("available", BigDecimal.ZERO);
            model.addAttribute("paid", BigDecimal.ZERO);
            model.addAttribute("list", java.util.List.of());
        }
        return "vendor/payout/payout_list";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.read')
//            or @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @GetMapping
    public String viewBalance(Model model) {
        try {
            Long vendorId = vendorUserContext.getActiveVendor().getId();
            EnumMap<VendorTransactionStatusEnum, BigDecimal> balanceMap = vendorFinanceService.getVendorBalance(vendorId);
            model.addAttribute("balance", balanceMap);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while loading vendor balance: " + e.getMessage());
        }
        // 2026-04-22: Keep controller view path aligned with the existing payout balance template.
        return "vendor/payout/balance";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @RequestMapping("/request")
    public String showPayoutRequestPage(Model model, VendorPayout vendorPayout) {
        try {
            Vendorprofile vendor = vendorUserContext.getActiveVendor();
            Long vendorId = vendor.getId();
            vendorPayout.setVendor(vendor);
            BigDecimal available = vendorFinanceService.getVendorBalance(vendorId).get(VendorTransactionStatusEnum.AVAILABLE);
            model.addAttribute("availableBalance", available);
            Map<Long, String> method = vendorPayoutMethodService.getPayoutMethodList(vendorId);
            model.addAttribute("method", method);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Runtime error while loading payout request form: " + e.getMessage());
            model.addAttribute("availableBalance", BigDecimal.ZERO);
            model.addAttribute("method", Map.of());
        }
        return "vendor/payout/payout_form";
    }
//
//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)

    @RequestMapping("/save")
    public String save(
            @Valid VendorPayout vendorPayout,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long vendorId = vendorUserContext.getActiveVendor().getId();
        BigDecimal available = vendorFinanceService.getVendorBalance(vendorId).get(VendorTransactionStatusEnum.AVAILABLE);
        if (result.hasErrors()) {
            model.addAttribute("availableBalance", available);
            Map<Long, String> method = vendorPayoutMethodService.getPayoutMethodList(vendorId);
            model.addAttribute("method", method);
            return "vendor/payout/payout_form";
        }

        if (vendorPayout.getAmount().compareTo(available) > 0) {
            model.addAttribute("availableBalance", available);
            Map<Long, String> method = vendorPayoutMethodService.getPayoutMethodList(vendorId);
            model.addAttribute("method", method);
            result.rejectValue("amount", "error.amount", "Requested amount exceeds available amount.");
            return "vendor/payout/payout_form";
        }

        try {
            vendorPayoutRepository.save(vendorPayout);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your payout request was submitted successfully and is now pending processing."
            );
        } catch (Exception e) {
            model.addAttribute("availableBalance", available);
            Map<Long, String> method = vendorPayoutMethodService.getPayoutMethodList(vendorId);
            model.addAttribute("method", method);
            model.addAttribute("errorMessage", "Runtime error while saving payout request: " + e.getMessage());
            return "vendor/payout/payout_form";
        }

        return "redirect:/vendor-payout/list";
    }

//    @PreAuthorize("""
//            @vendorAccessAuthorityChecker.hasAuthority(authentication, 'vendor.payout.manage')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'ADMIN')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'OWNER')
//            or @vendorRoleChecker.hasVendorRole(authentication, 'VENDOR_OWNER')
//            """)
    @PostMapping("/request-payout")
    public String requestPayout2(@RequestParam VendorPayoutMethod method, RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        try {
            vendorFinanceService.requestPayout(vendor, method);
            redirectAttributes.addFlashAttribute("successMessage", "Payout request submitted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/vendor-payout";
    }

}
