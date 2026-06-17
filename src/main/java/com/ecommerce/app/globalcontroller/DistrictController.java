/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.globalcontroller;

import com.ecommerce.app.globalServices.District;
import com.ecommerce.app.module.cart.model.CartItem;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/district")
public class DistrictController {

    @GetMapping("/select-district")
    public String districtPage(Model model) {

        model.addAttribute("districts", District.values());
        return "/district/select-district"; // Name of Thymeleaf HTML file
    }

    @PostMapping("/save-district")
    @ResponseBody
    public String saveDistrict(@RequestParam String districtName, HttpSession session) {
        try {
            District district = District.valueOf(districtName);
            session.setAttribute("shippingdistrict", district);
            clearShippingSelections(session);
            return "success";
        } catch (IllegalArgumentException e) {
            return "error";
        }
    }

    private void clearShippingSelections(HttpSession session) {
        session.removeAttribute("shippingCosts");

        List<CartItem> cart = (List<CartItem>) session.getAttribute("sessioncart");
        if (cart == null || cart.isEmpty()) {
            return;
        }

        cart.stream()
                .map(CartItem::getVendorId)
                .distinct()
                .forEach(vendorId -> {
                    session.removeAttribute("shippingCost_" + vendorId);
                    session.removeAttribute("shippingOption_" + vendorId);
                });

        cart.stream()
                .map(CartItem::getVendorUuid)
                .filter(vendorUuid -> vendorUuid != null && !vendorUuid.isBlank())
                .distinct()
                .forEach(vendorUuid -> {
                    session.removeAttribute("shippingCost_" + vendorUuid);
                    session.removeAttribute("shippingOption_" + vendorUuid);
                });
    }

}
