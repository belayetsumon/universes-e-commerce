package com.ecommerce.app.module.order.dto;

public class DateRangeOption {

    private final String value;
    private final String label;

    public DateRangeOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
