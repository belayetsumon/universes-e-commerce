/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.globalServices.EmailServices;
import com.ecommerce.app.vendor.model.VendorVerifications;
import com.ecommerce.app.vendor.repository.VendorVerificationsRepository;
import static jakarta.persistence.GenerationType.UUID;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author libertyerp_local
 */
@Controller
@RequestMapping("/vendorverifications")
public class VendorVerificationsController {

    @Autowired
    private EmailServices emailServices;

    @Autowired
    VendorVerificationsRepository vendorVerificationsRepository;

    @RequestMapping("/emailverification")
    public String email_verifications_link_send(Model model, VendorVerifications vendorVerifications) {
        String token = UUID.toString();
        String otp = String.valueOf(new Random().nextInt(999999));
        vendorVerifications.setToken(token);
        vendorVerifications.setOtp(otp);
        vendorVerificationsRepository.save(vendorVerifications);
        emailServices.sendEmail(vendorVerifications.getEmail(), "Verify your email",
                "Click to verify: http://localhost:8080/verify-email?token=" + token);
        // send SMS via any gateway with OTP (mocked here)
        System.out.println("Send OTP to mobile: " + otp);
        return "redirect:/verify-email";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token) {
        Optional<VendorVerifications> optional = vendorVerificationsRepository.findByToken(token);
        if (optional.isPresent()) {
            VendorVerifications vendorVerifications = optional.get();
            if (vendorVerifications.getTokenCreatedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
                return "token_expired";
            }
            vendorVerifications.setEmailVerified(true);
            vendorVerificationsRepository.save(vendorVerifications);
            return "email_verified";
        }
        return "invalid_token";
    }

    @RequestMapping("/mobile_verification_otp_send")
    public String mobileverifications(Model model) {
        model.addAttribute("attribute", "value");
        return "view.name";
    }

    public String verifyMobile(@RequestParam String email, @RequestParam String otp) {
        Optional<VendorVerifications> optional = vendorVerificationsRepository.findByEmail(email);
        if (optional.isPresent()) {
            VendorVerifications vendorVerifications = optional.get();
            if (vendorVerifications.getOtpCreatedAt().isBefore(LocalDateTime.now().minusMinutes(10))) {
                return "otp_expired";
            }
            if (vendorVerifications.getOtp().equals(otp)) {
                vendorVerifications.setMobileVerified(true);
                vendorVerificationsRepository.save(vendorVerifications);
                return "mobile_verified";
            }
        }
        return "verify_mobile";
    }

}
