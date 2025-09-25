/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.vendor.user.model;

import com.ecommerce.app.module.user.model.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
public class VendorRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Name cannot be blank.")
    private String name;

    @NotEmpty(message = "Slug cannot be blank.")
    private String slug;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "vendor_role_vendor_privilege",
            joinColumns = @JoinColumn(name = "vendor_role_id"),
            inverseJoinColumns = @JoinColumn(name = "vendor_privilege_id")
    )
    private Set<VendorPrivilege> vendorPrivilege = new HashSet<>();  // ✅ initialize

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

/// ✅ Full constructor
    public VendorRole(Long id,
            String name,
            String slug,
            Set<Users> users,
            Set<VendorPrivilege> vendorPrivilege,
            String createdBy,
            LocalDateTime created,
            String modifiedBy,
            LocalDateTime modified) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.vendorPrivilege = (vendorPrivilege != null) ? vendorPrivilege : new HashSet<>();
        this.createdBy = createdBy;
        this.created = created;
        this.modifiedBy = modifiedBy;
        this.modified = modified;
    }

    // ✅ Getters & setters (or Lombok @Data if you use it)
    public VendorRole() {
    }

    // ...getters and setters for all fields...
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Set<VendorPrivilege> getVendorPrivilege() {
        return vendorPrivilege;
    }

    public void setVendorPrivilege(Set<VendorPrivilege> vendorPrivilege) {
        this.vendorPrivilege = vendorPrivilege;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public LocalDateTime getModified() {
        return modified;
    }
}
