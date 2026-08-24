package com.ecommerce.app.module.order.services;

public class CheckoutPlacementIdempotencyException extends RuntimeException {

    public CheckoutPlacementIdempotencyException(String message) {
        super(message);
    }
}
