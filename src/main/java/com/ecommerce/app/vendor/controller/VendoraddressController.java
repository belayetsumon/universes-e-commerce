/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.vendor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/vendoraddress")
public class VendoraddressController {

    @RequestMapping(value = {"", "/", "/index"})
    public String page(Model model) {
        model.addAttribute("attribute", "value");
        return "view.name";
    }

}
