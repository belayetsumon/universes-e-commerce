package com.ecommerce.app.module.checkout.customer.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.checkout.customer.dto.CustomerCodMobileOtpResponse;
import com.ecommerce.app.module.checkout.guest.model.OtpPurpose;
import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import com.ecommerce.app.module.checkout.guest.repository.OtpVerificationRepository;
import com.ecommerce.app.module.checkout.guest.services.MobileNumberNormalizationService;
import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.services.MessageDispatchService;
import com.ecommerce.app.module.fraud.services.OrderVelocityService;
import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CustomerCodMobileVerificationServiceTest {

    @Mock
    private OtpVerificationRepository otpRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private MessageDispatchService messageDispatchService;

    @Mock
    private StoreOperationModeService storeOperationModeService;

    @Mock
    private OrderVelocityService orderVelocityService;

    private final MobileNumberNormalizationService mobileNumberService = new MobileNumberNormalizationService();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private CustomerCodMobileVerificationService service;

    @BeforeEach
    void setUp() {
        service = new CustomerCodMobileVerificationService(
                otpRepository,
                usersRepository,
                mobileNumberService,
                passwordEncoder,
                messageDispatchService,
                storeOperationModeService,
                orderVelocityService
        );
        org.mockito.Mockito.lenient().when(orderVelocityService.claimAllWithinLimits(any()))
                .thenReturn(true);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentMobileProofRequiresFlagTimestampAndMatchingNormalizedSnapshot() {
        Users user = customer();
        user.setMobile("01712345678");
        user.setMobileVerified(true);
        user.setMobileVerifiedAt(LocalDateTime.now());
        user.setMobileVerifiedNumber("+8801712345678");

        assertTrue(service.isCurrentMobileVerified(user));

        user.setMobile("01812345678");
        assertFalse(service.isCurrentMobileVerified(user));

        when(storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled()).thenReturn(false);
        assertTrue(service.isCodMobileVerificationSatisfied(user));
    }

    @Test
    void codProofAuthorizesOnlyTheSameNormalizedFulfillmentMobile() {
        Users user = customer();
        user.setMobile("01712345678");
        user.setMobileVerified(true);
        user.setMobileVerifiedAt(LocalDateTime.now());
        user.setMobileVerifiedNumber("8801712345678");

        assertTrue(service.isVerifiedCodContactMobile(user, "+880 1712-345678"));
        assertFalse(service.isVerifiedCodContactMobile(user, "01812345678"));
        assertFalse(service.isVerifiedCodContactMobile(user, null));
    }

    @Test
    void mobileChangeNormalizesAndInvalidatesExistingProof() {
        Users user = customer();
        user.setMobile("01712345678");
        user.setMobileVerified(true);
        user.setMobileVerifiedAt(LocalDateTime.now());
        user.setMobileVerifiedNumber("8801712345678");

        service.updateMobileAndInvalidateVerificationIfChanged(user, "+8801812345678");

        assertEquals("8801812345678", user.getMobile());
        assertFalse(user.isMobileVerified());
        assertNull(user.getMobileVerifiedAt());
        assertNull(user.getMobileVerifiedNumber());
    }

    @Test
    void acceptedDispatchStoresOnlyHashAndReturnsBcryptBackedSession() {
        Users user = authenticatedCustomer();
        prepareSendDefaults(user);
        when(messageDispatchService.dispatch(any(MessageDispatchRequest.class)))
                .thenReturn(CommunicationSendResult.sent("200", "sent"));
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.12");

        CustomerCodMobileOtpResponse response = service.sendOtp("browser-device-123", request, session);

        assertTrue(response.isSuccess());
        assertNotNull(response.getSessionToken());
        String deviceHash = (String) session.getAttribute(
                CustomerCodMobileVerificationService.CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE
        );
        assertNotNull(deviceHash);
        assertEquals(64, deviceHash.length());
        assertNotEquals("browser-device-123", deviceHash);

        ArgumentCaptor<MessageDispatchRequest> dispatchCaptor = ArgumentCaptor.forClass(MessageDispatchRequest.class);
        verify(messageDispatchService).dispatch(dispatchCaptor.capture());
        String otp = (String) dispatchCaptor.getValue().getVariables().get("otp");

        ArgumentCaptor<OtpVerification> verificationCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository).save(verificationCaptor.capture());
        OtpVerification verification = verificationCaptor.getValue();
        assertTrue(passwordEncoder.matches(otp, verification.getOtpHash()));
        assertNotEquals(otp, verification.getOtpHash());
        assertEquals(user.getId(), verification.getUserId());
    }

    @Test
    void queuedDispatchIsRejectedBecauseOtpMustBeDeliveredBeforeItCanBeVerified() {
        Users user = authenticatedCustomer();
        prepareSendDefaults(user);
        when(messageDispatchService.dispatch(any(MessageDispatchRequest.class)))
                .thenReturn(CommunicationSendResult.queued(41L, "queued"));

        CustomerCodMobileOtpResponse response = service.sendOtp(
                "browser-device-123",
                new MockHttpServletRequest(),
                new MockHttpSession()
        );

        assertFalse(response.isSuccess());
        assertNull(response.getSessionToken());
        ArgumentCaptor<OtpVerification> verificationCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository, org.mockito.Mockito.times(2)).save(verificationCaptor.capture());
        assertEquals(OtpStatus.FAILED, verificationCaptor.getAllValues().get(1).getStatus());
    }

    @Test
    void providerExceptionExpiresOtpAndDoesNotExposeSessionToken() {
        Users user = authenticatedCustomer();
        prepareSendDefaults(user);
        when(messageDispatchService.dispatch(any(MessageDispatchRequest.class)))
                .thenThrow(new IllegalStateException("provider secret failure"));
        MockHttpSession session = new MockHttpSession();

        CustomerCodMobileOtpResponse response = service.sendOtp(
                "browser-device-123",
                new MockHttpServletRequest(),
                session
        );

        assertFalse(response.isSuccess());
        assertNull(response.getSessionToken());
        assertFalse(response.getMessage().contains("provider secret failure"));
        assertNull(session.getAttribute(
                CustomerCodMobileVerificationService.CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE
        ));

        ArgumentCaptor<OtpVerification> verificationCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepository, org.mockito.Mockito.times(2)).save(verificationCaptor.capture());
        assertEquals(OtpStatus.FAILED, verificationCaptor.getAllValues().get(1).getStatus());
    }

    @Test
    void verifyRequiresMatchingUserSessionCurrentMobileAndDevice() throws Exception {
        Users user = authenticatedCustomer();
        when(storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled()).thenReturn(true);
        when(usersRepository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                CustomerCodMobileVerificationService.CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE,
                sha256("wrong-device")
        );
        OtpVerification verification = pendingVerification(user, session, "correct-device", "123456");
        when(otpRepository.findBySessionTokenForUpdate(verification.getSessionToken()))
                .thenReturn(Optional.of(verification));

        CustomerCodMobileOtpResponse response = service.verifyOtp(
                verification.getSessionToken(),
                "123456",
                "wrong-device",
                session
        );

        assertFalse(response.isSuccess());
        assertFalse(user.isMobileVerified());
        assertNull(session.getAttribute(
                CustomerCodMobileVerificationService.CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE
        ));
        verify(usersRepository, never()).save(any(Users.class));
    }

    @Test
    void successfulVerificationPersistsCurrentNumberProofAndBoundDeviceHash() throws Exception {
        Users user = authenticatedCustomer();
        when(storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled()).thenReturn(true);
        when(storeOperationModeService.guestOtpMaximumAttempts()).thenReturn(5);
        when(usersRepository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        MockHttpSession session = new MockHttpSession();
        OtpVerification verification = pendingVerification(user, session, "correct-device", "123456");
        when(otpRepository.findBySessionTokenForUpdate(verification.getSessionToken()))
                .thenReturn(Optional.of(verification));

        CustomerCodMobileOtpResponse response = service.verifyOtp(
                verification.getSessionToken(),
                "123456",
                "correct-device",
                session
        );

        assertTrue(response.isSuccess());
        assertTrue(user.isMobileVerified());
        assertNotNull(user.getMobileVerifiedAt());
        assertEquals("8801712345678", user.getMobileVerifiedNumber());
        assertEquals(OtpStatus.VERIFIED, verification.getStatus());
        assertEquals(sha256("correct-device"), session.getAttribute(
                CustomerCodMobileVerificationService.CUSTOMER_COD_DEVICE_HASH_SESSION_ATTRIBUTE
        ));
        verify(usersRepository).save(user);
    }

    @Test
    void wrongOtpIncrementsAttemptWithoutThrowingSoTransactionCanCommit() throws Exception {
        Users user = authenticatedCustomer();
        when(storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled()).thenReturn(true);
        when(storeOperationModeService.guestOtpMaximumAttempts()).thenReturn(5);
        when(usersRepository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        MockHttpSession session = new MockHttpSession();
        OtpVerification verification = pendingVerification(user, session, "correct-device", "123456");
        when(otpRepository.findBySessionTokenForUpdate(verification.getSessionToken()))
                .thenReturn(Optional.of(verification));

        CustomerCodMobileOtpResponse response = service.verifyOtp(
                verification.getSessionToken(),
                "654321",
                "correct-device",
                session
        );

        assertFalse(response.isSuccess());
        assertEquals(1, verification.getAttemptCount());
        assertEquals(OtpStatus.PENDING, verification.getStatus());
        verify(otpRepository).save(verification);
    }

    private Users authenticatedCustomer() {
        Users user = customer();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user.getEmail(), "n/a", List.of())
        );
        return user;
    }

    private Users customer() {
        Users user = new Users();
        user.setId(17L);
        user.setEmail("customer@example.com");
        user.setMobile("01712345678");
        return user;
    }

    private void prepareSendDefaults(Users user) {
        when(storeOperationModeService.isRegisteredCustomerCodMobileVerificationEnabled()).thenReturn(true);
        when(storeOperationModeService.guestOtpDailySendLimit()).thenReturn(5);
        when(storeOperationModeService.guestOtpExpiryMinutes()).thenReturn(5);
        when(storeOperationModeService.guestOtpResendCooldownSeconds()).thenReturn(60);
        when(usersRepository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        when(otpRepository.findTopByUserIdAndPurposeAndStatusOrderByCreatedAtDesc(
                user.getId(), OtpPurpose.CUSTOMER_COD, OtpStatus.PENDING
        )).thenReturn(Optional.empty());
        when(otpRepository.findByUserIdAndPurposeAndStatus(
                user.getId(), OtpPurpose.CUSTOMER_COD, OtpStatus.PENDING
        )).thenReturn(List.of());
        when(otpRepository.save(any(OtpVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private OtpVerification pendingVerification(
            Users user,
            MockHttpSession session,
            String deviceFingerprint,
            String otp) throws Exception {
        OtpVerification verification = new OtpVerification();
        verification.setUserId(user.getId());
        verification.setMobileNumber("8801712345678");
        verification.setPurpose(OtpPurpose.CUSTOMER_COD);
        verification.setStatus(OtpStatus.PENDING);
        verification.setOtpHash(passwordEncoder.encode(otp));
        verification.setSessionToken("verification-session-token");
        verification.setHttpSessionId(session.getId());
        verification.setDeviceFingerprintHash(sha256(deviceFingerprint));
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        org.mockito.Mockito.lenient().when(
                otpRepository.findTopByUserIdAndPurposeOrderByIdDesc(user.getId(), OtpPurpose.CUSTOMER_COD)
        ).thenReturn(Optional.of(verification));
        return verification;
    }

    private String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))
        );
    }
}
