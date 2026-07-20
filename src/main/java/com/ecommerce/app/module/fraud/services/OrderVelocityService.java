package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import java.time.Duration;

public interface OrderVelocityService {

    long count(VelocityCounterScope scope, String value, Duration window);

    void increment(VelocityCounterScope scope, String value);
}
