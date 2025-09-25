/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.module.user.services.LoggedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.vendor.model.VendorTransactionStatusEnum;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.services.VendorFinanceService;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Optional;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/vendor")
//@PreAuthorize("hasAuthority('instructor')")
public class VendorController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    SalesOrderRepository salesOrderRepository;

    @Autowired
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    private VendorUserContext vendorUserContext;

    @Autowired
    VendorFinanceService vendorFinanceService;

    @RequestMapping(value = {"/home"})
    public String home() {
        Long id = vendorUserContext.getActiveVendor().getId();
        return "redirect:/vendor/index/" + id;
    }

    @RequestMapping(value = {"/{id}", "/{id}", "/index/{id}", "/dashboards/{id}"})
    public String index(Model model, @PathVariable Long id) {
        vendorUserContext.setActiveVendor(null);
        Optional<Vendorprofile> vendorprofile = vendorprofileRepository.findById(id);
        Vendorprofile vendorprofiles = vendorprofile.get();
        vendorUserContext.setActiveVendor(vendorprofiles);
        EnumMap<VendorTransactionStatusEnum, BigDecimal> balance = vendorFinanceService
                .getVendorBalance(id);

        model.addAttribute("pending", balance.get(VendorTransactionStatusEnum.PENDING));
        model.addAttribute("available", balance.get(VendorTransactionStatusEnum.AVAILABLE));
        model.addAttribute("paid", balance.get(VendorTransactionStatusEnum.PAID));
        model.addAttribute("vendorprofile", vendorUserContext.getActiveVendor());

        return "vendor/dashboards";
    }

}
