/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.order.model;

/**
 *
 * @author User
 */
public enum PaymentMethod {
    COD("Cash on Delivery"),
    CARD("Credit/Debit Card"),
    MOBILE_BANKING("Mobile Banking"),
    PAYPAL("PayPal"),
    ALIPAY("Alipay"),
    GOOGLE_PAY("Google Pay"),
    DUE("Due Amount");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
