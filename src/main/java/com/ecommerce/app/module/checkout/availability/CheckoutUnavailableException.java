package com.ecommerce.app.module.checkout.availability;

public class CheckoutUnavailableException extends RuntimeException {

    public CheckoutUnavailableException(String message) {
        super(message);
    }
}
