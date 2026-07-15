package com.ecommerce.app.module.sales.dashboard.dto;

public enum SalesDashboardChannel {
    REGISTERED_CHECKOUT("Registered checkout"),
    GUEST_CHECKOUT("Guest checkout");

    private final String label;

    SalesDashboardChannel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
