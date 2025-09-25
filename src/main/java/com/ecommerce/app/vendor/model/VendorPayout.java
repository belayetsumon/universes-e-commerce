/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.vendor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *
 * @author libertyerp_local
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class VendorPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(optional = false)
    @JoinColumn(name = "vendor_id", nullable = false)
    @NotNull(message = "Vendor must not be null.")
    private Vendorprofile vendor;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount must not be null.")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
    @Digits(integer = 12, fraction = 2, message = "Amount format is invalid.")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status must not be null.")
    private VendorPayoutStatusEnum status; // REQUESTED, PROCESSING, PAID, FAILED

    @ManyToOne(optional = false)
    @JoinColumn(name = "payout_method_id", nullable = false)
    @NotNull(message = "Payout method must be selected.")
    private VendorPayoutMethod payoutMethod; // BANK, BKASH, UPI, etc.

    @Column(length = 100)
    private String payoutReference; // Reference ID from bank/gateway

    @Column(length = 255)
    private String adminNote;
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime processedAt;
    private LocalDateTime paidAt;

    /// Audit ///
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedBy
    @Column(insertable = false)
    private String modifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime modified;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
    }

    public VendorPayout() {
    }

    public VendorPayout(Long id, Vendorprofile vendor, BigDecimal amount, VendorPayoutStatusEnum status, VendorPayoutMethod payoutMethod, String payoutReference, String adminNote, LocalDateTime processedAt, LocalDateTime paidAt, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.vendor = vendor;
        this.amount = amount;
        this.status = status;
        this.payoutMethod = payoutMethod;
        this.payoutReference = payoutReference;
        this.adminNote = adminNote;
        this.processedAt = processedAt;
        this.paidAt = paidAt;
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Vendorprofile getVendor() {
        return vendor;
    }

    public void setVendor(Vendorprofile vendor) {
        this.vendor = vendor;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public VendorPayoutStatusEnum getStatus() {
        return status;
    }

    public void setStatus(VendorPayoutStatusEnum status) {
        this.status = status;
    }

    public VendorPayoutMethod getPayoutMethod() {
        return payoutMethod;
    }

    public void setPayoutMethod(VendorPayoutMethod payoutMethod) {
        this.payoutMethod = payoutMethod;
    }

    public String getPayoutReference() {
        return payoutReference;
    }

    public void setPayoutReference(String payoutReference) {
        this.payoutReference = payoutReference;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

}
