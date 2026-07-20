package com.ecommerce.app.module.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FraudCaseResolveRequest {

    @NotBlank(message = "Resolution is required.")
    @Size(max = 120, message = "Resolution must be 120 characters or less.")
    private String resolution;

    @NotBlank(message = "Resolution reason is required.")
    @Size(max = 1000, message = "Resolution reason must be 1000 characters or less.")
    private String resolutionReason;

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getResolutionReason() { return resolutionReason; }
    public void setResolutionReason(String resolutionReason) { this.resolutionReason = resolutionReason; }
}
