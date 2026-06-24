/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 *
 * @author libertyerp_local
 */
@Component
public class ShippingLocationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession();
        Object location = session.getAttribute("shippingLocation");
        if (location == null) {
            location = session.getAttribute("shippingLocationId");
        }

        String uri = request.getRequestURI();

        // Allow these URLs to bypass the check
        if (uri.startsWith("/district/select")
                || uri.startsWith("/district/select-district")
                || uri.contains("/css")
                || uri.contains("/js")
                || uri.contains("/images")) {
            return true;
        }

        if (location == null) {
            session.setAttribute("redirectAfterLocation", uri);
            response.sendRedirect("/district/select-district");
            return false;
        }

        return true;
    }
}
