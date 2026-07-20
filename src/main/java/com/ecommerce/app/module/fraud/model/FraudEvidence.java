package com.ecommerce.app.module.fraud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fraud_evidence", indexes = {
    @Index(name = "idx_fraud_evidence_case", columnList = "case_id"),
    @Index(name = "idx_fraud_evidence_assessment", columnList = "assessment_id")
})
public class FraudEvidence extends BaseFraudEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private FraudCase fraudCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id")
    private FraudAssessment assessment;

    @Column(name = "evidence_type", nullable = false, length = 80)
    private String evidenceType;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Lob
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "uploaded_by", length = 120)
    private String uploadedBy;

    public FraudCase getFraudCase() { return fraudCase; }
    public void setFraudCase(FraudCase fraudCase) { this.fraudCase = fraudCase; }
    public FraudAssessment getAssessment() { return assessment; }
    public void setAssessment(FraudAssessment assessment) { this.assessment = assessment; }
    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
