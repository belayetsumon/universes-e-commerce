package com.ecommerce.app.module.communication.security;

import com.ecommerce.app.module.communication.model.MessageEventType;
import java.util.EnumSet;
import java.util.Set;

/** Security policy for short-lived COD verification codes. */
public final class CodOtpMessagePolicy {

    private static final Set<MessageEventType> EPHEMERAL_EVENTS = EnumSet.of(
            MessageEventType.GUEST_CHECKOUT_OTP,
            MessageEventType.CUSTOMER_COD_OTP
    );

    private CodOtpMessagePolicy() {
    }

    public static boolean isEphemeral(MessageEventType eventType) {
        return eventType != null && EPHEMERAL_EVENTS.contains(eventType);
    }

    public static void requireQueueSafe(MessageEventType eventType) {
        if (isEphemeral(eventType)) {
            throw new IllegalArgumentException("COD OTP messages must be delivered directly and cannot be persisted in a message queue.");
        }
    }
}
