package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.model.CommunicationSetting;
import com.ecommerce.app.module.communication.model.DeliveryMode;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageRoutingRule;
import com.ecommerce.app.module.communication.repository.MessageRoutingRuleRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageRoutingService {

    private final MessageRoutingRuleRepository repository;
    private final CommunicationSettingsService settingsService;

    public MessageRoutingService(MessageRoutingRuleRepository repository, CommunicationSettingsService settingsService) {
        this.repository = repository;
        this.settingsService = settingsService;
    }

    @Transactional(readOnly = true)
    public RoutingDecision resolve(MessageEventType eventType, MessageChannel channel, int volume) {
        int safeVolume = Math.max(volume, 1);
        Optional<MessageRoutingRule> rule = repository.findByEventTypeAndChannelAndActiveTrueOrderByMinVolumeDescIdDesc(eventType, channel)
                .stream()
                .filter(candidate -> candidate.matchesVolume(safeVolume))
                .findFirst();

        if (rule.isPresent()) {
            MessageRoutingRule routingRule = rule.get();
            return new RoutingDecision(routingRule.getDeliveryMode(), Optional.ofNullable(routingRule.getProvider()));
        }

        CommunicationSetting settings = settingsService.getSettings();
        DeliveryMode mode = safeVolume <= settings.getDirectVolumeThreshold()
                ? DeliveryMode.DIRECT
                : DeliveryMode.DATABASE_QUEUE;
        return new RoutingDecision(mode, Optional.empty());
    }

    public static class RoutingDecision {

        private final DeliveryMode deliveryMode;
        private final Optional<com.ecommerce.app.module.communication.model.MessageProvider> provider;

        public RoutingDecision(DeliveryMode deliveryMode, Optional<com.ecommerce.app.module.communication.model.MessageProvider> provider) {
            this.deliveryMode = deliveryMode;
            this.provider = provider;
        }

        public DeliveryMode getDeliveryMode() {
            return deliveryMode;
        }

        public Optional<com.ecommerce.app.module.communication.model.MessageProvider> getProvider() {
            return provider;
        }
    }
}
