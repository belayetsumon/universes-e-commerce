package com.ecommerce.app.adminvendor.dto;

import com.ecommerce.app.vendor.model.VendorStatusEnum;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class AdminVendorFilter {

    private String q;
    private VendorStatusEnum status;
    private Boolean emailVerified;
    private Boolean mobileVerified;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdTo;

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = trimToNull(q);
    }

    public VendorStatusEnum getStatus() {
        return status;
    }

    public void setStatus(VendorStatusEnum status) {
        this.status = status;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getMobileVerified() {
        return mobileVerified;
    }

    public void setMobileVerified(Boolean mobileVerified) {
        this.mobileVerified = mobileVerified;
    }

    public LocalDate getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(LocalDate createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDate getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(LocalDate createdTo) {
        this.createdTo = createdTo;
    }

    public boolean hasActiveFilters() {
        return q != null
                || status != null
                || emailVerified != null
                || mobileVerified != null
                || createdFrom != null
                || createdTo != null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
