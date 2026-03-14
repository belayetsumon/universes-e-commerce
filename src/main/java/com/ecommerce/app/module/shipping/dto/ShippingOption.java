/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.shipping.dto;

import java.math.BigDecimal;

/**
 *
 * @author libertyerp_local
 */
public class ShippingOption {

    private String code;
    private String title;
    private BigDecimal price;
    private String estimatedDelivery;
    private String carrierCode;

    public ShippingOption(String code, String title, BigDecimal price, String estimatedDelivery, String carrierCode) {
        this.code = code;
        this.title = title;
        this.price = price;
        this.estimatedDelivery = estimatedDelivery;
        this.carrierCode = carrierCode;
    }

    public ShippingOption() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

}
