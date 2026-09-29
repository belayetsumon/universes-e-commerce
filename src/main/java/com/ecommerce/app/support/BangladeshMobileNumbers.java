package com.ecommerce.app.support;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** One canonical Bangladesh mobile contract shared by checkout and fraud. */
public final class BangladeshMobileNumbers {

    private static final String NORMALIZED_PATTERN = "^8801[3-9][0-9]{8}$";

    private BangladeshMobileNumbers() {
    }

    public static String normalize(String rawMobile) {
        if (rawMobile == null) {
            throw new IllegalArgumentException("Mobile number is required.");
        }
        String cleanedMobile = rawMobile.trim();
        if (!cleanedMobile.matches("^\\+?[0-9\\s().-]+$")) {
            throw new IllegalArgumentException("Enter a valid Bangladesh mobile number.");
        }
        String digits = cleanedMobile.replace("+", "").replaceAll("[^0-9]", "");
        if (digits.startsWith("00880")) {
            digits = digits.substring(2);
        }
        if (digits.startsWith("880")) {
            // Already in country-code format.
        } else if (digits.startsWith("01")) {
            digits = "88" + digits;
        } else if (digits.startsWith("1")) {
            digits = "880" + digits;
        }
        if (!digits.matches(NORMALIZED_PATTERN)) {
            throw new IllegalArgumentException("Enter a valid Bangladesh mobile number.");
        }
        return digits;
    }

    /**
     * Canonical value first, followed by common legacy representations. This
     * allows an irreversible legacy hash to be dual-read during migration.
     */
    public static List<String> commonRepresentations(String rawMobile) {
        String canonical = normalize(rawMobile);
        Set<String> representations = new LinkedHashSet<>();
        representations.add(canonical);
        representations.add("+" + canonical);
        representations.add("00" + canonical);
        representations.add("0" + canonical.substring(3));
        representations.add(canonical.substring(3));
        if (rawMobile != null && !rawMobile.isBlank()) {
            representations.add(rawMobile.trim().toLowerCase(Locale.ROOT));
        }
        return new ArrayList<>(representations);
    }

    public static boolean isNormalized(String mobile) {
        return mobile != null && mobile.matches(NORMALIZED_PATTERN);
    }
}
