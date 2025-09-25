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

    @GetMapping("/list")
    public String list(Model model) {

        Long vId = vendorUserContext.getActiveVendor().getId();
        EnumMap<VendorTransactionStatusEnum, BigDecimal> balance = vendorFinanceService
                .getVendorBalance(vId);

        model.addAttribute("pending", balance.get(VendorTransactionStatusEnum.PENDING));
        model.addAttribute("available", balance.get(VendorTransactionStatusEnum.AVAILABLE));
        model.addAttribute("paid", balance.get(VendorTransactionStatusEnum.PAID));
        model.addAttribute("vendorprofile", vendorUserContext.getActiveVendor());
        model.addAttribute("list", vendorPayoutRepository.findByVendor_IdOrderByIdDesc(vId));
        return "vendor/payout/payout_list";
    }

    @GetMapping
    public String viewBalance(Model model) {
        Long vendorId = vendorUserContext.getActiveVendor().getId();
        EnumMap<VendorTransactionStatusEnum, BigDecimal> balanceMap = vendorFinanceService.getVendorBalance(vendorId);
        model.addAttribute("balance", balanceMap);
        return "vendor/balance";
    }

    @RequestMapping("/request")
    public String showPayoutRequestPage(Model model, VendorPayout vendorPayout) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        Long vendorId = vendor.getId();
        vendorPayout.setVendor(vendor);
        BigDecimal available = transactionRepo.sumAvailableAmount(vendorId);
        model.addAttribute("availableBalance", available);
        Map<Long, String> method = vendorPayoutMethodService.getPayoutMethodList(vendorId);
        model.addAttribute("method", method);
        return "vendor/payout/payout_form";
    }

    @RequestMapping("/save")
    public String save(
            @Valid VendorPayout vendorPayout,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long vendorId = vendorUserContext.getActiveVendor().getId();
        BigDecimal available = transactionRepo.sumAvailableAmount(vendorId);
        // Always check BindingResult immediately
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
//            prepareForm(model, vendorId);
            return "vendor/payout/payout_form";
        }

        vendorPayoutRepository.save(vendorPayout);

        redirectAttributes.addFlashAttribute(
                "message",
                "Your payout request was submitted successfully and is now pending processing."
        );

        return "redirect:/vendor-payout/list";
    }

//    @PostMapping("/request")
//    public String requestPayout(@RequestParam VendorPayoutMethod method, RedirectAttributes redirectAttributes) {
//        Vendorprofile vendor = vendorUserContext.getActiveVendor();
//        try {
//            vendorFinanceService.requestPayout(vendor, method);
//            redirectAttributes.addFlashAttribute("message", "Payout requested successfully!");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/vendor/payout/request";
//    }
    @PostMapping("/request-payout")
    public String requestPayout2(@RequestParam VendorPayoutMethod method, RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = vendorUserContext.getActiveVendor();
        try {
            vendorFinanceService.requestPayout(vendor, method);
            redirectAttributes.addFlashAttribute("message", "Payout request submitted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/balance";
    }

}
