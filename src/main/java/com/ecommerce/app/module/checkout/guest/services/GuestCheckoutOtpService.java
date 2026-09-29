package com.ecommerce.app.module.checkout.guest.services;

import com.ecommerce.app.module.checkout.guest.dto.GuestOtpResponse;
import com.ecommerce.app.module.checkout.guest.model.MobileVerificationStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpPurpose;
import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import com.ecommerce.app.module.checkout.guest.repository.OtpVerificationRepository;
import com.ecommerce.app.module.checkout.guest.session.GuestCheckoutSession;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.model.MessageChannel;
import com.ecommerce.app.module.communication.model.MessageEventType;
import com.ecommerce.app.module.communication.model.MessageType;
import com.ecommerce.app.module.communication.services.MessageDispatchService;
import com.ecommerce.app.module.fraud.model.VelocityCounterScope;
import com.ecommerce.app.module.fraud.services.OrderVelocityService;
import com.ecommerce.app.module.fraud.services.VelocityLimitClaim;
import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import com.ecommerce.app.module.user.model.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestCheckoutOtpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuestCheckoutOtpService.class);
    private static final int MAX_RESENDS = 3;
    private static final int IP_DAILY_SEND_LIMIT = 20;
    private static final int DEVICE_DAILY_SEND_LIMIT = 10;

    private final OtpVerificationRepository repository;
    private final MobileNumberNormalizationService mobileNumberService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MessageDispatchService messageDispatchService;
    private final GuestCheckoutUserResolver userResolver;
    private final GuestCheckoutSessionService sessionService;
    private final StoreOperationModeService storeOperationModeService;
    private final OrderVelocityService orderVelocityService;
    private final SecureRandom secureRandom = new SecureRandom();

    public GuestCheckoutOtpService(
            OtpVerificationRepository repository,
            MobileNumberNormalizationService mobileNumberService,
            BCryptPasswordEncoder passwordEncoder,
            MessageDispatchService messageDispatchService,
            GuestCheckoutUserResolver userResolver,
            GuestCheckoutSessionService sessionService,
            StoreOperationModeService storeOperationModeService,
            OrderVelocityService orderVelocityService) {
        this.repository = repository;
        this.mobileNumberService = mobileNumberService;
        this.passwordEncoder = passwordEncoder;
        this.messageDispatchService = messageDispatchService;
        this.userResolver = userResolver;
        this.sessionService = sessionService;
        this.storeOperationModeService = storeOperationModeService;
        this.orderVelocityService = orderVelocityService;
    }

    @Transactional
    public GuestOtpResponse sendOtp(String rawMobile, String deviceFingerprint, HttpServletRequest request, HttpSession session) {
        if (!storeOperationModeService.isGuestCheckoutAllowed()) {
            throw new IllegalStateException("Guest checkout is currently disabled.");
        }
        String mobile = mobileNumberService.normalizeBangladeshMobile(rawMobile);
        if (!storeOperationModeService.isGuestMobileOtpVerificationEnabled()) {
            return startWithoutOtp(mobile, deviceFingerprint, request, session);
        }
        LocalDateTime now = LocalDateTime.now();
        String ipHash = hash(clientIp(request));
        String deviceHash = hash(deviceFingerprint);
        int resendCooldownSeconds = storeOperationModeService.guestOtpResendCooldownSeconds();
        int otpTtlMinutes = storeOperationModeService.guestOtpExpiryMinutes();

        OtpVerification latestPending = repository.findTopByMobileNumberAndPurposeAndStatusOrderByCreatedAtDesc(
                mobile,
                OtpPurpose.GUEST_CHECKOUT,
                OtpStatus.PENDING
        ).orElse(null);
        int resendCount = 0;
        long chainBaseline = 0L;
        if (latestPending != null && !latestPending.isExpired(now)) {
            ensureResendAllowed(latestPending, now, resendCooldownSeconds);
            resendCount = latestPending.getResendCount() + 1;
            chainBaseline = latestPending.getResendCount() + 1L;
        }
        claimSendLimits(
                mobile,
                ipHash,
                deviceHash,
                session.getId(),
                now,
                resendCooldownSeconds,
                otpTtlMinutes,
                chainBaseline);

        expirePendingOtps(mobile);
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        OtpVerification verification = new OtpVerification();
        verification.setMobileNumber(mobile);
        verification.setOtpHash(passwordEncoder.encode(otp));
        verification.setPurpose(OtpPurpose.GUEST_CHECKOUT);
        verification.setStatus(OtpStatus.PENDING);
        verification.setResendCount(resendCount);
        verification.setExpiresAt(now.plusMinutes(otpTtlMinutes));
        verification.setSessionToken(UUID.randomUUID().toString());
        verification.setHttpSessionId(session.getId());
        verification.setIpAddressHash(ipHash);
        verification.setDeviceFingerprintHash(deviceHash);
        verification = repository.save(verification);

        if (!dispatchOtp(verification, otp, otpTtlMinutes)) {
            verification.setStatus(OtpStatus.FAILED);
            verification.setExpiresAt(now);
            repository.save(verification);
            GuestOtpResponse response = GuestOtpResponse.failure(
                    "We could not send a verification code right now. Please try again later."
            );
            response.setMaskedMobile(mobileNumberService.mask(mobile));
            response.setOtpRequired(true);
            response.setMobileVerificationStatus(OtpStatus.FAILED.name());
            return response;
        }

        GuestOtpResponse response = GuestOtpResponse.success("If the mobile number is valid, a verification code has been sent.");
        response.setSessionToken(verification.getSessionToken());
        response.setMaskedMobile(mobileNumberService.mask(mobile));
        response.setResendAvailableInSeconds(resendCooldownSeconds);
        response.setOtpRequired(true);
        response.setMobileVerificationStatus(MobileVerificationStatus.PENDING.name());
        return response;
    }

    @Transactional
    public GuestOtpResponse verifyOtp(String sessionToken, String otp, String deviceFingerprint, HttpServletRequest request, HttpSession session) {
        OtpVerification verification = repository.findBySessionTokenForUpdate(clean(sessionToken)).orElse(null);
        if (verification == null) {
            return invalidOtpResponse();
        }

        LocalDateTime now = LocalDateTime.now();
        String submittedDeviceHash = hash(deviceFingerprint);
        if (verification.getPurpose() != OtpPurpose.GUEST_CHECKOUT
                || !session.getId().equals(verification.getHttpSessionId())
                || (verification.getDeviceFingerprintHash() != null
                && !secureEquals(verification.getDeviceFingerprintHash(), submittedDeviceHash))) {
            return invalidOtpResponse();
        }
        boolean latestVerification = repository
                .findTopByMobileNumberAndPurposeOrderByIdDesc(
                        verification.getMobileNumber(), OtpPurpose.GUEST_CHECKOUT)
                .map(latest -> secureEquals(latest.getSessionToken(), verification.getSessionToken()))
                .orElse(false);
        if (!latestVerification) {
            return invalidOtpResponse();
        }
        if (verification.getStatus() != OtpStatus.PENDING || verification.isExpired(now)) {
            if (verification.getStatus() == OtpStatus.PENDING) {
                verification.setStatus(OtpStatus.EXPIRED);
                repository.save(verification);
            }
            return invalidOtpResponse();
        }
        int maximumAttempts = storeOperationModeService.guestOtpMaximumAttempts();
        if (verification.getAttemptCount() >= maximumAttempts) {
            verification.setStatus(OtpStatus.BLOCKED);
            repository.save(verification);
            return invalidOtpResponse();
        }

        if (!passwordEncoder.matches(clean(otp), verification.getOtpHash())) {
            verification.setAttemptCount(verification.getAttemptCount() + 1);
            if (verification.getAttemptCount() >= maximumAttempts) {
                verification.setStatus(OtpStatus.BLOCKED);
            }
            repository.save(verification);
            return invalidOtpResponse();
        }

        verification.setStatus(OtpStatus.VERIFIED);
        verification.setVerifiedAt(now);
        repository.save(verification);

        Users user = userResolver.resolveUser(verification);
        storeGuestCheckoutSession(
                session,
                user,
                verification.getMobileNumber(),
                true,
                MobileVerificationStatus.VERIFIED,
                now,
                verification.getUuid(),
                hash(clientIp(request)),
                hash(deviceFingerprint)
        );

        GuestOtpResponse response = GuestOtpResponse.success("Mobile number verified.");
        response.setMaskedMobile(mobileNumberService.mask(verification.getMobileNumber()));
        response.setOtpRequired(true);
        response.setMobileVerificationStatus(MobileVerificationStatus.VERIFIED.name());
        return response;
    }

    @Transactional
    public void markUsed(String otpVerificationUuid) {
        if (otpVerificationUuid == null || otpVerificationUuid.isBlank()) {
            return;
        }
        repository.findByUuid(otpVerificationUuid)
                .ifPresent(verification -> {
                    verification.setStatus(OtpStatus.USED);
                    verification.setUsedAt(LocalDateTime.now());
                    repository.save(verification);
                });
    }

    private GuestOtpResponse startWithoutOtp(String mobile, String deviceFingerprint, HttpServletRequest request, HttpSession session) {
        Users user = userResolver.resolveMobile(
                mobile,
                false,
                storeOperationModeService.isGuestAutoCreateCustomerAccountEnabled()
        );
        storeGuestCheckoutSession(
                session,
                user,
                mobile,
                false,
                MobileVerificationStatus.UNVERIFIED,
                null,
                null,
                hash(clientIp(request)),
                hash(deviceFingerprint)
        );
        GuestOtpResponse response = GuestOtpResponse.success("Mobile number accepted. Continue to delivery details.");
        response.setOtpRequired(false);
        response.setMaskedMobile(mobileNumberService.mask(mobile));
        response.setMobileVerificationStatus(MobileVerificationStatus.UNVERIFIED.name());
        return response;
    }

    private void storeGuestCheckoutSession(
            HttpSession session,
            Users user,
            String mobile,
            boolean verificationRequired,
            MobileVerificationStatus verificationStatus,
            LocalDateTime verifiedAt,
            String otpVerificationUuid,
            String ipHash,
            String deviceHash) {
        GuestCheckoutSession guestSession = new GuestCheckoutSession();
        guestSession.setVerifiedMobile(mobile);
        guestSession.setOtpVerificationUuid(otpVerificationUuid);
        guestSession.setUserId(user.getId());
        guestSession.setMobileVerified(verificationStatus == MobileVerificationStatus.VERIFIED);
        guestSession.setMobileVerificationRequired(verificationRequired);
        guestSession.setMobileVerificationStatus(verificationStatus);
        guestSession.setVerificationTime(verifiedAt);
        guestSession.setExpiresAt(LocalDateTime.now().plusMinutes(45));
        guestSession.setIpAddressHash(ipHash);
        guestSession.setDeviceFingerprintHash(deviceHash);
        guestSession.setStatus(verificationStatus.name());
        Object locationId = session.getAttribute("shippingLocationId");
        if (locationId instanceof Long id) {
            guestSession.setLocationSessionId(id);
        }
        sessionService.store(session, guestSession);
    }

    private boolean dispatchOtp(OtpVerification verification, String otp, int otpTtlMinutes) {
        MessageDispatchRequest request = new MessageDispatchRequest();
        request.setEventType(MessageEventType.GUEST_CHECKOUT_OTP);
        request.setChannel(MessageChannel.SMS);
        request.setMessageType(MessageType.TRANSACTIONAL);
        request.setRecipient(verification.getMobileNumber());
        request.setReceiverMobile(verification.getMobileNumber());
        request.setSubject("Guest checkout OTP");
        request.setBody("Your Universes Commerce guest checkout verification code is " + otp + ". It expires in " + otpTtlMinutes + " minutes.");
        request.setIdempotencyKey("guest-checkout-otp:" + verification.getUuid());
        request.setVariables(Map.of("otp", otp, "ttlMinutes", otpTtlMinutes));
        try {
            var result = messageDispatchService.dispatch(request);
            boolean accepted = result != null
                    && result.isSuccess()
                    && "SENT".equals(result.getStatus());
            if (!accepted) {
                LOGGER.warn("Guest checkout OTP dispatch was not accepted.");
            }
            return accepted;
        } catch (RuntimeException ex) {
            LOGGER.error("Guest checkout OTP dispatch failed unexpectedly.");
            return false;
        }
    }

    private GuestOtpResponse invalidOtpResponse() {
        GuestOtpResponse response = GuestOtpResponse.failure("Invalid or expired verification code.");
        response.setOtpRequired(true);
        response.setMobileVerificationStatus(OtpStatus.FAILED.name());
        return response;
    }

    private void claimSendLimits(
            String mobile,
            String ipHash,
            String deviceHash,
            String httpSessionId,
            LocalDateTime now,
            int resendCooldownSeconds,
            int otpTtlMinutes,
            long chainBaseline) {
        LocalDateTime windowStart = now.minusDays(1);
        int mobileSendLimit = storeOperationModeService.guestOtpDailySendLimit();
        LocalDateTime cooldownStart = now.minusSeconds(Math.max(resendCooldownSeconds, 1));
        List<VelocityLimitClaim> claims = new ArrayList<>();
        claims.add(new VelocityLimitClaim(
                VelocityCounterScope.OTP_GUEST_MOBILE_COOLDOWN,
                mobile,
                1,
                Duration.ofSeconds(Math.max(resendCooldownSeconds, 1)),
                repository.countByMobileNumberAndPurposeAndCreatedAtAfter(
                        mobile, OtpPurpose.GUEST_CHECKOUT, cooldownStart)));
        long purposeDailyCount = repository.countByMobileNumberAndPurposeAndCreatedAtAfter(
                mobile, OtpPurpose.GUEST_CHECKOUT, windowStart);
        claims.add(new VelocityLimitClaim(
                VelocityCounterScope.OTP_GUEST_MOBILE_CHAIN,
                mobile,
                MAX_RESENDS + 1,
                Duration.ofMinutes(Math.max(otpTtlMinutes, 1)),
                chainBaseline));
        claims.add(new VelocityLimitClaim(
                VelocityCounterScope.OTP_GUEST_MOBILE_DAILY,
                mobile,
                mobileSendLimit,
                Duration.ofDays(1),
                repository.countByMobileNumberAndCreatedAtAfter(mobile, windowStart)));
        if (ipHash != null) {
            claims.add(new VelocityLimitClaim(
                    VelocityCounterScope.OTP_IP_DAILY,
                    ipHash,
                    IP_DAILY_SEND_LIMIT,
                    Duration.ofDays(1),
                    repository.countByIpAddressHashAndCreatedAtAfter(ipHash, windowStart)));
        }
        if (deviceHash != null) {
            claims.add(new VelocityLimitClaim(
                    VelocityCounterScope.OTP_DEVICE_DAILY,
                    deviceHash,
                    DEVICE_DAILY_SEND_LIMIT,
                    Duration.ofDays(1),
                    repository.countByDeviceFingerprintHashAndCreatedAtAfter(deviceHash, windowStart)));
        }
        if (httpSessionId != null) {
            claims.add(new VelocityLimitClaim(
                    VelocityCounterScope.OTP_SESSION_DAILY,
                    httpSessionId,
                    mobileSendLimit,
                    Duration.ofDays(1),
                    repository.countByHttpSessionIdAndCreatedAtAfter(httpSessionId, windowStart)));
        }
        if (!orderVelocityService.claimAllWithinLimits(claims)) {
            throw new IllegalStateException("Please wait before requesting another verification code.");
        }
    }

    private void ensureResendAllowed(OtpVerification existing, LocalDateTime now, int resendCooldownSeconds) {
        if (existing.getResendCount() >= MAX_RESENDS) {
            throw new IllegalStateException("Please wait before requesting another verification code.");
        }
        if (existing.getCreatedAt() != null) {
            long seconds = Duration.between(existing.getCreatedAt(), now).getSeconds();
            if (seconds < resendCooldownSeconds) {
                throw new IllegalStateException("Please wait before requesting another verification code.");
            }
        }
    }

    private void expirePendingOtps(String mobile) {
        List<OtpVerification> pending = repository.findByMobileNumberAndPurposeAndStatus(
                mobile,
                OtpPurpose.GUEST_CHECKOUT,
                OtpStatus.PENDING
        );
        for (OtpVerification verification : pending) {
            verification.setStatus(OtpStatus.EXPIRED);
        }
        repository.saveAll(pending);
    }

    private String clientIp(HttpServletRequest request) {
        // Forwarded headers are interpreted by the servlet container only when
        // the deployment has explicitly enabled a trusted proxy strategy.
        // Never trust a raw client-controlled X-Forwarded-For value here.
        return request == null ? null : clean(request.getRemoteAddr());
    }

    private String hash(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(cleaned.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : bytes) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to secure the verification request identifier.", ex);
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
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
