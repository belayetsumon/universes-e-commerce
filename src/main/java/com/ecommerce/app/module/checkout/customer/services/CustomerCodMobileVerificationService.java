package com.ecommerce.app.module.checkout.customer.services;

import com.ecommerce.app.module.checkout.customer.dto.CustomerCodMobileOtpResponse;
import com.ecommerce.app.module.checkout.guest.model.OtpPurpose;
import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import com.ecommerce.app.module.checkout.guest.repository.OtpVerificationRepository;
import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.communication.services.MessageDispatchService;
import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerCodMobileVerificationService {

    public static final String CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE
            = "customerCodMobileVerification.deviceHash";

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerCodMobileVerificationService.class);
    private static final OtpPurpose PURPOSE = OtpPurpose.CUSTOMER_COD;
    private static final int MAX_RESENDS = 3;
    private static final int IP_DAILY_SEND_LIMIT = 20;
    private static final int DEVICE_DAILY_SEND_LIMIT = 10;
    private static final int SESSION_DAILY_SEND_LIMIT = 10;
    private static final String GENERIC_INVALID_OTP_MESSAGE = "Invalid or expired verification code.";
    private static final String GENERIC_DISPATCH_FAILURE_MESSAGE = "We could not send a verification code right now. Please try again later.";

    private final OtpVerificationRepository otpRepository;
    private final UsersRepository usersRepository;
    private final MobileNumberNormalizationService mobileNumberService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MessageDispatchService messageDispatchService;
    private final StoreOperationModeService storeOperationModeService;
    private final SecureRandom secureRandom = new SecureRandom();

    public CustomerCodMobileVerificationService(
            OtpVerificationRepository otpRepository,
            UsersRepository usersRepository,
            MobileNumberNormalizationService mobileNumberService,
            BCryptPasswordEncoder passwordEncoder,
            MessageDispatchService messageDispatchService,
            StoreOperationModeService storeOperationModeService) {
        this.otpRepository = otpRepository;
        this.usersRepository = usersRepository;
        this.mobileNumberService = mobileNumberService;
        this.passwordEncoder = passwordEncoder;
        this.messageDispatchService = messageDispatchService;
        this.storeOperationModeService = storeOperationModeService;
    }

    @Transactional(readOnly = true)
    public CustomerCodMobileOtpResponse currentStatus() {
        Users user = requireActiveUser(false);
        boolean verificationRequired = storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled();
        boolean verified = isCodMobileVerificationSatisfied(user);

        CustomerCodMobileOtpResponse response = CustomerCodMobileOtpResponse.success(
                verified ? "Mobile verification is complete." : "Mobile verification is required for COD."
        );
        response.setVerificationRequired(verificationRequired);
        response.setVerified(verified);
        response.setMaskedMobile(maskCurrentMobile(user));
        return response;
    }

    @Transactional
    public CustomerCodMobileOtpResponse sendOtp(
            String deviceFingerprint,
            HttpServletRequest request,
            HttpSession session) {
        Users user = requireActiveUser(true);
        boolean verificationRequired = storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled();
        if (!verificationRequired) {
            expirePendingOtps(user.getId(), LocalDateTime.now());
            clearBoundDevice(session);
            CustomerCodMobileOtpResponse response = CustomerCodMobileOtpResponse.success(
                    "Registered customer COD mobile verification is currently disabled."
            );
            response.setVerificationRequired(false);
            response.setVerified(true);
            response.setMaskedMobile(maskCurrentMobile(user));
            return response;
        }

        String mobile = normalizeCurrentMobile(user);
        String deviceHash = hashRequired(deviceFingerprint, "Device verification is required.");
        if (isCurrentMobileVerified(user)) {
            clearBoundDevice(session);
            CustomerCodMobileOtpResponse response = CustomerCodMobileOtpResponse.success("Mobile number is already verified.");
            response.setVerificationRequired(true);
            response.setVerified(true);
            response.setMaskedMobile(mobileNumberService.mask(mobile));
            return response;
        }

        clearVerification(user);
        LocalDateTime now = LocalDateTime.now();
        String ipHash = hash(clientIp(request));
        enforceSendLimits(user.getId(), mobile, ipHash, deviceHash, session.getId(), now);

        int resendCount = 0;
        OtpVerification latestPending = otpRepository
                .findTopByUserIdAndPurposeAndStatusOrderByCreatedAtDesc(user.getId(), PURPOSE, OtpStatus.PENDING)
                .orElse(null);
        if (latestPending != null
                && mobile.equals(latestPending.getMobileNumber())
                && !latestPending.isExpired(now)) {
            ensureResendAllowed(latestPending, now);
            resendCount = latestPending.getResendCount() + 1;
        }

        expirePendingOtps(user.getId(), now);
        int otpTtlMinutes = storeOperationModeService.guestOtpExpiryMinutes();
        int resendCooldownSeconds = storeOperationModeService.guestOtpResendCooldownSeconds();
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        OtpVerification verification = new OtpVerification();
        verification.setUserId(user.getId());
        verification.setMobileNumber(mobile);
        verification.setOtpHash(passwordEncoder.encode(otp));
        verification.setPurpose(PURPOSE);
        verification.setStatus(OtpStatus.PENDING);
        verification.setResendCount(resendCount);
        verification.setExpiresAt(now.plusMinutes(otpTtlMinutes));
        verification.setSessionToken(UUID.randomUUID().toString());
        verification.setHttpSessionId(session.getId());
        verification.setIpAddressHash(ipHash);
        verification.setDeviceFingerprintHash(deviceHash);
        verification = otpRepository.save(verification);

        if (!dispatchOtp(verification, otp, otpTtlMinutes)) {
            verification.setStatus(OtpStatus.FAILED);
            verification.setExpiresAt(now);
            otpRepository.save(verification);
            clearBoundDevice(session);
            CustomerCodMobileOtpResponse response = CustomerCodMobileOtpResponse.failure(GENERIC_DISPATCH_FAILURE_MESSAGE);
            response.setVerificationRequired(true);
            response.setVerified(false);
            response.setMaskedMobile(mobileNumberService.mask(mobile));
            return response;
        }

        session.setAttribute(CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE, deviceHash);

        CustomerCodMobileOtpResponse response = CustomerCodMobileOtpResponse.success(
                "If the account mobile number is valid, a verification code has been sent."
        );
        response.setSessionToken(verification.getSessionToken());
        response.setResendAvailableInSeconds(resendCooldownSeconds);
        response.setMaskedMobile(mobileNumberService.mask(mobile));
        response.setVerificationRequired(true);
        response.setVerified(false);
        return response;
    }

    @Transactional
    public CustomerCodMobileOtpResponse verifyOtp(
            String sessionToken,
            String otp,
            String deviceFingerprint,
            HttpSession session) {
        Users user = requireActiveUser(true);
        if (!storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled()) {
            expirePendingOtps(user.getId(), LocalDateTime.now());
            clearBoundDevice(session);
            CustomerCodMobileOtpResponse response = CustomerCodMobileOtpResponse.success(
                    "Registered customer COD mobile verification is currently disabled."
            );
            response.setVerificationRequired(false);
            response.setVerified(true);
            response.setMaskedMobile(maskCurrentMobile(user));
            return response;
        }

        String cleanToken = clean(sessionToken);
        String cleanOtp = clean(otp);
        String deviceHash = hashRequired(deviceFingerprint, "Device verification is required.");
        if (cleanToken == null || cleanOtp == null) {
            return invalidOtpResponse(user);
        }

        OtpVerification verification = otpRepository.findBySessionTokenForUpdate(cleanToken).orElse(null);
        if (!hasValidBinding(verification, user, session, deviceHash)) {
            clearBoundDevice(session);
            return invalidOtpResponse(user);
        }

        LocalDateTime now = LocalDateTime.now();
        if (verification.getStatus() != OtpStatus.PENDING || verification.isExpired(now)) {
            if (verification.getStatus() == OtpStatus.PENDING) {
                verification.setStatus(OtpStatus.EXPIRED);
                otpRepository.save(verification);
            }
            clearBoundDevice(session);
            return invalidOtpResponse(user);
        }

        int maximumAttempts = storeOperationModeService.guestOtpMaximumAttempts();
        if (verification.getAttemptCount() >= maximumAttempts) {
            verification.setStatus(OtpStatus.BLOCKED);
            otpRepository.save(verification);
            clearBoundDevice(session);
            return invalidOtpResponse(user);
        }

        if (!passwordEncoder.matches(cleanOtp, verification.getOtpHash())) {
            verification.setAttemptCount(verification.getAttemptCount() + 1);
            if (verification.getAttemptCount() >= maximumAttempts) {
                verification.setStatus(OtpStatus.BLOCKED);
                clearBoundDevice(session);
            }
            otpRepository.save(verification);
            return invalidOtpResponse(user);
        }

        verification.setStatus(OtpStatus.VERIFIED);
        verification.setVerifiedAt(now);
        otpRepository.save(verification);

        user.setMobileVerified(true);
        user.setMobileVerifiedAt(now);
        user.setMobileVerifiedNumber(verification.getMobileNumber());
        usersRepository.save(user);
        session.setAttribute(CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE, deviceHash);

        CustomerCodMobileOtpResponse response = CustomerCodMobileOtpResponse.success("Mobile number verified for COD.");
        response.setVerificationRequired(true);
        response.setVerified(true);
        response.setMaskedMobile(mobileNumberService.mask(verification.getMobileNumber()));
        return response;
    }

    public boolean isCodMobileVerificationSatisfied(Users user) {
        if (user == null) {
            return false;
        }
        return !storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled()
                || isCurrentMobileVerified(user);
    }

    public boolean isCurrentMobileVerified(Users user) {
        if (user == null
                || !user.isMobileVerified()
                || user.getMobileVerifiedAt() == null
                || clean(user.getMobileVerifiedNumber()) == null) {
            return false;
        }
        try {
            String currentMobile = mobileNumberService.normalizeBangladeshMobile(user.getMobile());
            String verifiedMobile = mobileNumberService.normalizeBangladeshMobile(user.getMobileVerifiedNumber());
            return currentMobile.equals(verifiedMobile);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public String updateMobileAndInvalidateVerificationIfChanged(Users user, String rawMobile) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        String normalizedMobile = mobileNumberService.normalizeBangladeshMobile(rawMobile);
        String currentNormalizedMobile = normalizeOrNull(user.getMobile());
        if (!normalizedMobile.equals(currentNormalizedMobile)) {
            clearVerification(user);
        }
        user.setMobile(normalizedMobile);
        return normalizedMobile;
    }

    public void clearVerification(Users user) {
        if (user == null) {
            return;
        }
        user.setMobileVerified(false);
        user.setMobileVerifiedAt(null);
        user.setMobileVerifiedNumber(null);
    }

    private Users requireActiveUser(boolean lockForUpdate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || clean(authentication.getName()) == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new AuthenticationCredentialsNotFoundException("Authentication is required.");
        }

        return (lockForUpdate
                ? usersRepository.findByEmailForUpdate(authentication.getName())
                : usersRepository.findByEmail(authentication.getName()))
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authenticated customer was not found."));
    }

    private boolean hasValidBinding(
            OtpVerification verification,
            Users user,
            HttpSession session,
            String deviceHash) {
        if (verification == null
                || verification.getPurpose() != PURPOSE
                || user.getId() == null
                || !user.getId().equals(verification.getUserId())
                || !session.getId().equals(verification.getHttpSessionId())
                || !secureEquals(deviceHash, verification.getDeviceFingerprintHash())) {
            return false;
        }
        String currentMobile = normalizeOrNull(user.getMobile());
        return currentMobile != null && currentMobile.equals(verification.getMobileNumber());
    }

    private CustomerCodMobileOtpResponse invalidOtpResponse(Users user) {
        CustomerCodMobileOtpResponse response = CustomerCodMobileOtpResponse.failure(GENERIC_INVALID_OTP_MESSAGE);
        response.setVerificationRequired(true);
        response.setVerified(false);
        response.setMaskedMobile(maskCurrentMobile(user));
        return response;
    }

    private void enforceSendLimits(
            Long userId,
            String mobile,
            String ipHash,
            String deviceHash,
            String httpSessionId,
            LocalDateTime now) {
        LocalDateTime windowStart = now.minusDays(1);
        int dailyLimit = storeOperationModeService.guestOtpDailySendLimit();
        boolean limited = otpRepository.countByUserIdAndPurposeAndCreatedAtAfter(userId, PURPOSE, windowStart) >= dailyLimit
                || otpRepository.countByMobileNumberAndPurposeAndCreatedAtAfter(mobile, PURPOSE, windowStart) >= dailyLimit
                || (ipHash != null && otpRepository.countByIpAddressHashAndCreatedAtAfter(ipHash, windowStart) >= IP_DAILY_SEND_LIMIT)
                || otpRepository.countByDeviceFingerprintHashAndCreatedAtAfter(deviceHash, windowStart) >= DEVICE_DAILY_SEND_LIMIT
                || otpRepository.countByHttpSessionIdAndCreatedAtAfter(httpSessionId, windowStart) >= SESSION_DAILY_SEND_LIMIT;
        if (limited) {
            throw new OtpRateLimitException("Too many verification requests. Please try again later.");
        }
    }

    private void ensureResendAllowed(OtpVerification existing, LocalDateTime now) {
        if (existing.getResendCount() >= MAX_RESENDS) {
            throw new OtpRateLimitException("The resend limit has been reached. Please try again later.");
        }
        int cooldownSeconds = storeOperationModeService.guestOtpResendCooldownSeconds();
        LocalDateTime createdAt = existing.getCreatedAt();
        if (createdAt != null) {
            long elapsedSeconds = Math.max(0, Duration.between(createdAt, now).getSeconds());
            if (elapsedSeconds < cooldownSeconds) {
                long remaining = cooldownSeconds - elapsedSeconds;
                throw new OtpRateLimitException("Please wait " + remaining + " seconds before requesting another code.");
            }
        }
    }

    private void expirePendingOtps(Long userId, LocalDateTime now) {
        List<OtpVerification> pending = otpRepository.findByUserIdAndPurposeAndStatus(userId, PURPOSE, OtpStatus.PENDING);
        for (OtpVerification verification : pending) {
            verification.setStatus(OtpStatus.EXPIRED);
            verification.setExpiresAt(now);
        }
        if (!pending.isEmpty()) {
            otpRepository.saveAll(pending);
        }
    }

    private boolean dispatchOtp(OtpVerification verification, String otp, int otpTtlMinutes) {
        MessageDispatchRequest dispatchRequest = new MessageDispatchRequest();
        dispatchRequest.setEventType(MessageEventType.CUSTOMER_COD_OTP);
        dispatchRequest.setChannel(MessageChannel.SMS);
        dispatchRequest.setMessageType(MessageType.TRANSACTIONAL);
        dispatchRequest.setRecipient(verification.getMobileNumber());
        dispatchRequest.setReceiverMobile(verification.getMobileNumber());
        dispatchRequest.setSubject("COD mobile verification");
        dispatchRequest.setBody("Your Universes Commerce COD verification code is " + otp
                + ". It expires in " + otpTtlMinutes + " minutes.");
        dispatchRequest.setIdempotencyKey("customer-cod-otp:" + verification.getUuid());
        dispatchRequest.setVariables(Map.of("otp", otp, "ttlMinutes", otpTtlMinutes));

        try {
            CommunicationSendResult result = messageDispatchService.dispatch(dispatchRequest);
            boolean accepted = result != null
                    && result.isSuccess()
                    && ("SENT".equals(result.getStatus()) || "QUEUED".equals(result.getStatus()));
            if (!accepted) {
                LOGGER.warn("Customer COD OTP dispatch was not accepted. userId={}", verification.getUserId());
            }
            return accepted;
        } catch (RuntimeException ex) {
            LOGGER.error("Customer COD OTP dispatch failed unexpectedly. userId={}", verification.getUserId());
            return false;
        }
    }

    private String normalizeCurrentMobile(Users user) {
        try {
            return mobileNumberService.normalizeBangladeshMobile(user.getMobile());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Save a valid Bangladesh mobile number in your profile before using COD.");
        }
    }

    private String normalizeOrNull(String mobile) {
        try {
            return mobileNumberService.normalizeBangladeshMobile(mobile);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String maskCurrentMobile(Users user) {
        String normalizedMobile = user == null ? null : normalizeOrNull(user.getMobile());
        return mobileNumberService.mask(normalizedMobile);
    }

    private String clientIp(HttpServletRequest request) {
        return request == null ? null : clean(request.getRemoteAddr());
    }

    private void clearBoundDevice(HttpSession session) {
        if (session != null) {
            session.removeAttribute(CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE);
        }
    }

    private String hashRequired(String value, String message) {
        String hash = hash(value);
        if (hash == null) {
            throw new IllegalArgumentException(message);
        }
        return hash;
    }

    private String hash(String value) {
        String cleanValue = clean(value);
        if (cleanValue == null) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(cleanValue.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private boolean secureEquals(String first, String second) {
        if (first == null || second == null) {
            return false;
        }
        return MessageDigest.isEqual(
                first.getBytes(StandardCharsets.UTF_8),
                second.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleanValue = value.trim();
        return cleanValue.isEmpty() ? null : cleanValue;
    }

    public static class OtpRateLimitException extends RuntimeException {

        public OtpRateLimitException(String message) {
            super(message);
        }
    }
}
