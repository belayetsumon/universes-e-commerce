/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import com.ecommerce.app.module.settings.componant.GlobalSettingsContextComponent;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author libertyerp_local
 */
@Component

public class MaintenanceFilter extends OncePerRequestFilter {

    @Autowired
    private GlobalSettingsService globalSettingsService;

    @Autowired
    private GlobalSettingsContextComponent globalSettingsContextComponent;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        boolean staticResourceRequest = isStaticResourceRequest(uri);
        GlobalSettings settings = globalSettingsService.getActiveSettings();

        if (!staticResourceRequest) {
            HttpSession session = request.getSession();
            globalSettingsContextComponent.setGlobalSettings(session, settings);
        }

        boolean allowed
                = uri.startsWith("/admin")
                || uri.startsWith("/login")
                || staticResourceRequest;

        if (Boolean.TRUE.equals(settings.getMaintenanceMode()) && !allowed) {
            request.setAttribute("message", settings.getMaintenanceMessage());
            request.getRequestDispatcher("/maintenance").forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isStaticResourceRequest(String uri) {
        return uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/images")
                || uri.startsWith("/img")
                || uri.startsWith("/assets")
                || uri.startsWith("/webjars")
                || uri.startsWith("/bootstrap")
                || uri.startsWith("/plugin")
                || uri.startsWith("/files")
                || uri.equals("/favicon.ico");
    }
}
