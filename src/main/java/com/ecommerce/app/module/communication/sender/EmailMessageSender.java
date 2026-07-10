package com.ecommerce.app.module.communication.sender;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailMessageSender implements MessageChannelSender {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public EmailMessageSender(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public boolean supports(MessageChannel channel) {
        return channel == MessageChannel.EMAIL;
    }

    @Override
    public CommunicationSendResult send(MessageDispatchRequest request, MessageProvider provider, RenderedMessage renderedMessage) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return CommunicationSendResult.failed("EMAIL_SENDER_MISSING", "JavaMailSender bean is not available.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (provider.getSenderId() != null && !provider.getSenderId().isBlank()) {
                message.setFrom(provider.getSenderId().trim());
            }
            message.setTo(request.getRecipient());
            message.setSubject(renderedMessage.getSubject());
            message.setText(renderedMessage.getBody());
            mailSender.send(message);
            return CommunicationSendResult.sent("EMAIL_SENT", "Email sent successfully.");
        } catch (Exception ex) {
            return CommunicationSendResult.failed("EMAIL_SEND_FAILED", ex.getMessage());
        }
    }
}
