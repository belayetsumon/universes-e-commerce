/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author User
 */
@Controller
@RequestMapping("/customer-billingaddress")
public class BillingAddressController {

   @RequestMapping(value = {"", "/", "/index", "dashboards"})
    public String index(Model model) {
        model.addAttribute("attribute", "value");
        return "view.name";
    }

}
