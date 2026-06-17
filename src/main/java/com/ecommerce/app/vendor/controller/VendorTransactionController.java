/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.finance.services.FinanceReportService;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendor-transaction")
public class VendorTransactionController {

    @Autowired
    private FinanceReportService financeReportService;
    @Autowired
    private VendorUserContext vendorUserContext;

    @RequestMapping("/list")
    public String list(Model model) {
        try {
            if (vendorUserContext.getActiveVendor() == null || vendorUserContext.getActiveVendor().getId() == null) {
                model.addAttribute("errorMessage", "Vendor session not found. Please select or login to your vendor account again.");
                model.addAttribute("ledgerRows", new ArrayList<>());
                return "vendor/finance/ledger";
            }

            Long vId = vendorUserContext.getActiveVendor().getId();
            model.addAttribute("ledgerRows", financeReportService.getVendorLedger(vId));
            model.addAttribute("vendorprofile", vendorUserContext.getActiveVendor());
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Runtime error while loading vendor transaction ledger: " + ex.getMessage());
            model.addAttribute("ledgerRows", new ArrayList<>());
            model.addAttribute("vendorprofile", vendorUserContext.getActiveVendor());
        }
        return "vendor/finance/ledger";
    }

}
