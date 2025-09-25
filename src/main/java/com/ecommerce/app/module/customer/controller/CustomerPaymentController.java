/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.customer.controller;

import com.ecommerce.app.order.model.PaymentMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/customer_payment")
public class CustomerPaymentController {

    @RequestMapping("/payment_method")
    public String index(Model model) {
        model.addAttribute("methods", PaymentMethod.values());
        return "customer/order/payment_method";
    }

}
