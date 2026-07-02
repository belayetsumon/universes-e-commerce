/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ReferralRewards.model;

import com.ecommerce.app.module.ReferralRewards.enumvalue.PromotionAuditAction;
import com.ecommerce.app.module.ReferralRewards.enumvalue.PromotionAuditEntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 * @author libertyerp_local
 */
@Entity
@Table(name = "promotions_audit_log", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_performed_by", columnList = "performed_by")

})
public class AuditLogPromotions extends BaseEntityPromotions {

    @NotNull(message = "Audit entity type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private PromotionAuditEntityType entityType;

    @NotBlank(message = "Audit entity ID is required.")
    @Size(max = 80, message = "Audit entity ID must not exceed 80 characters.")
    @Column(name = "entity_id", nullable = false, length = 80)
    private String entityId;

    @NotNull(message = "Audit action is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PromotionAuditAction action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @NotBlank(message = "Performed by is required.")
    @Size(max = 100, message = "Performed by must not exceed 100 characters.")
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Size(max = 45, message = "IP address must not exceed 45 characters.")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 500, message = "User agent must not exceed 500 characters.")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Size(max = 100, message = "Request ID must not exceed 100 characters.")
    @Column(name = "request_id", length = 100)
    private String requestId;

    @Size(max = 100, message = "Correlation ID must not exceed 100 characters.")
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Size(max = 255, message = "Reason must not exceed 255 characters.")
    @Column(length = 255)
    private String reason;

    public AuditLogPromotions() {
    }

    public AuditLogPromotions(PromotionAuditEntityType entityType, String entityId, PromotionAuditAction action, String oldValue, String newValue, String performedBy, String ipAddress, String userAgent, String requestId, String correlationId, String reason) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.performedBy = performedBy;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestId = requestId;
        this.correlationId = correlationId;
        this.reason = reason;
    }

    public PromotionAuditEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(PromotionAuditEntityType entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public PromotionAuditAction getAction() {
        return action;
    }

    public void setAction(PromotionAuditAction action) {
        this.action = action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
