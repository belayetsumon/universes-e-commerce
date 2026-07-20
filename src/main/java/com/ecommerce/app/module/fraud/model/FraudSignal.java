package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_signals", indexes = {
    @Index(name = "idx_fraud_signal_assessment", columnList = "assessment_id"),
    @Index(name = "idx_fraud_signal_code", columnList = "signal_code"),
    @Index(name = "idx_fraud_signal_category", columnList = "signal_category"),
    @Index(name = "idx_fraud_signal_triggered", columnList = "triggered")
})
public class FraudSignal extends BaseFraudEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private FraudAssessment assessment;

    @Column(name = "signal_code", nullable = false, length = 100)
    private String signalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_category", nullable = false, length = 40)
    private FraudSignalCategory signalCategory;

    @Column(name = "signal_value", length = 500)
    private String signalValue;

    @Column(name = "score_impact", nullable = false)
    private int scoreImpact;

    @Column(name = "triggered", nullable = false)
    private boolean triggered;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private FraudSignalSeverity severity = FraudSignalSeverity.INFO;

    @Column(name = "source", length = 100)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", length = 80)
    private FraudReasonCode reasonCode;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Lob
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    public FraudAssessment getAssessment() { return assessment; }
    public void setAssessment(FraudAssessment assessment) { this.assessment = assessment; }
    public String getSignalCode() { return signalCode; }
    public void setSignalCode(String signalCode) { this.signalCode = signalCode; }
    public FraudSignalCategory getSignalCategory() { return signalCategory; }
    public void setSignalCategory(FraudSignalCategory signalCategory) { this.signalCategory = signalCategory; }
    public String getSignalValue() { return signalValue; }
    public void setSignalValue(String signalValue) { this.signalValue = signalValue; }
    public int getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(int scoreImpact) { this.scoreImpact = scoreImpact; }
    public boolean isTriggered() { return triggered; }
    public void setTriggered(boolean triggered) { this.triggered = triggered; }
    public FraudSignalSeverity getSeverity() { return severity; }
    public void setSeverity(FraudSignalSeverity severity) { this.severity = severity; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public FraudReasonCode getReasonCode() { return reasonCode; }
    public void setReasonCode(FraudReasonCode reasonCode) { this.reasonCode = reasonCode; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
