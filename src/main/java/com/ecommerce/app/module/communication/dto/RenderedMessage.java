package com.ecommerce.app.module.communication.dto;

public class RenderedMessage {

    private final String subject;
    private final String body;
    private final boolean fallbackUsed;
    private final Long templateId;
    private final Long templateVersion;

    public RenderedMessage(String subject, String body, boolean fallbackUsed) {
        this(subject, body, fallbackUsed, null, null);
    }

    public RenderedMessage(String subject, String body, boolean fallbackUsed, Long templateId, Long templateVersion) {
        this.subject = subject;
        this.body = body;
        this.fallbackUsed = fallbackUsed;
        this.templateId = templateId;
        this.templateVersion = templateVersion;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public Long getTemplateVersion() {
        return templateVersion;
    }
}
