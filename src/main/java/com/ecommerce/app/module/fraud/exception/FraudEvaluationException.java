package com.ecommerce.app.module.fraud.exception;

public class FraudEvaluationException extends FraudException {

    public FraudEvaluationException(String message) {
        super(message);
    }

    public FraudEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
