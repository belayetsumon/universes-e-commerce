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
import com.ecommerce.app.vendor.dto.VendorDashboardDto;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.services.VendorDashboardService;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
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
    VendorprofileRepository vendorprofileRepository;

    @Autowired
    private VendorUserContext vendorUserContext;

    @Autowired
    private VendorDashboardService vendorDashboardService;

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
        VendorDashboardDto dashboard = vendorDashboardService.buildDashboard(vendorprofiles);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("vendorprofile", vendorUserContext.getActiveVendor());
        return "vendor/dashboards";
    }

}
