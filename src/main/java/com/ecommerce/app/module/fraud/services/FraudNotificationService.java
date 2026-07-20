package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.events.FraudOutboxDispatchEvent;

public interface FraudNotificationService {

    void handleOutboxEvent(FraudOutboxDispatchEvent event);
}
