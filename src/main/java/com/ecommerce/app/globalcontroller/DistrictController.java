/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.globalcontroller;

import com.ecommerce.app.globalServices.District;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
            return "success";
        } catch (IllegalArgumentException e) {
            return "error";
        }
    }

}
