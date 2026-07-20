package com.ecommerce.app.module.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FraudWebhookRequest {

    @NotBlank(message = "Provider is required.")
    @Size(max = 80, message = "Provider must be 80 characters or less.")
    private String provider;

    @NotBlank(message = "Event type is required.")
    @Size(max = 120, message = "Event type must be 120 characters or less.")
    private String eventType;

    @Size(max = 160, message = "External reference must be 160 characters or less.")
    private String externalReference;

    @NotBlank(message = "Payload is required.")
    private String payloadJson;

    @Size(max = 250, message = "Signature must be 250 characters or less.")
    private String signature;

    @Size(max = 120, message = "Correlation ID must be 120 characters or less.")
    private String correlationId;

    @Size(max = 160, message = "Idempotency key must be 160 characters or less.")
    private String idempotencyKey;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
