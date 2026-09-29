package com.ecommerce.app.module.user.services;

import com.ecommerce.app.module.user.model.PasswordResetToken;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.PasswordResetTokenRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    public static final String NEUTRAL_REQUEST_MESSAGE =
            "If an account is registered with that email, recovery instructions will be sent shortly.";
    public static final String INVALID_TOKEN_MESSAGE = "This password reset link is invalid or has expired.";

    private final UsersRepository usersRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordResetEmailSender emailSender;
    private final SessionCredentialVersionService sessionCredentialVersionService;
    private final long ttlMinutes;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UsersRepository usersRepository,
            PasswordResetTokenRepository tokenRepository,
            BCryptPasswordEncoder passwordEncoder,
            PasswordResetEmailSender emailSender,
            @Value("${app.security.password-reset.ttl-minutes:30}") long ttlMinutes) {
        this(usersRepository, tokenRepository, passwordEncoder, emailSender, ttlMinutes, null);
    }

    @Autowired
    public PasswordResetService(
            UsersRepository usersRepository,
            PasswordResetTokenRepository tokenRepository,
            BCryptPasswordEncoder passwordEncoder,
            PasswordResetEmailSender emailSender,
            @Value("${app.security.password-reset.ttl-minutes:30}") long ttlMinutes,
            SessionCredentialVersionService sessionCredentialVersionService) {
        this.usersRepository = usersRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.sessionCredentialVersionService = sessionCredentialVersionService;
        this.ttlMinutes = ttlMinutes > 0 ? ttlMinutes : 30;
    }

    /**
     * Requests are deliberately neutral for blank, unknown, and known email
     * addresses. A failed delivery rolls back the newly-created capability.
     */
    @Transactional
    public void requestReset(String email, String resetUrl) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || resetUrl == null || resetUrl.isBlank()) {
            return;
        }

        Optional<Users> user = usersRepository.findByEmailIgnoreCase(normalizedEmail);
        if (user.isEmpty() || user.get().getEmail() == null || user.get().getEmail().isBlank()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        tokenRepository.invalidateActiveTokens(user.get().getId(), now, now);

        String rawToken = generateRawToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user.get());
        token.setTokenHash(hashToken(rawToken));
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusMinutes(ttlMinutes));
        tokenRepository.save(token);

        String link = appendToken(resetUrl, rawToken);
        emailSender.send(user.get().getEmail().trim(), user.get().getFirstName(), link, ttlMinutes);
    }

    @Transactional(readOnly = true)
    public boolean isTokenUsable(String rawToken) {
        String tokenHash = hashTokenOrNull(rawToken);
        if (tokenHash == null) {
            return false;
        }
        return tokenRepository.findByTokenHash(tokenHash)
                .filter(token -> token.getUsedAt() == null)
                .filter(token -> token.getExpiresAt() != null && token.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    /** Returns false for malformed, expired, already-used, or raced tokens. */
    @Transactional
    public boolean resetPassword(String rawToken, String newPassword) {
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 72) {
            return false;
        }
        String tokenHash = hashTokenOrNull(rawToken);
        if (tokenHash == null) {
            return false;
        }

        Optional<PasswordResetToken> token = tokenRepository.findByTokenHash(tokenHash);
        if (token.isEmpty() || token.get().getUsedAt() != null
                || token.get().getExpiresAt() == null
                || !token.get().getExpiresAt().isAfter(LocalDateTime.now())
                || token.get().getUser() == null) {
            return false;
        }

        Long userId = token.get().getUser().getId();
        if (userId == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (tokenRepository.consumeIfValid(token.get().getId(), now, now) != 1) {
            return false;
        }

        Users user = usersRepository.findByIdForUpdate(userId).orElse(null);
        if (user == null) {
            return false;
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        if (sessionCredentialVersionService != null) {
            return sessionCredentialVersionService.updatePassword(userId, encodedPassword, true);
        }

        // Kept for the narrow unit-test constructor; production wiring always
        // supplies the session/credential service above.
        user.setPassword(encodedPassword);
        user.setPasswordConfigured(true);
        usersRepository.save(user);
        return true;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String appendToken(String resetUrl, String rawToken) {
        String separator = resetUrl.contains("?") ? "&" : "?";
        return resetUrl + separator + "token=" + rawToken;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() || normalized.length() > 254 ? null : normalized;
    }

    private String hashTokenOrNull(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 200) {
            return null;
        }
        return hashToken(rawToken.trim());
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                hex.append(String.format(Locale.ROOT, "%02x", value));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash password-reset token.", ex);
        }
    }
}
