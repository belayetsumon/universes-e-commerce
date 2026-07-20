package com.ecommerce.app.module.fraud.security;

import com.ecommerce.app.module.fraud.dto.FraudWebhookRequest;
import com.ecommerce.app.module.fraud.exception.FraudIdempotencyException;
import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import com.ecommerce.app.module.fraud.services.FraudConfigurationService;
import com.ecommerce.app.module.fraud.services.FraudIdempotencyService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class FraudWebhookSecurityService {

    private static final long DEFAULT_TOLERANCE_SECONDS = 300L;

    private final FraudConfigurationService fraudConfigurationService;
    private final FraudIdempotencyService fraudIdempotencyService;
    private final FraudRateLimitService fraudRateLimitService;

    public FraudWebhookSecurityService(FraudConfigurationService fraudConfigurationService,
            FraudIdempotencyService fraudIdempotencyService,
            FraudRateLimitService fraudRateLimitService) {
        this.fraudConfigurationService = fraudConfigurationService;
        this.fraudIdempotencyService = fraudIdempotencyService;
        this.fraudRateLimitService = fraudRateLimitService;
    }

    public void validate(FraudWebhookRequest request, String signatureHeader, String timestampHeader) {
        if (request == null) {
            throw new FraudValidationException("Webhook request is required.");
        }
        String provider = clean(request.getProvider());
        if (provider == null) {
            throw new FraudValidationException("Webhook provider is required.");
        }
        int maxRequests = fraudConfigurationService.getInt("fraud.webhook.rate_limit.max_requests", 120);
        int windowSeconds = fraudConfigurationService.getInt("fraud.webhook.rate_limit.window_seconds", 60);
        fraudRateLimitService.checkAllowed("FRAUD_WEBHOOK", provider, maxRequests, windowSeconds);
        String idempotencyKey = clean(request.getIdempotencyKey());
        if (idempotencyKey == null) {
            throw new FraudValidationException("Webhook idempotency key is required.");
        }
        if (fraudIdempotencyService.findCompleted("FRAUD_WEBHOOK:" + provider, idempotencyKey).isPresent()) {
            throw new FraudIdempotencyException("Webhook event has already been processed.");
        }
        validateTimestamp(timestampHeader);
        String secret = fraudConfigurationService.findValue("fraud.webhook." + provider.toLowerCase() + ".secret")
                .orElse(null);
        if (secret == null || secret.isBlank()) {
            throw new FraudValidationException("Webhook secret is not configured.");
        }
        String suppliedSignature = clean(signatureHeader) == null ? clean(request.getSignature()) : clean(signatureHeader);
        if (suppliedSignature == null) {
            throw new FraudValidationException("Webhook signature is required.");
        }
        String payload = timestampHeader == null ? request.getPayloadJson() : timestampHeader.trim() + "." + request.getPayloadJson();
        String expectedSignature = hmacSha256(payload, secret);
        if (!constantTimeEquals(expectedSignature, normalizeSignature(suppliedSignature))) {
            throw new FraudValidationException("Invalid webhook signature.");
        }
    }

    public void markProcessed(FraudWebhookRequest request, String responseJson) {
        if (request == null || clean(request.getProvider()) == null || clean(request.getIdempotencyKey()) == null) {
            return;
        }
        String provider = clean(request.getProvider());
        String idempotencyKey = clean(request.getIdempotencyKey());
        fraudIdempotencyService.start("FRAUD_WEBHOOK:" + provider, idempotencyKey,
                fraudIdempotencyService.hashPayload(FraudPrivacySupport.redactJson(request.getPayloadJson())));
        fraudIdempotencyService.complete("FRAUD_WEBHOOK:" + provider, idempotencyKey,
                FraudPrivacySupport.redactJson(responseJson));
    }

    private void validateTimestamp(String timestampHeader) {
        if (timestampHeader == null || timestampHeader.isBlank()) {
            throw new FraudValidationException("Webhook timestamp is required.");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader.trim());
        } catch (NumberFormatException ex) {
            throw new FraudValidationException("Webhook timestamp is invalid.");
        }
        long now = Instant.now().getEpochSecond();
        long tolerance = fraudConfigurationService.findValue("fraud.webhook.timestamp_tolerance_seconds")
                .map(value -> parseLong(value, DEFAULT_TOLERANCE_SECONDS))
                .orElse(DEFAULT_TOLERANCE_SECONDS);
        if (Math.abs(now - timestamp) > Math.max(tolerance, 60L)) {
            throw new FraudValidationException("Webhook timestamp is outside the allowed replay window.");
        }
    }

    private String hmacSha256(String value, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] encoded = mac.doFinal((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : encoded) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new FraudValidationException("Webhook signature validation failed.");
        }
    }

    private boolean constantTimeEquals(String expected, String supplied) {
        if (expected == null || supplied == null) {
            return false;
        }
        byte[] left = expected.getBytes(StandardCharsets.UTF_8);
        byte[] right = supplied.getBytes(StandardCharsets.UTF_8);
        int diff = left.length ^ right.length;
        int max = Math.max(left.length, right.length);
        for (int i = 0; i < max; i++) {
            byte l = i < left.length ? left[i] : 0;
            byte r = i < right.length ? right[i] : 0;
            diff |= l ^ r;
        }
        return diff == 0;
    }

    private String normalizeSignature(String signature) {
        String cleaned = signature.trim();
        return cleaned.startsWith("sha256=") ? cleaned.substring("sha256=".length()) : cleaned;
    }

    private long parseLong(String value, long fallback) {
        try {
            return Long.parseLong(value.trim());
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
