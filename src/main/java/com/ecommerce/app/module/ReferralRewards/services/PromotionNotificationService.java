package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.CustomerNotifications;
import com.ecommerce.app.module.user.model.Users;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionNotificationService {

    public static final String CHANNEL_IN_APP = "IN_APP";
    public static final String STATUS_SENT = "SENT";

    private final com.ecommerce.app.module.ReferralRewards.repository.CustomerNotifications customerNotificationsRepository;

    public PromotionNotificationService(com.ecommerce.app.module.ReferralRewards.repository.CustomerNotifications customerNotificationsRepository) {
        this.customerNotificationsRepository = customerNotificationsRepository;
    }

    @Transactional
    public CustomerNotifications recordInApp(Users user, String eventName, String message, String payloadSummary) {
        if (user == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        CustomerNotifications notification = new CustomerNotifications();
        notification.setUser(user);
        notification.setEventName(clean(eventName));
        notification.setChannel(CHANNEL_IN_APP);
        notification.setRecipient(user.getEmail());
        notification.setMessage(clean(message));
        notification.setPayloadSummary(clean(payloadSummary));
        notification.setStatus(STATUS_SENT);
        notification.setRetryCount(0);
        notification.setCreatedAt(now);
        notification.setSentAt(now);
        return customerNotificationsRepository.save(notification);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
