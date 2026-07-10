package com.ecommerce.app.module.ReferralRewards.dto;

import com.ecommerce.app.order.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GiftCardPaymentForm {

    @NotNull(message = "Payment method is required.")
    private PaymentMethod paymentMethod = PaymentMethod.SSLCOMMERZ;

    @NotBlank(message = "Payment reference or transaction ID is required.")
    @Size(min = 4, max = 120, message = "Payment reference must be between 4 and 120 characters.")
    private String paymentReference;

    @Size(max = 500, message = "Payment note cannot exceed 500 characters.")
    private String paymentNote;

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }
}
