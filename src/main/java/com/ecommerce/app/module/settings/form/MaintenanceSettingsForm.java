package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;
import jakarta.validation.constraints.Size;

public class MaintenanceSettingsForm {

    private Long version;
    private Boolean maintenanceMode;

    @Size(max = 1000, message = "Maintenance message must be 1000 characters or fewer.")
    private String maintenanceMessage;

    private Boolean registrationEnabled;
    private Boolean vendorRegistrationEnabled;

    public static MaintenanceSettingsForm from(GlobalSettings settings) {
        MaintenanceSettingsForm form = new MaintenanceSettingsForm();
        form.setVersion(settings.getVersion());
        form.setMaintenanceMode(settings.getMaintenanceMode());
        form.setMaintenanceMessage(settings.getMaintenanceMessage());
        form.setRegistrationEnabled(settings.getRegistrationEnabled());
        form.setVendorRegistrationEnabled(settings.getVendorRegistrationEnabled());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(Boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public void setMaintenanceMessage(String maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    public Boolean getRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(Boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }

    public Boolean getVendorRegistrationEnabled() {
        return vendorRegistrationEnabled;
    }

    public void setVendorRegistrationEnabled(Boolean vendorRegistrationEnabled) {
        this.vendorRegistrationEnabled = vendorRegistrationEnabled;
    }
}
