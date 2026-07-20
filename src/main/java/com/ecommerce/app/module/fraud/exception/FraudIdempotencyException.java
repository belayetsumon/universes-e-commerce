package com.ecommerce.app.module.fraud.exception;

public class FraudIdempotencyException extends FraudException {

    public FraudIdempotencyException(String message) {
        super(message);
    }
}
