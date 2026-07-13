package com.ecommerce.app.module.checkout.guest.services;

import org.springframework.stereotype.Service;

@Service
public class MobileNumberNormalizationService {

    private static final String NORMALIZED_BANGLADESH_MOBILE_PATTERN = "^8801[3-9][0-9]{8}$";

    public String normalizeBangladeshMobile(String rawMobile) {
        if (rawMobile == null) {
            throw new IllegalArgumentException("Mobile number is required.");
        }

        String cleanedMobile = rawMobile.trim();
        if (!cleanedMobile.matches("^\\+?[0-9\\s().-]+$")) {
            throw new IllegalArgumentException("Enter a valid Bangladesh mobile number.");
        }

        String digits = cleanedMobile
                .replace("+", "")
                .replaceAll("[^0-9]", "");

        if (digits.startsWith("00880")) {
            digits = digits.substring(2);
        }
        if (digits.startsWith("880")) {
            // already country-code format
        } else if (digits.startsWith("01")) {
            digits = "88" + digits;
        } else if (digits.startsWith("1")) {
            digits = "880" + digits;
        }

        if (!digits.matches(NORMALIZED_BANGLADESH_MOBILE_PATTERN)) {
            throw new IllegalArgumentException("Enter a valid Bangladesh mobile number.");
        }
        return digits;
    }

    public String toLocalDisplay(String normalizedMobile) {
        if (normalizedMobile != null && normalizedMobile.matches(NORMALIZED_BANGLADESH_MOBILE_PATTERN)) {
            return "0" + normalizedMobile.substring(3);
        }
        return normalizedMobile;
    }

    public String mask(String normalizedMobile) {
        if (normalizedMobile == null || normalizedMobile.length() < 7) {
            return "01*********";
        }
        String local = toLocalDisplay(normalizedMobile);
        if (local == null || local.length() < 7) {
            return "01*********";
        }
        return local.substring(0, 3) + "*****" + local.substring(local.length() - 3);
    }
}
