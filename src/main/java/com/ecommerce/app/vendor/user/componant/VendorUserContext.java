/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.vendor.user.componant;

import com.ecommerce.app.vendor.model.Vendorprofile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 *
 * @author libertyerp_local
 */
@Component
@SessionScope
public class VendorUserContext {

    private Vendorprofile activeVendor;

    public Vendorprofile getActiveVendor() {
        return activeVendor;
    }

    public void setActiveVendor(Vendorprofile activeVendor) {
        this.activeVendor = activeVendor;
    }

}
