package com.ecommerce.app.commission.model;

public enum CommissionStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    ARCHIVED("Archived");

    private final String label;

    CommissionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
