/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import com.ecommerce.app.vendor.model.VendorTransactionTypeEnum;
import com.ecommerce.app.vendor.services.VendorTransactionService;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendor-transaction")
public class VendorTransactionController {

    @Autowired
    VendorTransactionService vendorTransactionService;
    @Autowired
    private VendorUserContext vendorUserContext;

    @RequestMapping("/list")
    public String list(Model model,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            @RequestParam(name = "typeStr", required = false) VendorTransactionTypeEnum typeStr,
            @RequestParam(name = "statusStr", required = false) VendorTransactionStatusEnum statusStr,
            @RequestParam(name = "salesOrderStr", required = false) String salesOrderStr,
            @RequestParam(name = "vendorId", required = false) Long vendorId
    ) {
        Long vId = vendorUserContext.getActiveVendor().getId();
        List<Map<String, Object>> transactionList = vendorTransactionService.findTransactions(vId, fromDate, toDate, statusStr, typeStr, salesOrderStr);
        model.addAttribute("transactionStatus", VendorTransactionStatusEnum.values());
        model.addAttribute("transactionType", VendorTransactionTypeEnum.values());
        model.addAttribute("list", transactionList);
        return "vendor/transaction/transaction-list";
    }

}
