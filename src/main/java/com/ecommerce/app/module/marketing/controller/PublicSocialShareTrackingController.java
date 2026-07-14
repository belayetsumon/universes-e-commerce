package com.ecommerce.app.module.marketing.controller;

import com.ecommerce.app.module.marketing.dto.SocialShareRequest;
import com.ecommerce.app.module.marketing.services.SocialShareTrackingService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/share-track")
public class PublicSocialShareTrackingController {

    private final SocialShareTrackingService trackingService;
    private final UsersRepository usersRepository;

    public PublicSocialShareTrackingController(
            SocialShareTrackingService trackingService,
            UsersRepository usersRepository
    ) {
        this.trackingService = trackingService;
        this.usersRepository = usersRepository;
    }

    @PostMapping
    public Map<String, Object> track(
            @Valid @RequestBody SocialShareRequest request,
            BindingResult bindingResult,
            HttpServletRequest servletRequest
    ) {
        if (bindingResult.hasErrors()) {
            return Map.of("success", false, "message", "Invalid share tracking request.");
        }
        try {
            trackingService.trackShare(request, authenticatedUser(), servletRequest);
            return Map.of("success", true, "event", "SHARE_INITIATED");
        } catch (IllegalArgumentException ex) {
            return Map.of("success", false, "message", ex.getMessage());
        }
    }

    private Users authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return usersRepository.findByEmail(authentication.getName()).orElse(null);
    }
}
