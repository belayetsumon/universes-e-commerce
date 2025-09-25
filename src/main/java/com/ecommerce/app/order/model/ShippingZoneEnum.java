/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.ecommerce.app.order.model;

/**
 *
 * @author libertyerp_local
 */
public enum ShippingZoneEnum {
    SAME_CITY, // Example: ঢাকার ভিতরে / একই শহর
    SUB_AREA, // Example: ঢাকা ↔ সাব এরিয়া
    DHAKA_OUTSIDE, // Example: ঢাকা ↔ অন্য জেলা
    OTHER_CITY, // Example: অন্য জেলা ↔ অন্য জেলা
    SAME_DAY        // Example: ঢাকা / চট্টগ্রাম মেট্রো এরিয়া
}
