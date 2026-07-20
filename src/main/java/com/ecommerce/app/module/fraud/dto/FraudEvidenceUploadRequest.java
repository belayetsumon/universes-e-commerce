package com.ecommerce.app.module.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FraudEvidenceUploadRequest {

    @NotNull(message = "Fraud case ID is required.")
    private Long caseId;

    private Long assessmentId;

    @NotBlank(message = "Evidence type is required.")
    @Size(max = 80, message = "Evidence type must be 80 characters or less.")
    private String evidenceType;

    @NotBlank(message = "Evidence title is required.")
    @Size(max = 160, message = "Evidence title must be 160 characters or less.")
    private String title;

    @Size(max = 1000, message = "Evidence note must be 1000 characters or less.")
    private String note;

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
