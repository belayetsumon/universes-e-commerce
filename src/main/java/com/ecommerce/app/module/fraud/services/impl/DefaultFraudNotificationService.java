package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.communication.events.CommunicationRequestedEvent;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.fraud.events.FraudOutboxDispatchEvent;
import com.ecommerce.app.module.fraud.model.FraudEventType;
import com.ecommerce.app.module.fraud.services.FraudNotificationService;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.user.model.Users;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DefaultFraudNotificationService implements FraudNotificationService {

    private static final String FRAUD_ANALYST_QUEUE = "FRAUD_ANALYST_QUEUE";
    private static final String FINANCE_QUEUE = "FINANCE_QUEUE";

    private final ApplicationEventPublisher applicationEventPublisher;
    private final SalesOrderRepository salesOrderRepository;

    public DefaultFraudNotificationService(ApplicationEventPublisher applicationEventPublisher,
            SalesOrderRepository salesOrderRepository) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Override
    @EventListener
    public void handleOutboxEvent(FraudOutboxDispatchEvent event) {
        if (event == null || event.getEventType() == null) {
            return;
        }
        switch (event.getEventType()) {
            case ORDER_FRAUD_VERIFICATION_REQUIRED -> notifyCustomerOrderEvent(event,
                    messageTypeForVerification(event), "Additional verification required",
                    "Additional verification is required before we continue your order.");
            case ORDER_FRAUD_HELD -> {
                notifyCustomerOrderEvent(event, MessageEventType.FRAUD_ORDER_UNDER_REVIEW,
                        "Order under review", "Your order is under review. We will update you shortly.");
                notifyInternal(MessageEventType.FRAUD_FLAG_CREATED, FRAUD_ANALYST_QUEUE, event, "Fraud review required");
            }
            case ORDER_FRAUD_REJECTED -> notifyCustomerOrderEvent(event, MessageEventType.FRAUD_ORDER_REJECTED,
                    "Order update", "Your order could not be processed. Please contact support for help.");
            case COD_DISABLED -> notifyCustomerOrderEvent(event, MessageEventType.FRAUD_COD_DISABLED,
                    "COD update", "Cash-on-delivery is currently unavailable for your account. Please use prepaid checkout.");
            case REFUND_HELD -> notifyInternal(MessageEventType.FRAUD_FLAG_CREATED, FINANCE_QUEUE, event, "Refund hold requires review");
            case VENDOR_PAYOUT_HELD -> notifyInternal(MessageEventType.FRAUD_VENDOR_PAYOUT_HELD, FINANCE_QUEUE, event, "Vendor payout held");
            case FRAUD_CASE_OPENED -> notifyInternal(MessageEventType.FRAUD_FLAG_CREATED, FRAUD_ANALYST_QUEUE, event, "Fraud case opened");
            case FRAUD_CASE_ASSIGNED -> notifyInternal(MessageEventType.FRAUD_CASE_ASSIGNED,
                    extractString(event.getPayloadJson(), "assignedInvestigator"), event, "Fraud case assigned");
            case FRAUD_CASE_RESOLVED -> notifyInternal(MessageEventType.FRAUD_FLAG_CREATED, FRAUD_ANALYST_QUEUE, event, "Fraud case resolved");
            case CUSTOMER_BLOCKED, DEVICE_BLOCKED -> notifyInternal(MessageEventType.FRAUD_ACCOUNT_BLOCKED, FRAUD_ANALYST_QUEUE, event, "Fraud block applied");
            default -> {
                if (isCriticalPayload(event.getPayloadJson())) {
                    notifyInternal(MessageEventType.FRAUD_CRITICAL_RULE_TRIGGERED, FRAUD_ANALYST_QUEUE, event, "Critical fraud signal");
                }
            }
        }
    }

    private void notifyCustomerOrderEvent(FraudOutboxDispatchEvent event, MessageEventType messageEventType,
            String subject, String body) {
        Optional<SalesOrder> order = findOrder(event);
        if (order.isEmpty()) {
            return;
        }
        String recipient = resolveCustomerRecipient(order.get());
        if (isBlank(recipient)) {
            return;
        }
        Map<String, Object> variables = baseVariables(event, order.get().getId(), "SALES_ORDER");
        variables.put("orderNumber", safe(order.get().getOrderCode()));
        applicationEventPublisher.publishEvent(CommunicationRequestedEvent.order(
                messageEventType,
                MessageChannel.SMS,
                recipient,
                order.get().getOrderCode(),
                variables));
    }

    private void notifyInternal(MessageEventType messageEventType, String recipient,
            FraudOutboxDispatchEvent event, String title) {
        if (isBlank(recipient)) {
            return;
        }
        Map<String, Object> variables = baseVariables(event, event == null ? null : event.getAggregateId(),
                event == null ? "FRAUD_EVENT" : event.getAggregateType());
        variables.put("title", title);
        applicationEventPublisher.publishEvent(CommunicationRequestedEvent.system(
                messageEventType,
                MessageChannel.IN_APP,
                recipient,
                variables));
    }

    private Optional<SalesOrder> findOrder(FraudOutboxDispatchEvent event) {
        Long orderId = extractLong(event == null ? null : event.getPayloadJson(), "orderId");
        if (orderId != null) {
            return salesOrderRepository.findById(orderId);
        }
        if (event != null && "SALES_ORDER".equals(event.getAggregateType()) && event.getAggregateId() != null) {
            return salesOrderRepository.findById(event.getAggregateId());
        }
        return Optional.empty();
    }

    private MessageEventType messageTypeForVerification(FraudOutboxDispatchEvent event) {
        String payload = event == null ? "" : safe(event.getPayloadJson()).toUpperCase();
        if (payload.contains("REQUIRE_PREPAID") || payload.contains("REQUIRE_PARTIAL_PREPAYMENT")) {
            return MessageEventType.FRAUD_PREPAYMENT_REQUIRED;
        }
        return MessageEventType.FRAUD_VERIFICATION_REQUIRED;
    }

    private String resolveCustomerRecipient(SalesOrder order) {
        if (!isBlank(order.getMobileNumber())) {
            return order.getMobileNumber().trim();
        }
        Users customer = order.getCustomer();
        if (customer != null && !isBlank(customer.getMobile())) {
            return customer.getMobile().trim();
        }
        if (customer != null && !isBlank(customer.getEmail())) {
            return customer.getEmail().trim();
        }
        return null;
    }

    private Map<String, Object> baseVariables(FraudOutboxDispatchEvent event, Long aggregateId, String aggregateType) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("fraudEventType", event == null ? "" : event.getEventType().name());
        variables.put("aggregateType", safe(aggregateType));
        variables.put("aggregateId", aggregateId == null ? "" : String.valueOf(aggregateId));
        variables.put("correlationId", event == null ? "" : safe(event.getCorrelationId()));
        variables.put("outboxEventId", event == null ? "" : String.valueOf(event.getOutboxEventId()));
        return variables;
    }

    private boolean isCriticalPayload(String payloadJson) {
        String payload = safe(payloadJson).toUpperCase();
        return payload.contains("CRITICAL") || payload.contains("BLOCK") || payload.contains("REJECT");
    }

    private Long extractLong(String json, String fieldName) {
        if (isBlank(json) || isBlank(fieldName)) {
            return null;
        }
        String needle = "\"" + fieldName + "\":";
        int start = json.indexOf(needle);
        if (start < 0) {
            return null;
        }
        int valueStart = start + needle.length();
        int valueEnd = valueStart;
        while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
            valueEnd++;
        }
        if (valueEnd == valueStart) {
            return null;
        }
        try {
            return Long.valueOf(json.substring(valueStart, valueEnd));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String extractString(String json, String fieldName) {
        if (isBlank(json) || isBlank(fieldName)) {
            return null;
        }
        String needle = "\"" + fieldName + "\":\"";
        int start = json.indexOf(needle);
        if (start < 0) {
            return null;
        }
        int valueStart = start + needle.length();
        int valueEnd = json.indexOf('"', valueStart);
        if (valueEnd <= valueStart) {
            return null;
        }
        String value = json.substring(valueStart, valueEnd).replace("\\\"", "\"").replace("\\\\", "\\");
        return isBlank(value) ? null : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
