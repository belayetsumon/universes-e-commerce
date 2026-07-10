package com.ecommerce.app.module.communication.dto;

import com.ecommerce.app.module.communication.model.ManualRecipientType;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.user.model.Users;

public class ManualRecipient {

    private final ManualRecipientType recipientType;
    private final Long recipientReferenceId;
    private final Users user;
    private final String displayName;
    private final String email;
    private final String mobile;

    public ManualRecipient(ManualRecipientType recipientType, Long recipientReferenceId, Users user, String displayName, String email, String mobile) {
        this.recipientType = recipientType;
        this.recipientReferenceId = recipientReferenceId;
        this.user = user;
        this.displayName = displayName;
        this.email = email;
        this.mobile = mobile;
    }

    public ManualRecipientType getRecipientType() {
        return recipientType;
    }

    public Long getRecipientReferenceId() {
        return recipientReferenceId;
    }

    public Users getUser() {
        return user;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String addressFor(MessageChannel channel) {
        if (channel == MessageChannel.EMAIL) {
            return clean(email);
        }
        if (channel == MessageChannel.SMS || channel == MessageChannel.WHATSAPP) {
            return clean(mobile);
        }
        if ((channel == MessageChannel.IN_APP || channel == MessageChannel.PUSH) && user != null && user.getId() != null) {
            return "user:" + user.getId();
        }
        return null;
    }

    public String stableKey() {
        return recipientType + ":" + String.valueOf(recipientReferenceId);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
