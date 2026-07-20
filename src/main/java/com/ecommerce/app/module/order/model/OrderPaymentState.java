package com.ecommerce.app.module.order.model;

public enum OrderPaymentState {
    UNPAID,
    ADVANCE_PENDING,
    COD_PENDING,
    PARTIALLY_PAID,
    PAID,
    EMI_PENDING,
    CANCELLED,
    REFUNDED
}
