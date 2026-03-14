/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component("app")
public class ThymeleafAppConfig {

    @Autowired
    private AppProperties appProperties;

    public String getName() {
        return appProperties.getName();
    }

    public String getHeader() {
        return appProperties.getHeader();
    }

    public String getFooter() {
        return appProperties.getFooter();
    }
}
