package com.ecommerce.app.module.shipping.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shipping_location")
public class ShippingLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String uuid = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Location type is required")
    @Column(nullable = false, length = 30)
    private ShippingLocationType type = ShippingLocationType.COUNTRY;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private ShippingLocation parent;

    @NotBlank(message = "Location name is required")
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String name;

    @Size(max = 160)
    @Column(length = 160)
    private String localName;

    @NotBlank(message = "Location code is required")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String code;

    @Size(max = 2)
    @Column(length = 2)
    private String iso2;

    @Size(max = 3)
    @Column(length = 3)
    private String iso3;

    @Size(max = 80)
    @Column(length = 80)
    private String externalCode;

    @Size(max = 80)
    @Column(length = 80)
    private String postalCode;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false)
    private Integer priority = 100;

    @Column(nullable = false)
    private boolean active = true;

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

    public String getDisplayLabel() {
        List<String> parts = new ArrayList<>();
        ShippingLocation current = this;
        while (current != null) {
            parts.add(current.getName());
            current = current.getParent();
        }
        Collections.reverse(parts);
        return String.join(" / ", parts);
    }

    public boolean isSameOrAncestorOf(ShippingLocation location) {
        ShippingLocation current = location;
        while (current != null) {
            if (id != null && id.equals(current.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    public boolean isSameOrChildOf(ShippingLocation location) {
        return location != null && location.isSameOrAncestorOf(this);
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public ShippingLocationType getType() { return type; }
    public void setType(ShippingLocationType type) { this.type = type; }
    public ShippingLocation getParent() { return parent; }
    public void setParent(ShippingLocation parent) { this.parent = parent; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocalName() { return localName; }
    public void setLocalName(String localName) { this.localName = localName; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getIso2() { return iso2; }
    public void setIso2(String iso2) { this.iso2 = iso2; }
    public String getIso3() { return iso3; }
    public void setIso3(String iso3) { this.iso3 = iso3; }
    public String getExternalCode() { return externalCode; }
    public void setExternalCode(String externalCode) { this.externalCode = externalCode; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
    public LocalDateTime getModified() { return modified; }
    public void setModified(LocalDateTime modified) { this.modified = modified; }
}
