package com.ecommerce.app.module.checkout.customer.dto;

public class CustomerCodMobileOtpResponse {

    private boolean success;
    private String message;
    private String sessionToken;
    private int resendAvailableInSeconds;
    private String maskedMobile;
    private boolean verificationRequired;
    private boolean verified;

    public static CustomerCodMobileOtpResponse success(String message) {
        CustomerCodMobileOtpResponse response = new CustomerCodMobileOtpResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static CustomerCodMobileOtpResponse failure(String message) {
        CustomerCodMobileOtpResponse response = new CustomerCodMobileOtpResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public int getResendAvailableInSeconds() {
        return resendAvailableInSeconds;
    }

    public void setResendAvailableInSeconds(int resendAvailableInSeconds) {
        this.resendAvailableInSeconds = resendAvailableInSeconds;
    }

    public String getMaskedMobile() {
        return maskedMobile;
    }

    public void setMaskedMobile(String maskedMobile) {
        this.maskedMobile = maskedMobile;
    }

    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    public void setVerificationRequired(boolean verificationRequired) {
        this.verificationRequired = verificationRequired;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
