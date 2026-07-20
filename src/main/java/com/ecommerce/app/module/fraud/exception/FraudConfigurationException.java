package com.ecommerce.app.module.fraud.exception;

public class FraudConfigurationException extends FraudException {

    public FraudConfigurationException(String message) {
        super(message);
    }

    public FraudConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
