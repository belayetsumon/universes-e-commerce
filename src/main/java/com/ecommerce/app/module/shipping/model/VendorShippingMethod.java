/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.shipping.model;

import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 *
 * @author libertyerp_local
 */
public class VendorShippingMethod {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Vendorprofile vendor;  // link to your Vendor entity

    private String methodName;   // "Vendor Home Delivery"
    private String code;         // VND-HOME
    private BigDecimal flatRate; // 60.00 BDT
    private int estimatedDays;   // 1-2 days
    private boolean allowCOD;    // Cash on Delivery?

    public VendorShippingMethod() {
    }

    public VendorShippingMethod(Long id, Vendorprofile vendor, String methodName, String code, BigDecimal flatRate, int estimatedDays, boolean allowCOD) {
        this.id = id;
        this.vendor = vendor;
        this.methodName = methodName;
        this.code = code;
        this.flatRate = flatRate;
        this.estimatedDays = estimatedDays;
        this.allowCOD = allowCOD;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vendorprofile getVendor() {
        return vendor;
    }

    public void setVendor(Vendorprofile vendor) {
        this.vendor = vendor;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getFlatRate() {
        return flatRate;
    }

    public void setFlatRate(BigDecimal flatRate) {
        this.flatRate = flatRate;
    }

    public int getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(int estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public boolean isAllowCOD() {
        return allowCOD;
    }

    public void setAllowCOD(boolean allowCOD) {
        this.allowCOD = allowCOD;
    }

}
