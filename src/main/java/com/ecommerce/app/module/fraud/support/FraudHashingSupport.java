package com.ecommerce.app.module.fraud.support;

import com.ecommerce.app.support.BangladeshMobileNumbers;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.List;

public final class FraudHashingSupport {

    private static final int SHA_256_HEX_LENGTH = 64;

    private FraudHashingSupport() {
    }

    public static String sha256(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return sha256Bytes(normalized);
    }

    /**
     * Hashes the exact UTF-8 value without case folding or trimming. Use this
     * for idempotency payloads and other case-sensitive canonical data, not
     * for fraud identifiers whose domain normalization is intentional.
     */
    public static String sha256Exact(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return sha256Bytes(value);
    }

    private static String sha256Bytes(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 hashing is not available.", ex);
        }
    }

    /**
     * Produces the canonical persisted form for identifiers that may already be
     * supplied as a SHA-256 digest. This keeps raw device identifiers out of
     * persistence while avoiding a second hash when a trusted caller already
     * supplies the digest.
     */
    public static String canonicalIdentifierHash(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return isSha256Hex(normalized) ? normalized : sha256(normalized);
    }

    public static String canonicalBangladeshMobileHash(String value) {
        return sha256Exact(BangladeshMobileNumbers.normalize(value));
    }

    /** Canonical and common legacy hashes, canonical first. */
    public static List<String> bangladeshMobileHashCandidates(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return BangladeshMobileNumbers.commonRepresentations(value).stream()
                    .map(FraudHashingSupport::sha256)
                    .distinct()
                    .toList();
        } catch (IllegalArgumentException invalidMobile) {
            return List.of();
        }
    }

    /** Canonical and common legacy raw values for scope-prefixed hashes. */
    public static List<String> bangladeshMobileValueCandidates(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return BangladeshMobileNumbers.commonRepresentations(value);
        } catch (IllegalArgumentException invalidMobile) {
            return List.of();
        }
    }

    private static boolean isSha256Hex(String value) {
        if (value.length() != SHA_256_HEX_LENGTH) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            boolean decimal = character >= '0' && character <= '9';
            boolean hexadecimal = character >= 'a' && character <= 'f';
            if (!decimal && !hexadecimal) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
