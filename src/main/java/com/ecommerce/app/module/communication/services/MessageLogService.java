package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.model.MessageLog;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.repository.MessageLogRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageLogService {

    private final MessageLogRepository repository;

    public MessageLogService(MessageLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MessageLog record(MessageDispatchRequest request, MessageProvider provider, CommunicationSendResult result) {
        MessageLog log = new MessageLog();
        log.setEventType(request.getEventType());
        log.setChannel(request.getChannel());
        log.setMessageType(request.getMessageType());
        log.setRecipient(request.getRecipient());
        log.setProvider(provider);
        log.setTemplateId(request.getTemplateId());
        log.setTemplateVersion(request.getTemplateVersion());
        log.setIdempotencyKey(request.getIdempotencyKey());
        log.setStatus(toStatus(result.getStatus()));
        log.setResponseCode(result.getResponseCode());
        log.setResponseMessage(result.getResponseMessage() != null ? result.getResponseMessage() : result.getFailedReason());
        log.setSentAt(LocalDateTime.now());
        return repository.save(log);
    }

    private MessageStatus toStatus(String status) {
        if (status == null || status.isBlank()) {
            return MessageStatus.FAILED;
        }
        try {
            return MessageStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            return MessageStatus.FAILED;
        }
    }
}
