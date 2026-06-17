/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.module.settings.componant;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class GlobalSettingsContextComponent {

    public static final String SESSION_ATTRIBUTE = "globalSettings";

    public GlobalSettings getGlobalSettings(HttpSession session) {
        Object value = session.getAttribute(SESSION_ATTRIBUTE);
        if (value instanceof GlobalSettings globalSettings) {
            return globalSettings;
        }
        return null;
    }

    public void setGlobalSettings(HttpSession session, GlobalSettings globalSettings) {
        session.setAttribute(SESSION_ATTRIBUTE, globalSettings);
    }

}
