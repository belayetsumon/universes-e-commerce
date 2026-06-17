/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.module.user.componant;

import com.ecommerce.app.module.browsinghistory.service.BrowsingHistoryService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    private BrowsingHistoryService browsingHistoryService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication != null) {
            browsingHistoryService.mergeGuestHistoryToUser(authentication.getName(), request, response);
        }
        handle(request, response, authentication);
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            return;
        }

        response.sendRedirect(targetUrl);
    }

    protected String determineTargetUrl(Authentication authentication) {
        String username = authentication.getName();

        Users users = usersRepository.findByEmail(username).orElse(null);
        if (users == null || users.getUserType() == null) {
            return "/access-denied";
        }

        switch (users.getUserType()) {
            case administrator:
                return "/admin";
            case systemadmin:
                return "/admin";
            case customer:
                return "/customer/index";
            default:
                return "/access-denied";
        }
    }
}
