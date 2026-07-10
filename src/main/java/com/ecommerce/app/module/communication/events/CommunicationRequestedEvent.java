package com.ecommerce.app.module.communication.events;

import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.user.model.Users;
import java.util.HashMap;
import java.util.Map;

public class CommunicationRequestedEvent {

    private final MessageEventType eventType;
    private final MessageChannel channel;
    private final String recipient;
    private final Users user;
    private final Map<String, Object> variables;
    private final String fallbackSubject;
    private final String fallbackBody;
    private final MessageType messageType;
    private final String idempotencyKey;

    private CommunicationRequestedEvent(
            MessageEventType eventType,
            MessageChannel channel,
            String recipient,
            Users user,
            Map<String, Object> variables,
            String fallbackSubject,
            String fallbackBody,
            MessageType messageType,
            String idempotencyKey) {
        this.eventType = eventType;
        this.channel = channel;
        this.recipient = recipient;
        this.user = user;
        this.variables = variables == null ? new HashMap<>() : new HashMap<>(variables);
        this.fallbackSubject = fallbackSubject;
        this.fallbackBody = fallbackBody;
        this.messageType = messageType == null ? MessageType.TRANSACTIONAL : messageType;
        this.idempotencyKey = idempotencyKey;
    }

    public static CommunicationRequestedEvent customer(MessageEventType eventType, Users user, MessageChannel channel, String recipient, Map<String, Object> variables) {
        return new CommunicationRequestedEvent(eventType, channel, recipient, user, variables, "Customer notification", "A customer account update is available.", MessageType.SYSTEM, null);
    }

    public static CommunicationRequestedEvent order(MessageEventType eventType, MessageChannel channel, String recipient, String orderNumber, Map<String, Object> variables) {
        Map<String, Object> merged = mergeVariables(variables);
        merged.putIfAbsent("orderNumber", orderNumber);
        return new CommunicationRequestedEvent(eventType, channel, recipient, null, merged, "Order update " + nullSafe(orderNumber), "Your order {{orderNumber}} has an update.", MessageType.ORDER, null);
    }

    public static CommunicationRequestedEvent payment(MessageEventType eventType, MessageChannel channel, String recipient, String orderNumber, Map<String, Object> variables) {
        Map<String, Object> merged = mergeVariables(variables);
        merged.putIfAbsent("orderNumber", orderNumber);
        return new CommunicationRequestedEvent(eventType, channel, recipient, null, merged, "Payment update " + nullSafe(orderNumber), "Your payment for order {{orderNumber}} has an update.", MessageType.PAYMENT, null);
    }

    public static CommunicationRequestedEvent shipment(MessageEventType eventType, MessageChannel channel, String recipient, String trackingNumber, Map<String, Object> variables) {
        Map<String, Object> merged = mergeVariables(variables);
        merged.putIfAbsent("trackingNumber", trackingNumber);
        return new CommunicationRequestedEvent(eventType, channel, recipient, null, merged, "Shipment update", "Your shipment tracking number is {{trackingNumber}}.", MessageType.SHIPPING, null);
    }

    public static CommunicationRequestedEvent vendor(MessageEventType eventType, MessageChannel channel, String recipient, Map<String, Object> variables) {
        return new CommunicationRequestedEvent(eventType, channel, recipient, null, variables, "Vendor notification", "A vendor account update is available.", MessageType.SYSTEM, null);
    }

    public static CommunicationRequestedEvent vendor(MessageEventType eventType, MessageChannel channel, String recipient, Map<String, Object> variables, String fallbackSubject, String fallbackBody) {
        return new CommunicationRequestedEvent(eventType, channel, recipient, null, variables, fallbackSubject, fallbackBody, MessageType.SYSTEM, null);
    }

    public static CommunicationRequestedEvent promotion(MessageEventType eventType, Users user, MessageChannel channel, String recipient, Map<String, Object> variables) {
        return new CommunicationRequestedEvent(eventType, channel, recipient, user, variables, "Promotion notification", "A new promotion update is available.", MessageType.PROMOTION, null);
    }

    public static CommunicationRequestedEvent system(MessageEventType eventType, MessageChannel channel, String recipient, Map<String, Object> variables) {
        return new CommunicationRequestedEvent(eventType, channel, recipient, null, variables, "System alert", "A system alert requires attention.", MessageType.SYSTEM, null);
    }

    public MessageDispatchRequest toDispatchRequest() {
        MessageDispatchRequest request = new MessageDispatchRequest();
        request.setEventType(eventType);
        request.setChannel(channel);
        request.setRecipient(recipient);
        request.setUser(user);
        request.setSubject(fallbackSubject);
        request.setBody(fallbackBody);
        request.setVariables(getVariables());
        request.setMessageType(messageType);
        request.setIdempotencyKey(idempotencyKey);
        return request;
    }

    public MessageEventType getEventType() { return eventType; }
    public MessageChannel getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public Users getUser() { return user; }
    public Map<String, Object> getVariables() { return new HashMap<>(variables); }
    public String getFallbackSubject() { return fallbackSubject; }
    public String getFallbackBody() { return fallbackBody; }
    public MessageType getMessageType() { return messageType; }
    public String getIdempotencyKey() { return idempotencyKey; }

    private static Map<String, Object> mergeVariables(Map<String, Object> variables) {
        return variables == null ? new HashMap<>() : new HashMap<>(variables);
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
