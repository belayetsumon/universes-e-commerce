package com.ecommerce.app.module.communication.services;

import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.ManualCommunicationActor;
import com.ecommerce.app.module.communication.dto.ManualMessageRequest;
import com.ecommerce.app.module.communication.dto.ManualMessageResponse;
import com.ecommerce.app.module.communication.dto.ManualRecipient;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.communication.model.ReceiverType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManualCommunicationDispatchService {

    private final ManualCommunicationPermissionService permissionService;
    private final ManualCommunicationAudienceResolverService audienceResolverService;
    private final MessageDispatchService messageDispatchService;
    private final MessageLogService messageLogService;
    private final CommunicationNotificationService notificationService;
    private final ObjectMapper objectMapper;

    public ManualCommunicationDispatchService(
            ManualCommunicationPermissionService permissionService,
            ManualCommunicationAudienceResolverService audienceResolverService,
            MessageDispatchService messageDispatchService,
            MessageLogService messageLogService,
            CommunicationNotificationService notificationService,
            ObjectMapper objectMapper) {
        this.permissionService = permissionService;
        this.audienceResolverService = audienceResolverService;
        this.messageDispatchService = messageDispatchService;
        this.messageLogService = messageLogService;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ManualMessageResponse send(ManualCommunicationActor actor, ManualMessageRequest request) {
        permissionService.validate(actor, request);
        String batchId = clean(request.getBatchId());
        if (batchId == null) {
            batchId = UUID.randomUUID().toString();
            request.setBatchId(batchId);
        }

        List<ManualRecipient> recipients = audienceResolverService.resolve(actor, request);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("No permitted recipients were found for this audience.");
        }

        ManualMessageResponse response = new ManualMessageResponse();
        response.setRecipientCount(recipients.size());
        int volume = recipients.size() * request.getChannels().size();

        if (request.getChannels().contains(MessageChannel.IN_APP)) {
            notificationService.createManualInAppMessage(actor, request, recipients);
            for (int i = 0; i < recipients.size(); i++) {
                response.addResult(CommunicationSendResult.sent("IN_APP_RECORDED", "In-app message recorded successfully."));
            }
        }

        for (ManualRecipient recipient : recipients) {
            for (MessageChannel channel : request.getChannels()) {
                if (channel == MessageChannel.IN_APP) {
                    continue;
                }
                MessageDispatchRequest dispatchRequest = buildDispatchRequest(actor, request, recipient, channel, batchId, volume);
                String address = recipient.addressFor(channel);
                if (address == null) {
                    dispatchRequest.setRecipient("missing:" + recipient.stableKey() + ":" + channel);
                    CommunicationSendResult failed = CommunicationSendResult.failed(
                            "MANUAL_RECIPIENT_ADDRESS_MISSING",
                            "Recipient " + recipient.getDisplayName() + " has no usable " + channel + " address."
                    );
                    messageLogService.record(dispatchRequest, null, failed);
                    response.addResult(failed);
                    continue;
                }
                dispatchRequest.setRecipient(address);
                response.addResult(messageDispatchService.dispatch(dispatchRequest));
            }
        }
        return response;
    }

    private MessageDispatchRequest buildDispatchRequest(
            ManualCommunicationActor actor,
            ManualMessageRequest request,
            ManualRecipient recipient,
            MessageChannel channel,
            String batchId,
            int volume) {
        MessageDispatchRequest dispatchRequest = new MessageDispatchRequest();
        dispatchRequest.setEventType(MessageEventType.MANUAL_MESSAGE);
        dispatchRequest.setChannel(channel);
        dispatchRequest.setSubject(clean(request.getSubject()) == null ? "Manual message" : clean(request.getSubject()));
        dispatchRequest.setBody(request.getBody());
        dispatchRequest.setLanguage(request.getLanguage());
        dispatchRequest.setMessageType(request.getMessageType() == null ? MessageType.CUSTOM : request.getMessageType());
        dispatchRequest.setUser(recipient.getUser());
        dispatchRequest.setVolume(Math.max(volume, 1));
        dispatchRequest.setIdempotencyKey(idempotencyKey(actor, recipient, channel, batchId));
        dispatchRequest.setVariables(variables(actor, request, recipient, batchId));
        dispatchRequest.setPayloadJson(payloadJson(actor, request, recipient, batchId));
        dispatchRequest.setReceiverType(receiverType(recipient));
        dispatchRequest.setReceiverReferenceId(recipient.getRecipientReferenceId());
        dispatchRequest.setVendorId(receiverType(recipient) == ReceiverType.VENDOR ? recipient.getRecipientReferenceId() : null);
        dispatchRequest.setReceiverDisplayName(recipient.getDisplayName());
        dispatchRequest.setReceiverEmail(recipient.getEmail());
        dispatchRequest.setReceiverMobile(recipient.getMobile());
        return dispatchRequest;
    }

    private Map<String, Object> variables(ManualCommunicationActor actor, ManualMessageRequest request, ManualRecipient recipient, String batchId) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("manualBatchId", batchId);
        variables.put("manualAudience", request.getAudience().name());
        variables.put("manualSenderType", actor.getActorType().name());
        variables.put("manualSenderName", actor.getDisplayName());
        variables.put("manualRecipientType", recipient.getRecipientType().name());
        variables.put("manualRecipientName", recipient.getDisplayName());
        return variables;
    }

    private String payloadJson(ManualCommunicationActor actor, ManualMessageRequest request, ManualRecipient recipient, String batchId) {
        try {
            Map<String, Object> payload = variables(actor, request, recipient, batchId);
            payload.put("senderUserId", actor.getActorUserId());
            payload.put("senderVendorId", actor.getVendorId());
            payload.put("recipientReferenceId", recipient.getRecipientReferenceId());
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String idempotencyKey(ManualCommunicationActor actor, ManualRecipient recipient, MessageChannel channel, String batchId) {
        return "manual:" + actor.getActorType() + ":" + String.valueOf(actor.getActorUserId()) + ":"
                + batchId + ":" + recipient.stableKey() + ":" + channel;
    }

    private ReceiverType receiverType(ManualRecipient recipient) {
        return switch (recipient.getRecipientType()) {
            case CUSTOMER -> ReceiverType.CUSTOMER;
            case VENDOR -> ReceiverType.VENDOR;
            case ADMIN -> ReceiverType.ADMIN;
        };
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
