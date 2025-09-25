/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.order.controller;

import com.ecommerce.app.order.repository.PackagingCostRepository;
import com.ecommerce.app.order.services.PackagingCostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/packagingcost")
public class PackagingCostController {

    @Autowired
    PackagingCostService packagingCostService;

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("list", packagingCostService.allPackagingCost());
        return "admin/customer/packagingcostlist";
    }

}
