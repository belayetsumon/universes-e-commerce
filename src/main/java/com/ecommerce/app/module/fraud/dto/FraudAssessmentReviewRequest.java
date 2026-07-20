package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudAction;
import com.ecommerce.app.module.fraud.model.FraudDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FraudAssessmentReviewRequest {

    @NotNull(message = "Decision is required.")
    private FraudDecision decision;

    @NotNull(message = "Action is required.")
    private FraudAction action;

    @NotBlank(message = "Review reason is required.")
    @Size(max = 500, message = "Review reason must be 500 characters or less.")
    private String reason;

    @Size(max = 2000, message = "Review notes must be 2000 characters or less.")
    private String notes;

    @Size(max = 120, message = "Correlation ID must be 120 characters or less.")
    private String correlationId;

    @Size(max = 160, message = "Idempotency key must be 160 characters or less.")
    private String idempotencyKey;

    public FraudDecision getDecision() { return decision; }
    public void setDecision(FraudDecision decision) { this.decision = decision; }
    public FraudAction getAction() { return action; }
    public void setAction(FraudAction action) { this.action = action; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
