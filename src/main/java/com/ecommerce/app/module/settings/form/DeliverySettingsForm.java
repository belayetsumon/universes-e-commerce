package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class DeliverySettingsForm {

    private Long version;
    private Boolean deliveryEnabled;
    private Boolean freeDeliveryEnabled;

    @DecimalMin(value = "0.00", message = "Free delivery minimum amount cannot be negative.")
    private BigDecimal freeDeliveryMinAmount;

    @DecimalMin(value = "0.00", message = "Inside Dhaka delivery charge cannot be negative.")
    private BigDecimal insideDhakaDeliveryCharge;

    @DecimalMin(value = "0.00", message = "Outside Dhaka delivery charge cannot be negative.")
    private BigDecimal outsideDhakaDeliveryCharge;

    @Size(max = 150, message = "Delivery time text must be 150 characters or fewer.")
    private String deliveryTimeText;

    @DecimalMin(value = "0.00", message = "Cash on delivery charge cannot be negative.")
    private BigDecimal cashOnDeliveryCharge;

    public static DeliverySettingsForm from(GlobalSettings settings) {
        DeliverySettingsForm form = new DeliverySettingsForm();
        form.setVersion(settings.getVersion());
        form.setDeliveryEnabled(settings.getDeliveryEnabled());
        form.setFreeDeliveryEnabled(settings.getFreeDeliveryEnabled());
        form.setFreeDeliveryMinAmount(settings.getFreeDeliveryMinAmount());
        form.setInsideDhakaDeliveryCharge(settings.getInsideDhakaDeliveryCharge());
        form.setOutsideDhakaDeliveryCharge(settings.getOutsideDhakaDeliveryCharge());
        form.setDeliveryTimeText(settings.getDeliveryTimeText());
        form.setCashOnDeliveryCharge(settings.getCashOnDeliveryCharge());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean getDeliveryEnabled() {
        return deliveryEnabled;
    }

    public void setDeliveryEnabled(Boolean deliveryEnabled) {
        this.deliveryEnabled = deliveryEnabled;
    }

    public Boolean getFreeDeliveryEnabled() {
        return freeDeliveryEnabled;
    }

    public void setFreeDeliveryEnabled(Boolean freeDeliveryEnabled) {
        this.freeDeliveryEnabled = freeDeliveryEnabled;
    }

    public BigDecimal getFreeDeliveryMinAmount() {
        return freeDeliveryMinAmount;
    }

    public void setFreeDeliveryMinAmount(BigDecimal freeDeliveryMinAmount) {
        this.freeDeliveryMinAmount = freeDeliveryMinAmount;
    }

    public BigDecimal getInsideDhakaDeliveryCharge() {
        return insideDhakaDeliveryCharge;
    }

    public void setInsideDhakaDeliveryCharge(BigDecimal insideDhakaDeliveryCharge) {
        this.insideDhakaDeliveryCharge = insideDhakaDeliveryCharge;
    }

    public BigDecimal getOutsideDhakaDeliveryCharge() {
        return outsideDhakaDeliveryCharge;
    }

    public void setOutsideDhakaDeliveryCharge(BigDecimal outsideDhakaDeliveryCharge) {
        this.outsideDhakaDeliveryCharge = outsideDhakaDeliveryCharge;
    }

    public String getDeliveryTimeText() {
        return deliveryTimeText;
    }

    public void setDeliveryTimeText(String deliveryTimeText) {
        this.deliveryTimeText = deliveryTimeText;
    }

    public BigDecimal getCashOnDeliveryCharge() {
        return cashOnDeliveryCharge;
    }

    public void setCashOnDeliveryCharge(BigDecimal cashOnDeliveryCharge) {
        this.cashOnDeliveryCharge = cashOnDeliveryCharge;
    }
}
