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
public class DistrictInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession();
        Object district = session.getAttribute("shippingdistrict");

        String uri = request.getRequestURI();

        // Allow these URLs to bypass the check
        if (uri.startsWith("/district/select")
                || uri.startsWith("/district/select-district")
                || uri.contains("/css")
                || uri.contains("/js")
                || uri.contains("/images")) {
            return true;
        }

        // If district is missing -> store target URL & redirect
        if (district == null) {
            session.setAttribute("redirectAfterDistrict", uri);
            response.sendRedirect("/district/select-district");
            return false;
        }

        return true;
    }
}
