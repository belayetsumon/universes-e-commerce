package com.ecommerce.app.module.settings.form;

import com.ecommerce.app.module.settings.model.GlobalSettings;

public class PolicySettingsForm {

    private Long version;
    private String aboutUs;
    private String contactUsContent;
    private String helpPageContent;
    private String termsOfUseContent;
    private String termsAndConditions;
    private String privacyPolicy;
    private String paymentMethodsContent;
    private String returnPolicy;
    private String refundPolicy;
    private String shippingPolicy;

    public static PolicySettingsForm from(GlobalSettings settings) {
        PolicySettingsForm form = new PolicySettingsForm();
        form.setVersion(settings.getVersion());
        form.setAboutUs(settings.getAboutUs());
        form.setContactUsContent(settings.getContactUsContent());
        form.setHelpPageContent(settings.getHelpPageContent());
        form.setTermsOfUseContent(settings.getTermsOfUseContent());
        form.setTermsAndConditions(settings.getTermsAndConditions());
        form.setPrivacyPolicy(settings.getPrivacyPolicy());
        form.setPaymentMethodsContent(settings.getPaymentMethodsContent());
        form.setReturnPolicy(settings.getReturnPolicy());
        form.setRefundPolicy(settings.getRefundPolicy());
        form.setShippingPolicy(settings.getShippingPolicy());
        return form;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getAboutUs() {
        return aboutUs;
    }

    public void setAboutUs(String aboutUs) {
        this.aboutUs = aboutUs;
    }

    public String getContactUsContent() {
        return contactUsContent;
    }

    public void setContactUsContent(String contactUsContent) {
        this.contactUsContent = contactUsContent;
    }

    public String getHelpPageContent() {
        return helpPageContent;
    }

    public void setHelpPageContent(String helpPageContent) {
        this.helpPageContent = helpPageContent;
    }

    public String getTermsOfUseContent() {
        return termsOfUseContent;
    }

    public void setTermsOfUseContent(String termsOfUseContent) {
        this.termsOfUseContent = termsOfUseContent;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public String getPaymentMethodsContent() {
        return paymentMethodsContent;
    }

    public void setPaymentMethodsContent(String paymentMethodsContent) {
        this.paymentMethodsContent = paymentMethodsContent;
    }

    public String getReturnPolicy() {
        return returnPolicy;
    }

    public void setReturnPolicy(String returnPolicy) {
        this.returnPolicy = returnPolicy;
    }

    public String getRefundPolicy() {
        return refundPolicy;
    }

    public void setRefundPolicy(String refundPolicy) {
        this.refundPolicy = refundPolicy;
    }

    public String getShippingPolicy() {
        return shippingPolicy;
    }

    public void setShippingPolicy(String shippingPolicy) {
        this.shippingPolicy = shippingPolicy;
    }
}
