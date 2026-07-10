package com.ecommerce.app.module.communication.sender;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.services.CommunicationNotificationService;
import org.springframework.stereotype.Service;

@Service
public class InAppMessageSender implements MessageChannelSender {

    private final CommunicationNotificationService notificationService;

    public InAppMessageSender(CommunicationNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public boolean supports(MessageChannel channel) {
        return channel == MessageChannel.IN_APP;
    }

    @Override
    public CommunicationSendResult send(MessageDispatchRequest request, MessageProvider provider, RenderedMessage renderedMessage) {
        try {
            notificationService.createRecipientFromDispatch(request, renderedMessage);
            return CommunicationSendResult.sent("IN_APP_RECORDED", "In-app notification recorded successfully.");
        } catch (Exception ex) {
            return CommunicationSendResult.failed("IN_APP_RECORD_FAILED", ex.getMessage());
        }
    }
}
