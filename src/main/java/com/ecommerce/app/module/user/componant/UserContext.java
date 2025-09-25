/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.module.user.componant;

import com.ecommerce.app.vendor.model.Vendorprofile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 *
 * @author libertyerp_local
 */
@Component
@SessionScope // or a ThreadLocal for APIs
public class UserContext {

    private Vendorprofile activeVendor;

    public Vendorprofile getActiveVendor() {
        return activeVendor;
    }

    public void setActiveVendor(Vendorprofile activeVendor) {
        this.activeVendor = activeVendor;
    }

}
