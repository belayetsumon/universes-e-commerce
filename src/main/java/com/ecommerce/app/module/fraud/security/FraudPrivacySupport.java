package com.ecommerce.app.module.fraud.security;

import com.ecommerce.app.module.fraud.support.FraudHashingSupport;

public final class FraudPrivacySupport {

    private FraudPrivacySupport() {
    }

    public static String hashIdentifier(String value) {
        return FraudHashingSupport.sha256(value);
    }

    public static String maskIdentifier(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return "";
        }
        if (cleaned.contains("@")) {
            return maskEmail(cleaned);
        }
        String digits = cleaned.replaceAll("[^0-9]", "");
        if (digits.length() >= 8) {
            return "****" + digits.substring(digits.length() - 4);
        }
        if (cleaned.length() <= 4) {
            return "****";
        }
        return "****" + cleaned.substring(cleaned.length() - 4);
    }

    public static String maskPaymentToken(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return "";
        }
        return cleaned.length() <= 4 ? "****" : "token_****" + cleaned.substring(cleaned.length() - 4);
    }

    public static String redactJson(String json) {
        if (json == null || json.isBlank()) {
            return "{}";
        }
        return json
                .replaceAll("(?i)\"cardNumber\"\\s*:\\s*\"[^\"]*\"", "\"cardNumber\":\"[REDACTED]\"")
                .replaceAll("(?i)\"cvv\"\\s*:\\s*\"[^\"]*\"", "\"cvv\":\"[REDACTED]\"")
                .replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"[REDACTED]\"")
                .replaceAll("(?i)\"paymentToken\"\\s*:\\s*\"[^\"]*\"", "\"paymentToken\":\"[TOKENIZED]\"");
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "****" + email.substring(atIndex);
        }
        return email.charAt(0) + "****" + email.substring(atIndex);
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
