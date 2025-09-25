/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.vendor.model;

import com.ecommerce.app.module.ReferralRewards.model.CustomerCashOutPaymentMethod;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class VendorPayoutMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(nullable = false)
    private Vendorprofile vendor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerCashOutPaymentMethod preferredMethod; // MOBILE or BANK
    // Bank details

    @NotBlank(message = " Account type can not be blank")
    @Size(min = 5, max = 255)
    private String accountType; //(Personal, Agent, Marchent, Company etc)

    @NotBlank(message = " bank/mobile finance company name can not be blank")
    @Size(min = 5, max = 255)
    private String bankName;
    @NotBlank(message = " Account name can not be blank")
    @Size(min = 5, max = 255)
    private String accountTitle;
    @NotBlank(message = " Account number can not be blank")
    @Size(min = 5, max = 255)
    private String accountNumber;

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

    public VendorPayoutMethod(Long id, Vendorprofile vendor, CustomerCashOutPaymentMethod preferredMethod, String accountType, String bankName, String accountTitle, String accountNumber, String createdBy, LocalDateTime created, String modifiedBy, LocalDateTime modified) {
        this.id = id;
        this.vendor = vendor;
        this.preferredMethod = preferredMethod;
        this.accountType = accountType;
        this.bankName = bankName;
        this.accountTitle = accountTitle;
        this.accountNumber = accountNumber;
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
    }

    public VendorPayoutMethod() {
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

    public CustomerCashOutPaymentMethod getPreferredMethod() {
        return preferredMethod;
    }

    public void setPreferredMethod(CustomerCashOutPaymentMethod preferredMethod) {
        this.preferredMethod = preferredMethod;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public void setAccountTitle(String accountTitle) {
        this.accountTitle = accountTitle;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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
