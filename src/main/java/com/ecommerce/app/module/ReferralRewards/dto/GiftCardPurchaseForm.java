package com.ecommerce.app.module.ReferralRewards.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class GiftCardPurchaseForm {

    @NotNull(message = "Gift card amount is required.")
    @DecimalMin(value = "100.00", message = "Gift card amount must be at least 100.00.")
    @DecimalMax(value = "50000.00", message = "Gift card amount cannot exceed 50000.00.")
    @Digits(integer = 10, fraction = 2, message = "Gift card amount format is invalid.")
    private BigDecimal amount = new BigDecimal("1000.00");

    @Size(max = 120, message = "Recipient name cannot exceed 120 characters.")
    private String recipientName;

    @Email(message = "Recipient email must be a valid email address.")
    @Size(max = 180, message = "Recipient email cannot exceed 180 characters.")
    private String recipientEmail;

    @Size(max = 500, message = "Gift message cannot exceed 500 characters.")
    private String message;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
