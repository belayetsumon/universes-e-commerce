package com.ecommerce.app.module.communication.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.dto.RenderedMessage;
import com.ecommerce.app.module.communication.model.DeliveryMode;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageJob;
import com.ecommerce.app.module.communication.model.MessageProvider;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.communication.sender.MessageChannelSender;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageDispatchServiceCodOtpSecurityTest {

    private MessageTemplateService templateService;
    private MessageRoutingService routingService;
    private MessageProviderService providerService;
    private MessageJobService jobService;
    private MessageLogService logService;
    private CommunicationPreferenceService preferenceService;
    private CommunicationRateLimitService rateLimitService;
    private MessageChannelSender sender;
    private MessageDispatchService service;

    @BeforeEach
    void setUp() {
        templateService = mock(MessageTemplateService.class);
        routingService = mock(MessageRoutingService.class);
        providerService = mock(MessageProviderService.class);
        jobService = mock(MessageJobService.class);
        logService = mock(MessageLogService.class);
        preferenceService = mock(CommunicationPreferenceService.class);
        rateLimitService = mock(CommunicationRateLimitService.class);
        sender = mock(MessageChannelSender.class);
        service = new MessageDispatchService(
                templateService,
                routingService,
                providerService,
                jobService,
                logService,
                preferenceService,
                rateLimitService,
                List.of(sender)
        );
        when(preferenceService.canSend(any(MessageDispatchRequest.class))).thenReturn(true);
    }

    @Test
    void codOtpIgnoresQueueRoutingAndIsDeliveredDirectlyWithoutJobPersistence() {
        MessageDispatchRequest request = otpRequest(MessageEventType.CUSTOMER_COD_OTP);
        RenderedMessage rendered = new RenderedMessage("OTP", "code 123456", false);
        MessageProvider provider = new MessageProvider();
        when(templateService.render(request)).thenReturn(rendered);
        when(routingService.resolve(MessageEventType.CUSTOMER_COD_OTP, MessageChannel.SMS, 1))
                .thenReturn(new MessageRoutingService.RoutingDecision(
                        DeliveryMode.DATABASE_QUEUE, Optional.of(provider)));
        when(providerService.findActiveProviders(MessageChannel.SMS)).thenReturn(List.of());
        when(sender.supports(MessageChannel.SMS)).thenReturn(true);
        when(sender.send(request, provider, rendered))
                .thenReturn(CommunicationSendResult.sent("200", "sent"));

        CommunicationSendResult result = service.dispatch(request);

        assertEquals("SENT", result.getStatus());
        verify(sender).send(request, provider, rendered);
        verify(jobService, never()).normalizeIdempotencyKey(any());
        verify(jobService, never()).findByIdempotencyKey(any());
        verify(jobService, never()).queue(any(), any(), any(), any());
        verify(jobService, never()).fail(any(), any(), any(), any(), any());
    }

    @Test
    void providerMissingFailureDoesNotPersistCodOtpPayload() {
        MessageDispatchRequest request = otpRequest(MessageEventType.GUEST_CHECKOUT_OTP);
        RenderedMessage rendered = new RenderedMessage("OTP", "code 123456", false);
        when(templateService.render(request)).thenReturn(rendered);
        when(routingService.resolve(MessageEventType.GUEST_CHECKOUT_OTP, MessageChannel.SMS, 1))
                .thenReturn(new MessageRoutingService.RoutingDecision(
                        DeliveryMode.DATABASE_QUEUE, Optional.empty()));
        when(providerService.findActiveProvider(MessageChannel.SMS)).thenReturn(Optional.empty());

        CommunicationSendResult result = service.dispatch(request);

        assertFalse(result.isSuccess());
        assertEquals("FAILED", result.getStatus());
        verify(jobService, never()).fail(any(), any(), any(), any(), any());
        verify(jobService, never()).queue(any(), any(), any(), any());
    }

    @Test
    void ordinaryMessagesStillUseConfiguredQueueRouting() {
        MessageDispatchRequest request = otpRequest(MessageEventType.ORDER_PLACED);
        RenderedMessage rendered = new RenderedMessage("Order", "Order placed", false);
        MessageProvider provider = new MessageProvider();
        MessageJob job = mock(MessageJob.class);
        when(job.getId()).thenReturn(77L);
        when(jobService.normalizeIdempotencyKey(request)).thenReturn("order-key");
        when(jobService.findByIdempotencyKey("order-key")).thenReturn(Optional.empty());
        when(templateService.render(request)).thenReturn(rendered);
        when(routingService.resolve(MessageEventType.ORDER_PLACED, MessageChannel.SMS, 1))
                .thenReturn(new MessageRoutingService.RoutingDecision(
                        DeliveryMode.DATABASE_QUEUE, Optional.of(provider)));
        when(jobService.queue(request, rendered, provider, DeliveryMode.DATABASE_QUEUE)).thenReturn(job);

        CommunicationSendResult result = service.dispatch(request);

        assertEquals("QUEUED", result.getStatus());
        assertEquals(77L, result.getJobId());
        verify(jobService).queue(request, rendered, provider, DeliveryMode.DATABASE_QUEUE);
    }

    private MessageDispatchRequest otpRequest(MessageEventType eventType) {
        MessageDispatchRequest request = new MessageDispatchRequest();
        request.setEventType(eventType);
        request.setChannel(MessageChannel.SMS);
        request.setMessageType(MessageType.TRANSACTIONAL);
        request.setRecipient("8801712345678");
        request.setBody("code 123456");
        return request;
    }
}
