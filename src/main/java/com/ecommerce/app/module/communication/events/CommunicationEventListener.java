package com.ecommerce.app.module.communication.events;

import com.ecommerce.app.module.communication.services.MessageJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CommunicationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationEventListener.class);

    private final MessageJobService jobService;

    public CommunicationEventListener(MessageJobService jobService) {
        this.jobService = jobService;
    }

    @EventListener
    public void onCommunicationRequested(CommunicationRequestedEvent event) {
        if (event == null) {
            return;
        }

        try {
            jobService.enqueueRequest(event.toDispatchRequest());
        } catch (Exception ex) {
            LOGGER.warn("Communication outbox enqueue failed for eventType={} channel={} recipient={}",
                    event.getEventType(), event.getChannel(), event.getRecipient(), ex);
            throw new IllegalStateException("Communication outbox enqueue failed.", ex);
        }
    }
}

