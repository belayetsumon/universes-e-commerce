package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.model.CommunicationPreference;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.communication.repository.CommunicationPreferenceRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunicationPreferenceService {

    private final CommunicationPreferenceRepository repository;

    public CommunicationPreferenceService(CommunicationPreferenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void prepareForSend(MessageDispatchRequest request) {
        if (request == null || request.getMessageType() != MessageType.MARKETING) {
            return;
        }
        CommunicationPreference preference = ensurePreference(request.getRecipient(), request.getChannel());
        request.getVariables().putIfAbsent("unsubscribeToken", preference.getUnsubscribeToken());
        request.getVariables().putIfAbsent("unsubscribeUrl", "/public/communication/unsubscribe?token=" + preference.getUnsubscribeToken());
    }
    @Transactional(readOnly = true)
    public boolean canSend(MessageDispatchRequest request) {
        if (request == null || request.getChannel() == null || clean(request.getRecipient()) == null) {
            return false;
        }

        Optional<CommunicationPreference> preference = findPreference(request);
        if (preference.isEmpty()) {
            return true;
        }

        CommunicationPreference current = preference.get();
        if (request.getMessageType() == MessageType.MARKETING) {
            return current.isMarketingEnabled() && current.getUnsubscribedAt() == null;
        }
        return current.isTransactionalEnabled();
    }

    @Transactional
    public CommunicationPreference unsubscribeMarketing(String unsubscribeToken) {
        CommunicationPreference preference = repository.findByUnsubscribeToken(unsubscribeToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid unsubscribe token."));
        preference.setMarketingEnabled(false);
        preference.setUnsubscribedAt(LocalDateTime.now());
        return repository.save(preference);
    }

    @Transactional
    public CommunicationPreference ensurePreference(String recipient, MessageChannel channel) {
        String cleanedRecipient = clean(recipient);
        if (cleanedRecipient == null || channel == null) {
            throw new IllegalArgumentException("Recipient and channel are required.");
        }
        return repository.findByRecipientIgnoreCaseAndChannel(cleanedRecipient, channel)
                .orElseGet(() -> {
                    CommunicationPreference preference = new CommunicationPreference();
                    preference.setRecipient(cleanedRecipient);
                    preference.setChannel(channel);
                    preference.setUnsubscribeToken(UUID.randomUUID().toString());
                    return repository.save(preference);
                });
    }

    private Optional<CommunicationPreference> findPreference(MessageDispatchRequest request) {
        if (request.getUser() != null) {
            Optional<CommunicationPreference> byUser = repository.findByUserAndChannel(request.getUser(), request.getChannel());
            if (byUser.isPresent()) {
                return byUser;
            }
        }
        return repository.findByRecipientIgnoreCaseAndChannel(request.getRecipient().trim(), request.getChannel());
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

