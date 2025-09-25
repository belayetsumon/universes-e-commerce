/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.module.cart.controller;

import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.LoggedUserService;
import com.ecommerce.app.order.model.BillingAddress;
import com.ecommerce.app.order.model.ShippingAddress;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/cart_address")
public class CartAddressController {

    @Autowired
    LoggedUserService loggedUserService;

    @Autowired
    UsersRepository usersRepository;

    @RequestMapping("/add_billing_address")
    public String addBillingAddress(Model model, HttpSession session, BillingAddress billingAddress,
            @RequestParam(name = "sameAddress", required = false) Boolean sameAddress
    ) {

        session.setAttribute("session_Billing_address", billingAddress);

        ShippingAddress shippingAddress = new ShippingAddress();

        if (Boolean.TRUE.equals(sameAddress)) {

            shippingAddress.setFirstName(billingAddress.getFirstName());
            shippingAddress.setLastName(billingAddress.getLastName());
            shippingAddress.setEmail(billingAddress.getEmail());
            shippingAddress.setMobile(billingAddress.getMobile());
            shippingAddress.setCompany(billingAddress.getCompany());
            shippingAddress.setCountry(billingAddress.getCountry());
            shippingAddress.setDistrict(billingAddress.getDistrict());
            shippingAddress.setAddressLineOne(billingAddress.getAddressLineOne());
            shippingAddress.setAddressLinetwo(billingAddress.getAddressLinetwo());
            shippingAddress.setCity(billingAddress.getCity());
            shippingAddress.setPostCode(billingAddress.getPostCode());
            session.setAttribute("session_Shipping_address", shippingAddress);
        }

        return "redirect:/order/create";
    }

    @RequestMapping("/add_shipping_address")
    public String addShippingAddress(Model model, HttpSession session, ShippingAddress shippingAddress) {

        session.setAttribute("session_Shipping_address", shippingAddress);

        return "redirect:/order/create";
    }

}
