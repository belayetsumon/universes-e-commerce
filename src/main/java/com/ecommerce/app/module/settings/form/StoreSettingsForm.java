package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.model.StoreMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class StoreSettingsForm {

    private Long version;

    @Size(max = 10, message = "Default currency must be 10 characters or fewer.")
    private String defaultCurrency;

    private Boolean taxEnabled;

    @DecimalMin(value = "0.00", message = "Tax percentage cannot be negative.")
    @DecimalMax(value = "100.00", message = "Tax percentage cannot exceed 100.")
    private BigDecimal taxPercentage;

    private Boolean stockManagementEnabled;

    @Min(value = 0, message = "Low stock alert quantity cannot be negative.")
    private Integer lowStockAlertQty;

    private Boolean secureCheckoutEnabled;

    private Boolean allowGuestCheckout;

    private Boolean guestMobileRequired;

    private Boolean guestMobileOtpVerificationEnabled;

    @Min(value = 1, message = "Guest OTP expiry must be at least 1 minute.")
    private Integer guestOtpExpiryMinutes;

    @Min(value = 1, message = "Guest OTP maximum attempts must be at least 1.")
    private Integer guestOtpMaximumAttempts;

    @Min(value = 0, message = "Guest OTP resend cooldown cannot be negative.")
    private Integer guestOtpResendCooldownSeconds;

    @Min(value = 1, message = "Guest OTP daily send limit must be at least 1.")
    private Integer guestOtpDailySendLimit;

    private Boolean guestAutoCreateCustomerAccount;

    private StoreMode storeMode;

    private Long primaryVendorId;

    @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative.")
    private BigDecimal minimumOrderAmount;

    @DecimalMin(value = "0.00", message = "Maximum order amount cannot be negative.")
    private BigDecimal maximumOrderAmount;

    public static StoreSettingsForm from(GlobalSettings settings) {
        StoreSettingsForm form = new StoreSettingsForm();
        form.setVersion(settings.getVersion());
        form.setDefaultCurrency(settings.getDefaultCurrency());
        form.setTaxEnabled(settings.getTaxEnabled());
        form.setTaxPercentage(settings.getTaxPercentage());
        form.setStockManagementEnabled(settings.getStockManagementEnabled());
        form.setLowStockAlertQty(settings.getLowStockAlertQty());
        form.setSecureCheckoutEnabled(settings.getSecureCheckoutEnabled());
        form.setAllowGuestCheckout(settings.getAllowGuestCheckout());
        form.setGuestMobileRequired(settings.getGuestMobileRequired());
        form.setGuestMobileOtpVerificationEnabled(settings.getGuestMobileOtpVerificationEnabled());
        form.setGuestOtpExpiryMinutes(settings.getGuestOtpExpiryMinutes());
        form.setGuestOtpMaximumAttempts(settings.getGuestOtpMaximumAttempts());
        form.setGuestOtpResendCooldownSeconds(settings.getGuestOtpResendCooldownSeconds());
        form.setGuestOtpDailySendLimit(settings.getGuestOtpDailySendLimit());
        form.setGuestAutoCreateCustomerAccount(settings.getGuestAutoCreateCustomerAccount());
        form.setStoreMode(settings.getStoreMode());
        form.setPrimaryVendorId(settings.getPrimaryVendorId());
        form.setMinimumOrderAmount(settings.getMinimumOrderAmount());
        form.setMaximumOrderAmount(settings.getMaximumOrderAmount());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public Boolean getTaxEnabled() {
        return taxEnabled;
    }

    public void setTaxEnabled(Boolean taxEnabled) {
        this.taxEnabled = taxEnabled;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public Boolean getStockManagementEnabled() {
        return stockManagementEnabled;
    }

    public void setStockManagementEnabled(Boolean stockManagementEnabled) {
        this.stockManagementEnabled = stockManagementEnabled;
    }

    public Integer getLowStockAlertQty() {
        return lowStockAlertQty;
    }

    public void setLowStockAlertQty(Integer lowStockAlertQty) {
        this.lowStockAlertQty = lowStockAlertQty;
    }

    public Boolean getSecureCheckoutEnabled() {
        return secureCheckoutEnabled;
    }

    public void setSecureCheckoutEnabled(Boolean secureCheckoutEnabled) {
        this.secureCheckoutEnabled = secureCheckoutEnabled;
    }

    public Boolean getAllowGuestCheckout() {
        return allowGuestCheckout;
    }

    public void setAllowGuestCheckout(Boolean allowGuestCheckout) {
        this.allowGuestCheckout = allowGuestCheckout;
    }

    public Boolean getGuestMobileRequired() {
        return guestMobileRequired;
    }

    public void setGuestMobileRequired(Boolean guestMobileRequired) {
        this.guestMobileRequired = guestMobileRequired;
    }

    public Boolean getGuestMobileOtpVerificationEnabled() {
        return guestMobileOtpVerificationEnabled;
    }

    public void setGuestMobileOtpVerificationEnabled(Boolean guestMobileOtpVerificationEnabled) {
        this.guestMobileOtpVerificationEnabled = guestMobileOtpVerificationEnabled;
    }

    public Integer getGuestOtpExpiryMinutes() {
        return guestOtpExpiryMinutes;
    }

    public void setGuestOtpExpiryMinutes(Integer guestOtpExpiryMinutes) {
        this.guestOtpExpiryMinutes = guestOtpExpiryMinutes;
    }

    public Integer getGuestOtpMaximumAttempts() {
        return guestOtpMaximumAttempts;
    }

    public void setGuestOtpMaximumAttempts(Integer guestOtpMaximumAttempts) {
        this.guestOtpMaximumAttempts = guestOtpMaximumAttempts;
    }

    public Integer getGuestOtpResendCooldownSeconds() {
        return guestOtpResendCooldownSeconds;
    }

    public void setGuestOtpResendCooldownSeconds(Integer guestOtpResendCooldownSeconds) {
        this.guestOtpResendCooldownSeconds = guestOtpResendCooldownSeconds;
    }

    public Integer getGuestOtpDailySendLimit() {
        return guestOtpDailySendLimit;
    }

    public void setGuestOtpDailySendLimit(Integer guestOtpDailySendLimit) {
        this.guestOtpDailySendLimit = guestOtpDailySendLimit;
    }

    public Boolean getGuestAutoCreateCustomerAccount() {
        return guestAutoCreateCustomerAccount;
    }

    public void setGuestAutoCreateCustomerAccount(Boolean guestAutoCreateCustomerAccount) {
        this.guestAutoCreateCustomerAccount = guestAutoCreateCustomerAccount;
    }

    public StoreMode getStoreMode() {
        return storeMode;
    }

    public void setStoreMode(StoreMode storeMode) {
        this.storeMode = storeMode;
    }

    public Long getPrimaryVendorId() {
        return primaryVendorId;
    }

    public void setPrimaryVendorId(Long primaryVendorId) {
        this.primaryVendorId = primaryVendorId;
    }

    public BigDecimal getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public BigDecimal getMaximumOrderAmount() {
        return maximumOrderAmount;
    }

    public void setMaximumOrderAmount(BigDecimal maximumOrderAmount) {
        this.maximumOrderAmount = maximumOrderAmount;
    }
}
