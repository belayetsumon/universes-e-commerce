package com.ecommerce.app.module.fraud.dto;

public class FraudConfigurationAdminRow {

    private Long id;
    private String configKey;
    private String displayValue;
    private String description;
    private boolean active;
    private boolean sensitive;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getDisplayValue() { return displayValue; }
    public void setDisplayValue(String displayValue) { this.displayValue = displayValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }
}
