/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.services;

import com.ecommerce.app.module.communication.events.CommunicationRequestedEvent;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.vendor.model.VendorVerifications;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorVerificationsRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 *
 * @author libertyerp_local
 */
@Service
public class VendorVerificationsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendorVerificationsService.class);
    private static final int EMAIL_TOKEN_VALID_MINUTES = 15;
    private static final int MOBILE_OTP_VALID_MINUTES = 10;
    private static final SecureRandom OTP_RANDOM = new SecureRandom();

    private final VendorVerificationsRepository repository;
    private final VendorprofileRepository vendorprofileRepository;
    private final VendorUserContext vendorUserContext;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder;

    public VendorVerificationsService(
            VendorVerificationsRepository repository,
            VendorprofileRepository vendorprofileRepository,
            VendorUserContext vendorUserContext,
            org.springframework.context.ApplicationEventPublisher eventPublisher,
            BCryptPasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.vendorprofileRepository = vendorprofileRepository;
        this.vendorUserContext = vendorUserContext;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    public Vendorprofile requireActiveVendorProfile() {
        Vendorprofile activeVendor = vendorUserContext.getActiveVendor();
        if (activeVendor != null && activeVendor.getId() != null) {
            Vendorprofile refreshed = vendorprofileRepository.findById(activeVendor.getId()).orElse(null);
            if (refreshed != null) {
                vendorUserContext.setActiveVendor(refreshed);
                return refreshed;
            }
        }
        return null;
    }

    public VendorVerifications getOrCreateForVendor(Vendorprofile vendorprofile) {
        if (vendorprofile == null || vendorprofile.getId() == null) {
            return null;
        }

        VendorVerifications verification = repository.findByVendorprofile_Id(vendorprofile.getId())
                .orElseGet(VendorVerifications::new);
        verification.setVendorprofile(vendorprofile);
        String currentEmail = normalize(vendorprofile.getEmail());
        String currentMobile = normalize(vendorprofile.getPhone());

        if (hasChanged(verification.getEmail(), currentEmail)) {
            verification.setEmailVerified(false);
            verification.setToken(null);
            verification.setTokenCreatedAt(null);
        }
        if (hasChanged(verification.getMobile(), currentMobile)) {
            verification.setMobileVerified(false);
            verification.setOtp(null);
            verification.setOtpCreatedAt(null);
        }

        verification.setEmail(currentEmail);
        verification.setMobile(currentMobile);
        return repository.save(verification);
    }

    public VendorVerifications createEmailVerification(Vendorprofile vendorprofile) {
        VendorVerifications verification = getOrCreateForVendor(vendorprofile);
        if (verification == null || isBlank(verification.getEmail())) {
            throw new IllegalStateException("Vendor email address is required before verification.");
        }

        String rawToken = generateRawToken();
        verification.setToken(hashToken(rawToken));
        verification.setTokenCreatedAt(LocalDateTime.now());
        verification.setEmailVerified(false);
        VendorVerifications saved = repository.save(verification);

        publishEmailVerification(saved, rawToken);
        return saved;
    }

    public EmailVerificationResult verifyEmail(String token) {
        String tokenHash = hashTokenOrNull(token);
        if (tokenHash == null) {
            return EmailVerificationResult.INVALID;
        }

        Optional<VendorVerifications> optional = repository.findByToken(tokenHash);
        if (optional.isEmpty()) {
            return EmailVerificationResult.INVALID;
        }

        VendorVerifications verification = optional.get();
        if (isExpired(verification.getTokenCreatedAt(), EMAIL_TOKEN_VALID_MINUTES)) {
            return EmailVerificationResult.EXPIRED;
        }

        verification.setEmailVerified(true);
        verification.setToken(null);
        verification.setTokenCreatedAt(null);
        repository.save(verification);
        return EmailVerificationResult.VERIFIED;
    }

    public VendorVerifications createMobileOtp(Vendorprofile vendorprofile) {
        VendorVerifications verification = getOrCreateForVendor(vendorprofile);
        if (verification == null || isBlank(verification.getMobile())) {
            throw new IllegalStateException("Vendor mobile number is required before verification.");
        }

        String rawOtp = String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
        verification.setOtp(passwordEncoder.encode(rawOtp));
        verification.setOtpCreatedAt(LocalDateTime.now());
        verification.setMobileVerified(false);
        VendorVerifications saved = repository.save(verification);

        publishMobileOtp(saved, rawOtp);
        return saved;
    }

    public MobileVerificationResult verifyMobile(Vendorprofile vendorprofile, String otp) {
        if (vendorprofile == null || vendorprofile.getId() == null || isBlank(otp)) {
            return MobileVerificationResult.INVALID;
        }

        Optional<VendorVerifications> optional = repository.findByVendorprofile_Id(vendorprofile.getId());
        if (optional.isEmpty()) {
            return MobileVerificationResult.INVALID;
        }

        VendorVerifications verification = optional.get();
        if (isExpired(verification.getOtpCreatedAt(), MOBILE_OTP_VALID_MINUTES)) {
            return MobileVerificationResult.EXPIRED;
        }
        if (verification.getOtp() == null || !passwordEncoder.matches(otp.trim(), verification.getOtp())) {
            return MobileVerificationResult.INVALID;
        }

        verification.setMobileVerified(true);
        verification.setOtp(null);
        verification.setOtpCreatedAt(null);
        repository.save(verification);
        return MobileVerificationResult.VERIFIED;
    }

    public int emailTokenValidMinutes() {
        return EMAIL_TOKEN_VALID_MINUTES;
    }

    public int mobileOtpValidMinutes() {
        return MOBILE_OTP_VALID_MINUTES;
    }

    private void publishEmailVerification(VendorVerifications verification, String rawToken) {
        String verificationLink = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/vendorverifications/verify-email")
                .queryParam("token", rawToken)
                .build()
                .toUriString();

        Map<String, Object> variables = baseVariables(verification);
        variables.put("verificationLink", verificationLink);
        variables.put("expiresInMinutes", EMAIL_TOKEN_VALID_MINUTES);

        publishSafely(CommunicationRequestedEvent.vendor(
                MessageEventType.VENDOR_EMAIL_VERIFICATION,
                MessageChannel.EMAIL,
                verification.getEmail(),
                variables,
                "Verify your vendor email",
                "Verify your vendor email using this link: {{verificationLink}}. This link expires in {{expiresInMinutes}} minutes."
        ));

        LOGGER.info("Vendor email verification requested. vendorId={} expiresInMinutes={}",
                vendorId(verification), EMAIL_TOKEN_VALID_MINUTES);
    }

    private void publishMobileOtp(VendorVerifications verification, String rawOtp) {
        Map<String, Object> variables = baseVariables(verification);
        variables.put("otp", rawOtp);
        variables.put("expiresInMinutes", MOBILE_OTP_VALID_MINUTES);

        publishSafely(CommunicationRequestedEvent.vendor(
                MessageEventType.VENDOR_MOBILE_VERIFICATION_OTP,
                MessageChannel.SMS,
                verification.getMobile(),
                variables,
                "Vendor mobile verification OTP",
                "Your vendor mobile verification OTP is {{otp}}. It expires in {{expiresInMinutes}} minutes."
        ));

        LOGGER.info("Vendor mobile verification OTP requested. vendorId={} expiresInMinutes={}",
                vendorId(verification), MOBILE_OTP_VALID_MINUTES);
    }

    private void publishSafely(CommunicationRequestedEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (RuntimeException ex) {
            LOGGER.warn("Vendor verification communication enqueue failed for eventType={}", event.getEventType(), ex);
        }
    }

    private Map<String, Object> baseVariables(VendorVerifications verification) {
        Map<String, Object> variables = new HashMap<>();
        Vendorprofile vendor = verification.getVendorprofile();
        variables.put("vendorId", vendorId(verification));
        variables.put("vendorCode", vendor == null ? "" : nullSafe(vendor.getVendorCode()));
        variables.put("companyName", vendor == null ? "" : nullSafe(vendor.getCompanyName()));
        variables.put("email", nullSafe(verification.getEmail()));
        variables.put("mobile", nullSafe(verification.getMobile()));
        return variables;
    }

    private boolean isExpired(LocalDateTime createdAt, int validMinutes) {
        return createdAt == null || createdAt.isBefore(LocalDateTime.now().minusMinutes(validMinutes));
    }

    private Long vendorId(VendorVerifications verification) {
        return verification.getVendorprofile() == null ? null : verification.getVendorprofile().getId();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean hasChanged(String oldValue, String newValue) {
        String oldNormalized = normalize(oldValue);
        String newNormalized = normalize(newValue);
        if (oldNormalized == null) {
            return newNormalized != null;
        }
        return !oldNormalized.equalsIgnoreCase(newNormalized == null ? "" : newNormalized);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        OTP_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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
            throw new IllegalStateException("Unable to hash vendor verification token.", ex);
        }
    }

    public enum EmailVerificationResult {
        VERIFIED,
        EXPIRED,
        INVALID
    }

    public enum MobileVerificationResult {
        VERIFIED,
        EXPIRED,
        INVALID
    }
}
