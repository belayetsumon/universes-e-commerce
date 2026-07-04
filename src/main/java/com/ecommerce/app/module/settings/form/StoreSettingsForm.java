package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
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

    private Boolean allowGuestCheckout;

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
        form.setAllowGuestCheckout(settings.getAllowGuestCheckout());
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

    public Boolean getAllowGuestCheckout() {
        return allowGuestCheckout;
    }

    public void setAllowGuestCheckout(Boolean allowGuestCheckout) {
        this.allowGuestCheckout = allowGuestCheckout;
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
