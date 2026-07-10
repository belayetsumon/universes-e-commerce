package com.ecommerce.app.vendor.controller;

import com.ecommerce.app.vendor.model.VendorVerifications;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.services.VendorVerificationsService;
import com.ecommerce.app.vendor.services.VendorVerificationsService.EmailVerificationResult;
import com.ecommerce.app.vendor.services.VendorVerificationsService.MobileVerificationResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendorverifications")
public class VendorVerificationsController {

    private final VendorVerificationsService verificationService;

    public VendorVerificationsController(VendorVerificationsService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping({"", "/", "/index"})
    public String index(Model model) {
        Vendorprofile vendor = verificationService.requireActiveVendorProfile();
        if (vendor == null) {
            return "redirect:/vendorprofile/index";
        }

        VendorVerifications verification = verificationService.getOrCreateForVendor(vendor);
        populateModel(model, vendor, verification);
        return "vendor/verifications/verify_email";
    }

    @PostMapping("/emailverification")
    public String sendEmailVerification(RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = verificationService.requireActiveVendorProfile();
        if (vendor == null) {
            return "redirect:/vendorprofile/index";
        }

        try {
            verificationService.createEmailVerification(vendor);
            redirectAttributes.addFlashAttribute("successMessage", "Verification email has been queued for " + vendor.getEmail() + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendorverifications";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(required = false) String token, Model model, RedirectAttributes redirectAttributes) {
        EmailVerificationResult result = verificationService.verifyEmail(token);
        if (result == EmailVerificationResult.VERIFIED) {
            model.addAttribute("successMessage", "Vendor email verified successfully.");
            return "vendor/verifications/email_verified";
        }
        if (result == EmailVerificationResult.EXPIRED) {
            return "vendor/verifications/token_expired";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid email verification token.");
        return "redirect:/vendorverifications";
    }

    @GetMapping("/email-verified")
    public String emailVerified(Model model) {
        Vendorprofile vendor = verificationService.requireActiveVendorProfile();
        if (vendor == null) {
            return "redirect:/vendorprofile/index";
        }
        populateModel(model, vendor, verificationService.getOrCreateForVendor(vendor));
        return "vendor/verifications/email_verified";
    }

    @PostMapping("/mobile-verification-otp-send")
    public String sendMobileOtp(RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = verificationService.requireActiveVendorProfile();
        if (vendor == null) {
            return "redirect:/vendorprofile/index";
        }

        try {
            verificationService.createMobileOtp(vendor);
            redirectAttributes.addFlashAttribute("successMessage", "Mobile verification OTP has been queued for " + vendor.getPhone() + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/vendorverifications/verify-mobile";
    }

    @GetMapping("/verify-mobile")
    public String verifyMobileForm(Model model) {
        Vendorprofile vendor = verificationService.requireActiveVendorProfile();
        if (vendor == null) {
            return "redirect:/vendorprofile/index";
        }
        populateModel(model, vendor, verificationService.getOrCreateForVendor(vendor));
        return "vendor/verifications/verify_mobile";
    }

    @PostMapping("/verify-mobile")
    public String verifyMobile(@RequestParam(required = false) String otp, RedirectAttributes redirectAttributes) {
        Vendorprofile vendor = verificationService.requireActiveVendorProfile();
        if (vendor == null) {
            return "redirect:/vendorprofile/index";
        }

        MobileVerificationResult result = verificationService.verifyMobile(vendor, otp);
        if (result == MobileVerificationResult.VERIFIED) {
            redirectAttributes.addFlashAttribute("successMessage", "Vendor mobile number verified successfully.");
            return "redirect:/vendorverifications/mobile-verified";
        }
        if (result == MobileVerificationResult.EXPIRED) {
            return "vendor/verifications/otp_expired";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Invalid mobile verification OTP.");
        return "redirect:/vendorverifications/verify-mobile";
    }

    @GetMapping("/mobile-verified")
    public String mobileVerified(Model model) {
        Vendorprofile vendor = verificationService.requireActiveVendorProfile();
        if (vendor == null) {
            return "redirect:/vendorprofile/index";
        }
        populateModel(model, vendor, verificationService.getOrCreateForVendor(vendor));
        return "vendor/verifications/mobile_verified";
    }

    private void populateModel(Model model, Vendorprofile vendor, VendorVerifications verification) {
        model.addAttribute("vendor", vendor);
        model.addAttribute("verification", verification);
        model.addAttribute("emailTokenValidMinutes", verificationService.emailTokenValidMinutes());
        model.addAttribute("mobileOtpValidMinutes", verificationService.mobileOtpValidMinutes());
    }
}
