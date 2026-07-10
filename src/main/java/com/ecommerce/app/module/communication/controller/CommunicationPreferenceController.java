package com.ecommerce.app.module.communication.controller;

import com.ecommerce.app.module.communication.services.CommunicationPreferenceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/public/communication")
public class CommunicationPreferenceController {

    private final CommunicationPreferenceService preferenceService;

    public CommunicationPreferenceController(CommunicationPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/unsubscribe")
    public String unsubscribe(@RequestParam("token") String token, Model model) {
        try {
            preferenceService.unsubscribeMarketing(token);
            model.addAttribute("successMessage", "You have been unsubscribed from marketing messages.");
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", "This unsubscribe link is invalid or expired.");
        }
        return "public/communication-unsubscribe";
    }
}
