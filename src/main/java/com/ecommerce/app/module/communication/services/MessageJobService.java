package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.DeliveryMode;
import com.ecommerce.app.module.communication.model.MessageJob;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.communication.repository.MessageJobRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageJobService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final MessageJobRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageJobService(MessageJobRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MessageJob enqueueRequest(MessageDispatchRequest request) {
        String idempotencyKey = normalizeIdempotencyKey(request);
        Optional<MessageJob> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        MessageJob job = new MessageJob();
        job.setEventType(request.getEventType());
        job.setChannel(request.getChannel());
        job.setMessageType(request.getMessageType());
        job.setUser(request.getUser());
        job.setRecipient(request.getRecipient());
        job.setSubject(defaultText(request.getSubject(), request.getEventType() == null ? "Notification" : request.getEventType().name().replace('_', ' ')));
        job.setBody(defaultText(request.getBody(), "A new update is available."));
        job.setLanguage(request.getLanguage());
        job.setVariablesJson(writeVariables(request.getVariables()));
        job.setPayloadJson(request.getPayloadJson());
        job.setIdempotencyKey(idempotencyKey);
        job.setTemplateId(request.getTemplateId());
        job.setTemplateVersion(request.getTemplateVersion());
        job.setStatus(MessageStatus.QUEUED);
        job.setScheduledAt(LocalDateTime.now());
        return repository.save(job);
    }

    @Transactional
    public MessageJob queue(MessageDispatchRequest request, RenderedMessage rendered, MessageProvider provider, DeliveryMode deliveryMode) {
        String idempotencyKey = normalizeIdempotencyKey(request);
        Optional<MessageJob> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        MessageJob job = new MessageJob();
        job.setEventType(request.getEventType());
        job.setChannel(request.getChannel());
        job.setMessageType(request.getMessageType());
        job.setUser(request.getUser());
        job.setRecipient(request.getRecipient());
        job.setSubject(rendered.getSubject());
        job.setBody(rendered.getBody());
        job.setLanguage(request.getLanguage());
        job.setVariablesJson(writeVariables(request.getVariables()));
        job.setPayloadJson(request.getPayloadJson());
        job.setIdempotencyKey(idempotencyKey);
        job.setTemplateId(rendered.getTemplateId());
        job.setTemplateVersion(rendered.getTemplateVersion());
        job.setProvider(provider);
        job.setDeliveryMode(deliveryMode);
        job.setStatus(MessageStatus.QUEUED);
        job.setScheduledAt(LocalDateTime.now());
        return repository.save(job);
    }

    @Transactional
    public MessageJob fail(MessageDispatchRequest request, RenderedMessage rendered, MessageProvider provider, DeliveryMode deliveryMode, String reason) {
        MessageJob job = queue(request, rendered, provider, deliveryMode);
        job.setStatus(MessageStatus.FAILED);
        job.setFailedReason(reason);
        return repository.save(job);
    }

    @Transactional(readOnly = true)
    public Optional<MessageJob> findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return repository.findByIdempotencyKey(idempotencyKey.trim());
    }

    @Transactional(readOnly = true)
    public List<MessageJob> findDueJobs(int maxRetryCount) {
        return repository.findRetryableJobs(LocalDateTime.now(), Math.max(maxRetryCount, 0), PageRequest.of(0, 25));
    }

    @Transactional
    public MessageJob markProcessing(MessageJob job) {
        job.setStatus(MessageStatus.PROCESSING);
        return repository.save(job);
    }

    @Transactional
    public MessageJob markSent(MessageJob job, RenderedMessage rendered, MessageProvider provider) {
        applyRendered(job, rendered, provider);
        job.setStatus(MessageStatus.SENT);
        job.setSentAt(LocalDateTime.now());
        job.setFailedReason(null);
        return repository.save(job);
    }

    @Transactional
    public MessageJob markSkipped(MessageJob job, String reason) {
        job.setStatus(MessageStatus.SKIPPED);
        job.setSentAt(LocalDateTime.now());
        job.setFailedReason(reason);
        return repository.save(job);
    }

    @Transactional
    public MessageJob markFailed(MessageJob job, String failedReason, int retryDelayMinutes) {
        job.setStatus(MessageStatus.FAILED);
        job.setRetryCount(job.getRetryCount() + 1);
        job.setFailedReason(failedReason);
        job.setScheduledAt(LocalDateTime.now().plusMinutes(Math.max(retryDelayMinutes, 1)));
        return repository.save(job);
    }

    @Transactional
    public MessageJob cancelRetry(MessageJob job, String failedReason) {
        job.setStatus(MessageStatus.FAILED);
        job.setFailedReason(failedReason);
        return repository.save(job);
    }

    public MessageDispatchRequest toRequest(MessageJob job) {
        MessageDispatchRequest request = new MessageDispatchRequest();
        request.setEventType(job.getEventType());
        request.setChannel(job.getChannel());
        request.setRecipient(job.getRecipient());
        request.setSubject(job.getSubject());
        request.setBody(job.getBody());
        request.setLanguage(job.getLanguage());
        request.setPayloadJson(job.getPayloadJson());
        request.setIdempotencyKey(job.getIdempotencyKey());
        request.setTemplateId(job.getTemplateId());
        request.setTemplateVersion(job.getTemplateVersion());
        request.setMessageType(job.getMessageType() == null ? MessageType.TRANSACTIONAL : job.getMessageType());
        request.setUser(job.getUser());
        request.setVariables(readVariables(job.getVariablesJson()));
        return request;
    }

    public String normalizeIdempotencyKey(MessageDispatchRequest request) {
        String existing = request.getIdempotencyKey();
        if (existing != null && !existing.isBlank()) {
            String cleaned = existing.trim();
            request.setIdempotencyKey(cleaned);
            return cleaned;
        }
        String source = String.valueOf(request.getEventType()) + "|"
                + String.valueOf(request.getChannel()) + "|"
                + clean(request.getRecipient()) + "|"
                + String.valueOf(request.getMessageType()) + "|"
                + writeVariables(request.getVariables()) + "|"
                + clean(request.getPayloadJson());
        String generated = sha256(source);
        request.setIdempotencyKey(generated);
        return generated;
    }

    private void applyRendered(MessageJob job, RenderedMessage rendered, MessageProvider provider) {
        if (rendered != null) {
            job.setSubject(rendered.getSubject());
            job.setBody(rendered.getBody());
            job.setTemplateId(rendered.getTemplateId());
            job.setTemplateVersion(rendered.getTemplateVersion());
        }
        job.setProvider(provider);
    }

    private Map<String, Object> readVariables(String variablesJson) {
        if (variablesJson == null || variablesJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(variablesJson, MAP_TYPE);
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    private String writeVariables(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : encoded) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (Exception ex) {
            return String.valueOf(value.hashCode());
        }
    }

    private String defaultText(String value, String fallback) {
        String cleaned = clean(value);
        return cleaned == null ? fallback : cleaned;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
