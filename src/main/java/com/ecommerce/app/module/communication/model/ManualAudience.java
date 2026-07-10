package com.ecommerce.app.module.communication.model;

public enum ManualAudience {
    ADMIN("Admin"),
    ALL_CUSTOMERS("All customers"),
    ALL_VENDORS("All vendors"),
    SELECTED_CUSTOMERS("Selected customers"),
    SELECTED_VENDORS("Selected vendors"),
    SINGLE_CUSTOMER("Single customer"),
    SINGLE_VENDOR("Single vendor"),
    OWN_CUSTOMERS("Own customers"),
    SELECTED_OWN_CUSTOMERS("Selected own customers");

    private final String displayName;

    ManualAudience(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
