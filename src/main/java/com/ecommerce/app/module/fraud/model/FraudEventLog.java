package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_event_logs", indexes = {
    @Index(name = "idx_fraud_event_type", columnList = "event_type"),
    @Index(name = "idx_fraud_event_aggregate", columnList = "aggregate_type,aggregate_id"),
    @Index(name = "idx_fraud_event_order", columnList = "order_id"),
    @Index(name = "idx_fraud_event_created", columnList = "event_time")
})
public class FraudEventLog extends BaseFraudEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 80)
    private FraudEventType eventType;

    @Column(name = "aggregate_type", length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id")
    private Long aggregateId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "correlation_id", length = 120)
    private String correlationId;

    @Column(name = "idempotency_key", length = 160)
    private String idempotencyKey;

    @Lob
    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime = LocalDateTime.now();

    public FraudEventType getEventType() { return eventType; }
    public void setEventType(FraudEventType eventType) { this.eventType = eventType; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public Long getAggregateId() { return aggregateId; }
    public void setAggregateId(Long aggregateId) { this.aggregateId = aggregateId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
