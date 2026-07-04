package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;

public class PaymentSettingsForm {

    private Long version;
    private Boolean codEnabled;
    private Boolean onlinePaymentEnabled;
    private Boolean partialPaymentEnabled;
    private Boolean emiEnabled;

    public static PaymentSettingsForm from(GlobalSettings settings) {
        PaymentSettingsForm form = new PaymentSettingsForm();
        form.setVersion(settings.getVersion());
        form.setCodEnabled(settings.getCodEnabled());
        form.setOnlinePaymentEnabled(settings.getOnlinePaymentEnabled());
        form.setPartialPaymentEnabled(settings.getPartialPaymentEnabled());
        form.setEmiEnabled(settings.getEmiEnabled());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean getCodEnabled() {
        return codEnabled;
    }

    public void setCodEnabled(Boolean codEnabled) {
        this.codEnabled = codEnabled;
    }

    public Boolean getOnlinePaymentEnabled() {
        return onlinePaymentEnabled;
    }

    public void setOnlinePaymentEnabled(Boolean onlinePaymentEnabled) {
        this.onlinePaymentEnabled = onlinePaymentEnabled;
    }

    public Boolean getPartialPaymentEnabled() {
        return partialPaymentEnabled;
    }

    public void setPartialPaymentEnabled(Boolean partialPaymentEnabled) {
        this.partialPaymentEnabled = partialPaymentEnabled;
    }

    public Boolean getEmiEnabled() {
        return emiEnabled;
    }

    public void setEmiEnabled(Boolean emiEnabled) {
        this.emiEnabled = emiEnabled;
    }
}
