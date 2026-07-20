/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.module.order.model.PaymentMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customer_payment")
public class CustomerPaymentController {

    @RequestMapping("/payment_method")
    public String index(Model model,
            @RequestParam(name = "orderId", required = false) Long orderId,
            RedirectAttributes redirectAttributes) {
        if (orderId != null) {
            return "redirect:/customerorder/payment/" + orderId;
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Please choose an order before paying.");
        return "redirect:/customerorder/index";
    }

}
