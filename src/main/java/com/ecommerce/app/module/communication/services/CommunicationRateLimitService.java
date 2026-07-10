package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.model.CommunicationSetting;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageStatus;
import com.ecommerce.app.module.communication.repository.MessageLogRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommunicationRateLimitService {

    private final MessageLogRepository logRepository;
    private final CommunicationSettingsService settingsService;

    public CommunicationRateLimitService(MessageLogRepository logRepository, CommunicationSettingsService settingsService) {
        this.logRepository = logRepository;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public String blockedReason(MessageDispatchRequest request, MessageProvider provider) {
        CommunicationSetting settings = settingsService.getSettings();
        if (request != null && request.getChannel() != null && request.getRecipient() != null) {
            long sentToRecipient = logRepository.countByChannelAndRecipientIgnoreCaseAndStatusAndSentAtAfter(
                    request.getChannel(),
                    request.getRecipient(),
                    MessageStatus.SENT,
                    LocalDateTime.now().minusHours(1)
            );
            if (sentToRecipient >= settings.getRecipientHourlyLimit()) {
                return "Recipient hourly communication limit reached.";
            }
        }

        if (provider != null) {
            long sentByProvider = logRepository.countByProviderAndStatusAndSentAtAfter(
                    provider,
                    MessageStatus.SENT,
                    LocalDateTime.now().minusMinutes(1)
            );
            if (sentByProvider >= settings.getProviderPerMinuteLimit()) {
                return "Provider per-minute communication limit reached.";
            }
        }
        return null;
    }
}
