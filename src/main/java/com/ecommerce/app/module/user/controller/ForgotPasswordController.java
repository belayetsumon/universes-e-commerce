/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.module.user.controller;

import com.ecommerce.app.module.user.dto.PasswordResetForm;
import com.ecommerce.app.module.user.services.PasswordResetService;
import com.ecommerce.app.module.user.services.PasswordResetEmailSender.PasswordResetDeliveryException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 *
 * @author Md Belayet Hossin
 */
@Controller
@RequestMapping("/forgotpassword")

public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;
    private final String configuredResetBaseUrl;

    @Autowired
    public ForgotPasswordController(
            PasswordResetService passwordResetService,
            @Value("${app.security.password-reset.base-url:}") String configuredResetBaseUrl) {
        this.passwordResetService = passwordResetService;
        this.configuredResetBaseUrl = configuredResetBaseUrl;
    }

    @GetMapping(value = {"", "/", "/index", "/userforgotpassword"})
    public String userforgotpassword(Model model) {
        model.addAttribute("attribute", "value");
        return "user/forgotpassword";
    }

    @PostMapping("/showemail")
    public String showemail(@RequestParam(required = false, name = "email") String email,
            @RequestParam(required = false, name = "source") String source,
            Model model) {

        boolean publicRequest = "public".equalsIgnoreCase(source);
        String resetUrl = resetUrl();
        try {
            passwordResetService.requestReset(email, resetUrl);
        } catch (PasswordResetDeliveryException ex) {
            // Keep delivery failures indistinguishable from unknown addresses.
        }
        model.addAttribute("success", PasswordResetService.NEUTRAL_REQUEST_MESSAGE);
        return publicRequest ? "frontview/forgot-password" : "user/forgotpassword";
    }

    @GetMapping("/reset")
    public String resetPage(
            @RequestParam(required = false) String token,
            Model model) {
        model.addAttribute("token", token == null ? "" : token);
        model.addAttribute("passwordResetForm", new PasswordResetForm());
        if (!passwordResetService.isTokenUsable(token)) {
            model.addAttribute("error", PasswordResetService.INVALID_TOKEN_MESSAGE);
        }
        return "frontview/reset-password";
    }

    @PostMapping("/reset")
    public String resetPassword(
            @RequestParam(required = false) String token,
            @Valid @ModelAttribute("passwordResetForm") PasswordResetForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasFieldErrors("confirmPassword")
                && form.getNewPassword() != null
                && !form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }

        if (!bindingResult.hasErrors()
                && !passwordResetService.resetPassword(token, form.getNewPassword())) {
            model.addAttribute("error", PasswordResetService.INVALID_TOKEN_MESSAGE);
        }

        if (bindingResult.hasErrors() || model.containsAttribute("error")) {
            model.addAttribute("token", token == null ? "" : token);
            return "frontview/reset-password";
        }

        redirectAttributes.addFlashAttribute("success", "Your password has been reset. Please sign in with the new password.");
        return "redirect:/public/member-login";
    }

    private String resetUrl() {
        if (configuredResetBaseUrl != null && !configuredResetBaseUrl.isBlank()) {
            return configuredResetBaseUrl.trim().replaceAll("/+$", "") + "/forgotpassword/reset";
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/forgotpassword/reset")
                .toUriString();
    }
}
