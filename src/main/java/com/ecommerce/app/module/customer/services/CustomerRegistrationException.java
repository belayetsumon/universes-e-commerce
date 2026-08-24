package com.ecommerce.app.module.customer.services;

public class CustomerRegistrationException extends RuntimeException {

    private final String field;

    public CustomerRegistrationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
