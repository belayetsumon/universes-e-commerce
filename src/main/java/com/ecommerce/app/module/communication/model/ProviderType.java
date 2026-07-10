package com.ecommerce.app.module.communication.model;

public enum ProviderType {
    SMTP("SMTP"),
    SMS_GATEWAY("SMS Gateway"),
    WHATSAPP_API("WhatsApp API"),
    FCM("Firebase Cloud Messaging"),
    INTERNAL("Internal"),
    WEBHOOK("Webhook");

    private final String displayName;

    ProviderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
