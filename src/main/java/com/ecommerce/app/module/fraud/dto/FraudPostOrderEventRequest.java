package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudPostOrderEventType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FraudPostOrderEventRequest {

    @NotNull
    private FraudPostOrderEventType eventType;
    private Long orderId;
    private Long customerId;
    private Long vendorId;
    private String aggregateType;
    private Long aggregateId;
    private BigDecimal amount;
    private String reason;
    private String source;
    private String correlationId;
    private String idempotencyKey;
    private Map<String, Object> metadata = new HashMap<>();

    public FraudPostOrderEventType getEventType() { return eventType; }
    public void setEventType(FraudPostOrderEventType eventType) { this.eventType = eventType; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public Long getAggregateId() { return aggregateId; }
    public void setAggregateId(Long aggregateId) { this.aggregateId = aggregateId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata == null ? new HashMap<>() : metadata; }
}
