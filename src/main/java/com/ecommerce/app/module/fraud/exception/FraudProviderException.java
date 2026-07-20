package com.ecommerce.app.module.fraud.exception;

public class FraudProviderException extends FraudException {

    public FraudProviderException(String message) {
        super(message);
    }

    public FraudProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
