/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.system.controller;

import com.ecommerce.app.module.system.services.EndpointScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/admin/system")
public class SystemEndpointController {

    @Autowired
    private EndpointScannerService endpointScannerService;

    @GetMapping("/endpoints")
    public String endpoints(Model model) {
        model.addAttribute("endpoints", endpointScannerService.getAllEndpoints());
        return "admin/system/endpoints";
    }

}
