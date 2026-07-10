package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.repository.MessageTemplateRepository;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageTemplateService {

    private final MessageTemplateRepository repository;
    private final CommunicationSettingsService settingsService;

    public MessageTemplateService(MessageTemplateRepository repository, CommunicationSettingsService settingsService) {
        this.repository = repository;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public RenderedMessage render(MessageDispatchRequest request) {
        String language = clean(request.getLanguage());
        if (language == null) {
            language = settingsService.getSettings().getDefaultLanguage();
        }

        return repository.findFirstByEventTypeAndChannelAndLanguageIgnoreCaseAndStatusOrderByUpdatedAtDescIdDesc(
                request.getEventType(),
                request.getChannel(),
                language,
                MessageStatus.ACTIVE
        ).map(template -> new RenderedMessage(
                renderVariables(template.getSubject(), request.getVariables()),
                renderVariables(template.getBody(), request.getVariables()),
                false,
                template.getId(),
                template.getVersion()
        )).orElseGet(() -> fallbackMessage(request));
    }

    private RenderedMessage fallbackMessage(MessageDispatchRequest request) {
        String subject = clean(request.getSubject());
        String body = clean(request.getBody());
        if (subject == null) {
            subject = request.getEventType() == null ? "Notification" : request.getEventType().name().replace('_', ' ');
        }
        if (body == null) {
            body = "A new update is available.";
        }
        return new RenderedMessage(renderVariables(subject, request.getVariables()), renderVariables(body, request.getVariables()), true);
    }

    private String renderVariables(String value, Map<String, Object> variables) {
        if (value == null || variables == null || variables.isEmpty()) {
            return value;
        }
        String rendered = value;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String token = "{{" + entry.getKey() + "}}";
            String replacement = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            rendered = rendered.replace(token, replacement);
        }
        return rendered;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
