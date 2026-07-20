package com.ecommerce.app.module.fraud.dto;

import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalCategory;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import java.time.LocalDateTime;

public class FraudSignalResult {

    private String signalCode;
    private FraudSignalCategory category;
    private String signalValue;
    private int scoreImpact;
    private boolean triggered;
    private FraudSignalSeverity severity = FraudSignalSeverity.INFO;
    private String source;
    private FraudReasonCode reasonCode;
    private LocalDateTime detectedAt = LocalDateTime.now();
    private String metadataJson;

    public String getSignalCode() { return signalCode; }
    public void setSignalCode(String signalCode) { this.signalCode = signalCode; }
    public FraudSignalCategory getCategory() { return category; }
    public void setCategory(FraudSignalCategory category) { this.category = category; }
    public String getSignalValue() { return signalValue; }
    public void setSignalValue(String signalValue) { this.signalValue = signalValue; }
    public int getScoreImpact() { return scoreImpact; }
    public void setScoreImpact(int scoreImpact) { this.scoreImpact = scoreImpact; }
    public boolean isTriggered() { return triggered; }
    public void setTriggered(boolean triggered) { this.triggered = triggered; }
    public FraudSignalSeverity getSeverity() { return severity; }
    public void setSeverity(FraudSignalSeverity severity) { this.severity = severity == null ? FraudSignalSeverity.INFO : severity; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public FraudReasonCode getReasonCode() { return reasonCode; }
    public void setReasonCode(FraudReasonCode reasonCode) { this.reasonCode = reasonCode; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
