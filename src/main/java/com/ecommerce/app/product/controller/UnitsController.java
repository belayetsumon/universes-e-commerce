/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/uom")
public class UnitsController {

    
    @RequestMapping(value = {"", "/", "/index"})
    public String index(Model model) {
        model.addAttribute("attribute", "value");
        return "view.name";
    }
    
    

}
