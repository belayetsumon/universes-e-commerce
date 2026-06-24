/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.admin.controller;

import com.ecommerce.app.admin.services.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('admin')")
public class AdminController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @RequestMapping(value = {"", "/", "/index"})
    public String page(Model model) {
        model.addAttribute("pageTitle", "Admin Dashboard");
        try {
            model.addAttribute("dashboard", adminDashboardService.buildDashboard());
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", "Runtime error while loading admin dashboard: " + ex.getMessage());
            model.addAttribute("dashboard", adminDashboardService.emptyDashboard());
        }
        return "/admin/index";
    }

    @RequestMapping(value = {"/returns-refunds", "/return-refund", "/return-refunds", "/returns"})
    public String returnsRefunds() {
        return "redirect:/admin/finance/returns";
    }

    @RequestMapping(value = {"/refunds"})
    public String refunds() {
        return "redirect:/admin/finance/refunds";
    }

}
