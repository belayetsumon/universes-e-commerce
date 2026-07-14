package com.ecommerce.app.module.marketing.services;

import com.ecommerce.app.module.marketing.model.TrackingDeliveryEvent;
import com.ecommerce.app.module.marketing.model.TrackingDeliveryStatus;
import com.ecommerce.app.module.marketing.model.TrackingProvider;
import com.ecommerce.app.module.marketing.repository.TrackingDeliveryEventRepository;
import com.ecommerce.app.module.settings.model.GlobalSettings;
import com.ecommerce.app.module.settings.services.GlobalSettingsService;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacebookConversionApiService {

    private static final Set<String> SUPPORTED_EVENTS = Set.of(
            "Purchase",
            "CompleteRegistration",
            "Lead",
            "AddToCart",
            "InitiateCheckout"
    );

    private final GlobalSettingsService globalSettingsService;
    private final TrackingDeliveryEventRepository repository;

    public FacebookConversionApiService(
            GlobalSettingsService globalSettingsService,
            TrackingDeliveryEventRepository repository
    ) {
        this.globalSettingsService = globalSettingsService;
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void queueEvent(String eventName, String eventId, String entityReference, String sanitizedPayloadJson) {
        GlobalSettings settings = globalSettingsService.getActiveSettings();
        if (!Boolean.TRUE.equals(settings.getFacebookConversionApiEnabled())
                || settings.getFacebookConversionApiAccessToken() == null
                || !SUPPORTED_EVENTS.contains(eventName)
                || eventId == null
                || eventId.isBlank()) {
            return;
        }
        repository.findByProviderAndEventId(TrackingProvider.FACEBOOK_CONVERSION_API, eventId)
                .orElseGet(() -> {
                    TrackingDeliveryEvent event = new TrackingDeliveryEvent();
                    event.setProvider(TrackingProvider.FACEBOOK_CONVERSION_API);
                    event.setEventName(eventName);
                    event.setEventId(eventId);
                    event.setEntityReference(entityReference);
                    event.setPayload(sanitizedPayloadJson);
                    event.setDeliveryStatus(TrackingDeliveryStatus.PENDING);
                    event.setAttemptCount(0);
                    event.setNextAttemptAt(Instant.now());
                    return repository.save(event);
                });
    }
}
