package com.ecommerce.app.module.communication.dto;

import java.util.ArrayList;
import java.util.List;

public class ManualMessageResponse {

    private int recipientCount;
    private int attemptedDeliveries;
    private int sentCount;
    private int queuedCount;
    private int skippedCount;
    private int failedCount;
    private final List<String> errors = new ArrayList<>();

    public void addResult(CommunicationSendResult result) {
        attemptedDeliveries++;
        if (result == null) {
            failedCount++;
            return;
        }
        String status = result.getStatus();
        if ("SENT".equals(status)) {
            sentCount++;
        } else if ("QUEUED".equals(status)) {
            queuedCount++;
        } else if ("SKIPPED".equals(status)) {
            skippedCount++;
        } else {
            failedCount++;
            if (result.getFailedReason() != null && !result.getFailedReason().isBlank()) {
                errors.add(result.getFailedReason());
            }
        }
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
    }

    public int getAttemptedDeliveries() {
        return attemptedDeliveries;
    }

    public int getSentCount() {
        return sentCount;
    }

    public int getQueuedCount() {
        return queuedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public List<String> getErrors() {
        return errors;
    }
}
