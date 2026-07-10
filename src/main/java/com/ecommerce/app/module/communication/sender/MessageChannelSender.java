package com.ecommerce.app.module.communication.sender;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageProvider;

public interface MessageChannelSender {

    boolean supports(MessageChannel channel);

    CommunicationSendResult send(MessageDispatchRequest request, MessageProvider provider, RenderedMessage renderedMessage);
}
