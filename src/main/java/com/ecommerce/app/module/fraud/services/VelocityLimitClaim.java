package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import java.time.Duration;

/**
 * One dimension of an atomic velocity reservation.
 */
public record VelocityLimitClaim(
        VelocityCounterScope scope,
        String value,
        int limit,
        Duration window,
        long baselineCount) {
}
