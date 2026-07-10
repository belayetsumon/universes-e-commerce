package com.ecommerce.app.module.communication.dto;

public class CommunicationSendResult {

    private boolean success;
    private String status;
    private String responseCode;
    private String responseMessage;
    private String failedReason;
    private Long jobId;

    public CommunicationSendResult() {
    }

    private CommunicationSendResult(boolean success, String status, String responseCode, String responseMessage, String failedReason) {
        this.success = success;
        this.status = status;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.failedReason = failedReason;
    }

    public static CommunicationSendResult sent(String responseCode, String responseMessage) {
        return new CommunicationSendResult(true, "SENT", responseCode, responseMessage, null);
    }

    public static CommunicationSendResult failed(String responseCode, String failedReason) {
        return new CommunicationSendResult(false, "FAILED", responseCode, null, failedReason);
    }

    public static CommunicationSendResult skipped(String responseCode, String responseMessage) {
        return new CommunicationSendResult(true, "SKIPPED", responseCode, responseMessage, null);
    }

    public static CommunicationSendResult queued(Long jobId, String responseMessage) {
        CommunicationSendResult result = new CommunicationSendResult(true, "QUEUED", "MESSAGE_QUEUED", responseMessage, null);
        result.setJobId(jobId);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
}
