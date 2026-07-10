package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.CommunicationSetting;
import com.ecommerce.app.module.communication.model.MessageJob;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MessageJobRetryScheduler {

    private final MessageJobService jobService;
    private final CommunicationSettingsService settingsService;
    private final MessageTemplateService templateService;
    private final CommunicationPreferenceService preferenceService;
    private final MessageDispatchService dispatchService;

    public MessageJobRetryScheduler(
            MessageJobService jobService,
            CommunicationSettingsService settingsService,
            MessageTemplateService templateService,
            CommunicationPreferenceService preferenceService,
            MessageDispatchService dispatchService) {
        this.jobService = jobService;
        this.settingsService = settingsService;
        this.templateService = templateService;
        this.preferenceService = preferenceService;
        this.dispatchService = dispatchService;
    }

    @Scheduled(fixedDelayString = "${communication.retry.fixed-delay-ms:60000}")
    public void processDueJobs() {
        CommunicationSetting settings = settingsService.getSettings();
        if (!settings.isSchedulerEnabled()) {
            return;
        }

        List<MessageJob> dueJobs = jobService.findDueJobs(settings.getMaxRetryCount());
        for (MessageJob job : dueJobs) {
            processJob(job, settings);
        }
    }

    private void processJob(MessageJob job, CommunicationSetting settings) {
        if (job.getStatus() == MessageStatus.FAILED && job.getRetryCount() >= settings.getMaxRetryCount()) {
            jobService.cancelRetry(job, "Retry limit reached. Last failure: " + nullSafe(job.getFailedReason()));
            return;
        }

        MessageDispatchRequest request = jobService.toRequest(job);
        preferenceService.prepareForSend(request);
        if (!preferenceService.canSend(request)) {
            jobService.markSkipped(job, "Recipient preference blocks this message.");
            return;
        }

        RenderedMessage rendered = templateService.render(request);
        MessageProvider preferredProvider = job.getProvider();

        jobService.markProcessing(job);
        var result = dispatchService.sendDirect(request, rendered, preferredProvider);
        if (result.isSuccess() && MessageStatus.SENT.name().equals(result.getStatus())) {
            jobService.markSent(job, rendered, preferredProvider);
            return;
        }
        if (result.isSuccess() && MessageStatus.SKIPPED.name().equals(result.getStatus())) {
            jobService.markSkipped(job, nullSafe(result.getResponseMessage()));
            return;
        }

        if (job.getRetryCount() + 1 >= settings.getMaxRetryCount()) {
            jobService.cancelRetry(job, "Retry limit reached. Last failure: " + nullSafe(result.getFailedReason()));
        } else {
            jobService.markFailed(job, result.getFailedReason(), settings.getRetryDelayMinutes());
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
