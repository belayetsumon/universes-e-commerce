package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudEvaluationSource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class FraudAssessmentCreateRequest {

    @NotNull(message = "Order ID is required.")
    private Long orderId;

    @Size(max = 80, message = "Order UUID must be 80 characters or less.")
    private String orderUuid;

    private Long customerId;
    private Long vendorId;
    private BigDecimal orderTotal;

    @NotNull(message = "Evaluation source is required.")
    private FraudEvaluationSource evaluationSource;

    @Size(max = 120, message = "Correlation ID must be 120 characters or less.")
    private String correlationId;

    @Size(max = 160, message = "Idempotency key must be 160 characters or less.")
    private String idempotencyKey;

    private FraudContext context = new FraudContext();

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderUuid() { return orderUuid; }
    public void setOrderUuid(String orderUuid) { this.orderUuid = orderUuid; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public BigDecimal getOrderTotal() { return orderTotal; }
    public void setOrderTotal(BigDecimal orderTotal) { this.orderTotal = orderTotal; }
    public FraudEvaluationSource getEvaluationSource() { return evaluationSource; }
    public void setEvaluationSource(FraudEvaluationSource evaluationSource) { this.evaluationSource = evaluationSource; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public FraudContext getContext() { return context; }
    public void setContext(FraudContext context) { this.context = context == null ? new FraudContext() : context; }
}
