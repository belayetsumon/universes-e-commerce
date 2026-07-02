package com.ecommerce.app.commission.model;

public enum CommissionType {
    DEFAULT("Default Commission"),
    CATEGORY("Category Commission"),
    VENDOR("Vendor Commission"),
    PRODUCT("Product Commission");

    private final String label;

    CommissionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
