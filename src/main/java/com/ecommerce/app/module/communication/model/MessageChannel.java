package com.ecommerce.app.module.communication.model;

public enum MessageChannel {
    EMAIL("Email"),
    SMS("SMS"),
    WHATSAPP("WhatsApp"),
    PUSH("Push"),
    IN_APP("In-App");

    private final String displayName;

    MessageChannel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
