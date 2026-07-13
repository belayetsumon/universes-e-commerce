package com.ecommerce.app.module.checkout.guest.session;

import com.ecommerce.app.module.checkout.guest.model.MobileVerificationStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class GuestCheckoutSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String checkoutSessionUuid = UUID.randomUUID().toString();
    private String verifiedMobile;
    private String otpVerificationUuid;
    private Long userId;
    private Long locationSessionId;
    private boolean guestCheckout = true;
    private boolean mobileVerified = false;
    private boolean mobileVerificationRequired = true;
    private MobileVerificationStatus mobileVerificationStatus = MobileVerificationStatus.PENDING;
    private LocalDateTime verificationTime;
    private LocalDateTime expiresAt;
    private String ipAddressHash;
    private String deviceFingerprintHash;
    private String status = "PENDING";

    public boolean isActive(LocalDateTime now) {
        return userId != null && expiresAt != null && expiresAt.isAfter(now)
                && (!mobileVerificationRequired || mobileVerificationStatus == MobileVerificationStatus.VERIFIED);
    }

    public String getCheckoutSessionUuid() { return checkoutSessionUuid; }
    public void setCheckoutSessionUuid(String checkoutSessionUuid) { this.checkoutSessionUuid = checkoutSessionUuid; }
    public String getVerifiedMobile() { return verifiedMobile; }
    public void setVerifiedMobile(String verifiedMobile) { this.verifiedMobile = verifiedMobile; }
    public String getOtpVerificationUuid() { return otpVerificationUuid; }
    public void setOtpVerificationUuid(String otpVerificationUuid) { this.otpVerificationUuid = otpVerificationUuid; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getLocationSessionId() { return locationSessionId; }
    public void setLocationSessionId(Long locationSessionId) { this.locationSessionId = locationSessionId; }
    public boolean isGuestCheckout() { return guestCheckout; }
    public void setGuestCheckout(boolean guestCheckout) { this.guestCheckout = guestCheckout; }
    public boolean isMobileVerified() { return mobileVerified; }
    public void setMobileVerified(boolean mobileVerified) { this.mobileVerified = mobileVerified; }
    public boolean isMobileVerificationRequired() { return mobileVerificationRequired; }
    public void setMobileVerificationRequired(boolean mobileVerificationRequired) { this.mobileVerificationRequired = mobileVerificationRequired; }
    public MobileVerificationStatus getMobileVerificationStatus() { return mobileVerificationStatus; }
    public void setMobileVerificationStatus(MobileVerificationStatus mobileVerificationStatus) { this.mobileVerificationStatus = mobileVerificationStatus; }
    public LocalDateTime getVerificationTime() { return verificationTime; }
    public void setVerificationTime(LocalDateTime verificationTime) { this.verificationTime = verificationTime; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getIpAddressHash() { return ipAddressHash; }
    public void setIpAddressHash(String ipAddressHash) { this.ipAddressHash = ipAddressHash; }
    public String getDeviceFingerprintHash() { return deviceFingerprintHash; }
    public void setDeviceFingerprintHash(String deviceFingerprintHash) { this.deviceFingerprintHash = deviceFingerprintHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
