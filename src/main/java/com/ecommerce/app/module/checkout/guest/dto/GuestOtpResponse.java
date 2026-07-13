package com.ecommerce.app.module.checkout.guest.dto;

public class GuestOtpResponse {

    private boolean success;
    private String message;
    private String sessionToken;
    private int resendAvailableInSeconds;
    private String maskedMobile;
    private boolean otpRequired = true;
    private String mobileVerificationStatus;

    public static GuestOtpResponse success(String message) {
        GuestOtpResponse response = new GuestOtpResponse();
        response.setSuccess(true);
        response.setMessage(message);
        return response;
    }

    public static GuestOtpResponse failure(String message) {
        GuestOtpResponse response = new GuestOtpResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    public int getResendAvailableInSeconds() { return resendAvailableInSeconds; }
    public void setResendAvailableInSeconds(int resendAvailableInSeconds) { this.resendAvailableInSeconds = resendAvailableInSeconds; }
    public String getMaskedMobile() { return maskedMobile; }
    public void setMaskedMobile(String maskedMobile) { this.maskedMobile = maskedMobile; }
    public boolean isOtpRequired() { return otpRequired; }
    public void setOtpRequired(boolean otpRequired) { this.otpRequired = otpRequired; }
    public String getMobileVerificationStatus() { return mobileVerificationStatus; }
    public void setMobileVerificationStatus(String mobileVerificationStatus) { this.mobileVerificationStatus = mobileVerificationStatus; }
}
