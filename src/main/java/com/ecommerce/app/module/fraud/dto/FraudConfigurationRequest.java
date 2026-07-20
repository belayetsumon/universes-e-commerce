package com.ecommerce.app.module.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FraudConfigurationRequest {

    @NotBlank(message = "Configuration key is required.")
    @Size(max = 120, message = "Configuration key must be 120 characters or less.")
    private String configKey;

    @NotBlank(message = "Configuration value is required.")
    private String configValue;

    @Size(max = 500, message = "Description must be 500 characters or less.")
    private String description;

    private boolean active = true;

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
