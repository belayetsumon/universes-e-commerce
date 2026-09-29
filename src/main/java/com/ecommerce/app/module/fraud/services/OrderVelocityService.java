package com.ecommerce.app.module.fraud.services;

import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import java.time.Duration;
import java.util.List;

public interface OrderVelocityService {

    long count(VelocityCounterScope scope, String value, Duration window);

    void increment(VelocityCounterScope scope, String value);

    /**
     * Atomically reserves one attempt in an independent transaction. The
     * baseline imports attempts recorded before the counter claim existed.
     */
    boolean claimWithinLimit(
            VelocityCounterScope scope,
            String value,
            int limit,
            Duration window,
            long baselineCount);

    /**
     * Atomically reserves every supplied dimension or none of them.
     */
    boolean claimAllWithinLimits(List<VelocityLimitClaim> claims);
}
