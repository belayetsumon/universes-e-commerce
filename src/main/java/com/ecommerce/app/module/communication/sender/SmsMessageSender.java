package com.ecommerce.app.module.communication.sender;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageProvider;
import org.springframework.stereotype.Service;

@Service
public class SmsMessageSender implements MessageChannelSender {

    private final ProviderHttpClient providerHttpClient;

    public SmsMessageSender(ProviderHttpClient providerHttpClient) {
        this.providerHttpClient = providerHttpClient;
    }

    @Override
    public boolean supports(MessageChannel channel) {
        return channel == MessageChannel.SMS;
    }

    @Override
    public CommunicationSendResult send(MessageDispatchRequest request, MessageProvider provider, RenderedMessage renderedMessage) {
        return providerHttpClient.post(request, provider, renderedMessage, "SMS_SENT");
    }
}
