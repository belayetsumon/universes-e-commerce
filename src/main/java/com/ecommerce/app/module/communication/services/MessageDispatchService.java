package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.DeliveryMode;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageJob;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.sender.MessageChannelSender;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageDispatchService {

    private final MessageTemplateService templateService;
    private final MessageRoutingService routingService;
    private final MessageProviderService providerService;
    private final MessageJobService jobService;
    private final MessageLogService logService;
    private final CommunicationPreferenceService preferenceService;
    private final CommunicationRateLimitService rateLimitService;
    private final List<MessageChannelSender> senders;

    public MessageDispatchService(
            MessageTemplateService templateService,
            MessageRoutingService routingService,
            MessageProviderService providerService,
            MessageJobService jobService,
            MessageLogService logService,
            CommunicationPreferenceService preferenceService,
            CommunicationRateLimitService rateLimitService,
            List<MessageChannelSender> senders) {
        this.templateService = templateService;
        this.routingService = routingService;
        this.providerService = providerService;
        this.jobService = jobService;
        this.logService = logService;
        this.preferenceService = preferenceService;
        this.rateLimitService = rateLimitService;
        this.senders = senders;
    }

    @Transactional
    public CommunicationSendResult dispatch(MessageDispatchRequest request) {
        String validationError = validate(request);
        if (validationError != null) {
            return CommunicationSendResult.failed("MESSAGE_REQUEST_INVALID", validationError);
        }

        try {
            preferenceService.prepareForSend(request);
            String idempotencyKey = jobService.normalizeIdempotencyKey(request);
            Optional<MessageJob> existing = jobService.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return duplicateResult(existing.get());
            }

            if (!preferenceService.canSend(request)) {
                CommunicationSendResult skipped = CommunicationSendResult.skipped("MESSAGE_PREFERENCE_BLOCKED", "Recipient preference blocks this message.");
                logService.record(request, null, skipped);
                return skipped;
            }

            RenderedMessage rendered = templateService.render(request);
            applyTemplateMetadata(request, rendered);
            MessageRoutingService.RoutingDecision routing = routingService.resolve(request.getEventType(), request.getChannel(), request.getVolume());
            MessageProvider provider = routing.getProvider()
                    .or(() -> providerService.findActiveProvider(request.getChannel()))
                    .orElse(null);

            if (provider == null && isProviderRequired(request.getChannel())) {
                CommunicationSendResult failed = CommunicationSendResult.failed("MESSAGE_PROVIDER_MISSING", "No active provider found for " + request.getChannel() + ".");
                jobService.fail(request, rendered, null, routing.getDeliveryMode(), failed.getFailedReason());
                logService.record(request, null, failed);
                return failed;
            }

            if (routing.getDeliveryMode() != DeliveryMode.DIRECT) {
                Long jobId = jobService.queue(request, rendered, provider, routing.getDeliveryMode()).getId();
                return CommunicationSendResult.queued(jobId, "Message queued using " + routing.getDeliveryMode().getDisplayName() + ".");
            }

            return sendDirect(request, rendered, provider);
        } catch (Exception ex) {
            return CommunicationSendResult.failed("MESSAGE_DISPATCH_FAILED", ex.getMessage());
        }
    }

    @Transactional
    public CommunicationSendResult sendDirect(MessageDispatchRequest request, RenderedMessage rendered, MessageProvider preferredProvider) {
        if (!preferenceService.canSend(request)) {
            CommunicationSendResult skipped = CommunicationSendResult.skipped("MESSAGE_PREFERENCE_BLOCKED", "Recipient preference blocks this message.");
            logService.record(request, preferredProvider, skipped);
            return skipped;
        }

        applyTemplateMetadata(request, rendered);
        Optional<MessageChannelSender> sender = senders.stream()
                .filter(candidate -> candidate.supports(request.getChannel()))
                .findFirst();
        if (sender.isEmpty()) {
            CommunicationSendResult failed = CommunicationSendResult.failed("MESSAGE_SENDER_MISSING", "No sender found for " + request.getChannel() + ".");
            logService.record(request, preferredProvider, failed);
            return failed;
        }

        CommunicationSendResult lastFailure = null;
        MessageProvider lastProvider = preferredProvider;
        for (MessageProvider provider : orderedProviders(request, preferredProvider)) {
            lastProvider = provider;
            String blockedReason = rateLimitService.blockedReason(request, provider);
            if (blockedReason != null) {
                lastFailure = CommunicationSendResult.failed("MESSAGE_RATE_LIMITED", blockedReason);
                logService.record(request, provider, lastFailure);
                continue;
            }

            CommunicationSendResult result = sender.get().send(request, provider, rendered);
            logService.record(request, provider, result);
            if (result.isSuccess() && MessageStatus.SENT.name().equals(result.getStatus())) {
                return result;
            }
            lastFailure = result;
        }

        if (lastFailure != null) {
            return lastFailure;
        }

        CommunicationSendResult failed = CommunicationSendResult.failed("MESSAGE_PROVIDER_MISSING", "No active provider found for " + request.getChannel() + ".");
        logService.record(request, lastProvider, failed);
        return failed;
    }

    private List<MessageProvider> orderedProviders(MessageDispatchRequest request, MessageProvider preferredProvider) {
        if (!isProviderRequired(request.getChannel())) {
            return java.util.Collections.singletonList(null);
        }
        Set<Long> seenIds = new LinkedHashSet<>();
        List<MessageProvider> providers = new ArrayList<>();
        if (preferredProvider != null) {
            providers.add(preferredProvider);
            if (preferredProvider.getId() != null) {
                seenIds.add(preferredProvider.getId());
            }
        }
        for (MessageProvider provider : providerService.findActiveProviders(request.getChannel())) {
            if (provider.getId() == null || seenIds.add(provider.getId())) {
                providers.add(provider);
            }
        }
        return providers;
    }

    private boolean isProviderRequired(MessageChannel channel) {
        return channel == MessageChannel.EMAIL
                || channel == MessageChannel.SMS
                || channel == MessageChannel.WHATSAPP
                || channel == MessageChannel.PUSH;
    }

    private CommunicationSendResult duplicateResult(MessageJob job) {
        if (job.getStatus() == MessageStatus.SENT) {
            return CommunicationSendResult.skipped("MESSAGE_DUPLICATE_SENT", "Duplicate message already sent.");
        }
        if (job.getStatus() == MessageStatus.SKIPPED || job.getStatus() == MessageStatus.CANCELLED) {
            return CommunicationSendResult.skipped("MESSAGE_DUPLICATE_SKIPPED", "Duplicate message already closed.");
        }
        return CommunicationSendResult.queued(job.getId(), "Duplicate message already queued.");
    }

    private void applyTemplateMetadata(MessageDispatchRequest request, RenderedMessage rendered) {
        if (rendered == null) {
            return;
        }
        request.setTemplateId(rendered.getTemplateId());
        request.setTemplateVersion(rendered.getTemplateVersion());
    }

    private String validate(MessageDispatchRequest request) {
        if (request == null) {
            return "Message request is required.";
        }
        if (request.getEventType() == null) {
            return "Event type is required.";
        }
        if (request.getChannel() == null) {
            return "Channel is required.";
        }
        if (request.getRecipient() == null || request.getRecipient().trim().isEmpty()) {
            return "Recipient is required.";
        }
        return null;
    }
}

