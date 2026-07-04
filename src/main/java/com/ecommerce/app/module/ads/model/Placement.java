/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.ecommerce.app.module.ads.model;

/**
 *
 * @author libertyerp_local
 */
public enum Placement {
    HOME_BANNER("Home Hero Banner"),
    HOME_FEATURED_CATEGORY_SHOWCASE("Home Featured Category Showcase"),
    HOME_TODAY_DEALS_SPOTLIGHT("Home Today Deals Spotlight"),
    HOME_BOTTOM("Home Bottom"),
    CATEGORY_TOP("Category Top"),
    PRODUCT_LIST_INLINE("Product List Inline"),
    PRODUCT_DETAIL_BOTTOM("Product Detail Bottom"),
    CART_TOP("Cart Top"),
    CHECKOUT_TOP("Checkout Top"),
    VENDOR_STORE_TOP("Vendor Store Top"),
    SIDEBAR("Sidebar Banner"),
    SIDEBAR_RIGHT("Sidebar Right"),
    FOOTER("Footer Banner"),
    FOOTER_TOP("Footer Top"),
    POPUP("Popup Banner"),
    OTHER("Other Placement");

    private final String displayName;

    Placement(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
