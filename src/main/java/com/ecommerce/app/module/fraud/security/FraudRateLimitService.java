package com.ecommerce.app.module.fraud.security;

import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class FraudRateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public void checkAllowed(String scope, String key, int maxRequests, long windowSeconds) {
        String bucketKey = (scope == null ? "FRAUD" : scope) + ":" + (key == null ? "UNKNOWN" : key);
        long now = Instant.now().getEpochSecond();
        long safeWindow = Math.max(windowSeconds, 1L);
        int safeLimit = Math.max(maxRequests, 1);
        Bucket bucket = buckets.compute(bucketKey, (ignored, existing) -> {
            if (existing == null || now >= existing.windowStart + safeWindow) {
                return new Bucket(now, 1);
            }
            existing.count++;
            return existing;
        });
        if (bucket.count > safeLimit) {
            throw new FraudValidationException("Too many fraud requests. Please try again later.");
        }
    }

    private static class Bucket {
        private final long windowStart;
        private int count;

        private Bucket(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
