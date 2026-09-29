package com.ecommerce.app.module.checkout.guest.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.checkout.guest.dto.GuestOtpResponse;
import com.ecommerce.app.module.checkout.guest.model.OtpPurpose;
import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import com.ecommerce.app.module.checkout.guest.repository.OtpVerificationRepository;
import com.ecommerce.app.module.communication.dto.CommunicationSendResult;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.services.MessageDispatchService;
import com.ecommerce.app.module.fraud.services.OrderVelocityService;
import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class GuestCheckoutOtpServiceTest {

    @Mock
    private OtpVerificationRepository repository;

    @Mock
    private MessageDispatchService messageDispatchService;

    @Mock
    private GuestCheckoutUserResolver userResolver;

    @Mock
    private GuestCheckoutSessionService sessionService;

    @Mock
    private StoreOperationModeService storeOperationModeService;

    @Mock
    private OrderVelocityService orderVelocityService;

    @Test
    void providerExceptionExpiresGuestOtpAndReturnsNoUsableToken() {
        when(storeOperationModeService.isGuestCheckoutAllowed()).thenReturn(true);
        when(storeOperationModeService.isGuestMobileOtpVerificationEnabled()).thenReturn(true);
        when(storeOperationModeService.guestOtpDailySendLimit()).thenReturn(5);
        when(storeOperationModeService.guestOtpExpiryMinutes()).thenReturn(5);
        when(storeOperationModeService.guestOtpResendCooldownSeconds()).thenReturn(60);
        when(repository.findTopByMobileNumberAndPurposeAndStatusOrderByCreatedAtDesc(
                "8801712345678", OtpPurpose.GUEST_CHECKOUT, OtpStatus.PENDING
        )).thenReturn(Optional.empty());
        when(repository.findByMobileNumberAndPurposeAndStatus(
                "8801712345678", OtpPurpose.GUEST_CHECKOUT, OtpStatus.PENDING
        )).thenReturn(List.of());
        when(repository.save(any(OtpVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderVelocityService.claimAllWithinLimits(any()))
                .thenReturn(true);
        when(messageDispatchService.dispatch(any(MessageDispatchRequest.class)))
                .thenThrow(new IllegalStateException("provider secret failure"));
        GuestCheckoutOtpService service = new GuestCheckoutOtpService(
                repository,
                new MobileNumberNormalizationService(),
                new BCryptPasswordEncoder(4),
                messageDispatchService,
                userResolver,
                sessionService,
                storeOperationModeService,
                orderVelocityService
        );

        GuestOtpResponse response = service.sendOtp(
                "01712345678",
                "guest-device",
                new MockHttpServletRequest(),
                new MockHttpSession()
        );

        assertFalse(response.isSuccess());
        assertNull(response.getSessionToken());
        assertFalse(response.getMessage().contains("provider secret failure"));
        ArgumentCaptor<OtpVerification> verificationCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(repository, org.mockito.Mockito.times(2)).save(verificationCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                OtpStatus.FAILED,
                verificationCaptor.getAllValues().get(1).getStatus()
        );
    }

    @Test
    void queuedDispatchExpiresGuestOtpAndReturnsNoUsableToken() {
        prepareSendDefaults();
        when(messageDispatchService.dispatch(any(MessageDispatchRequest.class)))
                .thenReturn(CommunicationSendResult.queued(41L, "queued"));
        GuestCheckoutOtpService service = service();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.14");
        request.addHeader("X-Forwarded-For", "198.51.100.99");
        GuestOtpResponse response = service.sendOtp(
                "01712345678",
                "guest-device",
                request,
                new MockHttpSession()
        );

        assertFalse(response.isSuccess());
        assertNull(response.getSessionToken());
        ArgumentCaptor<OtpVerification> verificationCaptor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(repository, org.mockito.Mockito.times(2)).save(verificationCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                OtpStatus.FAILED,
                verificationCaptor.getAllValues().get(1).getStatus()
        );
        verify(repository).countByIpAddressHashAndCreatedAtAfter(
                eq(sha256("203.0.113.14")), any());
        verify(repository, never()).countByIpAddressHashAndCreatedAtAfter(
                eq(sha256("198.51.100.99")), any());
    }

    @Test
    void supersededGuestOtpCannotBeVerifiedEvenIfItsHashAndBindingAreValid() {
        GuestCheckoutOtpService service = service();
        MockHttpSession session = new MockHttpSession();
        OtpVerification stale = new OtpVerification();
        stale.setMobileNumber("8801712345678");
        stale.setPurpose(OtpPurpose.GUEST_CHECKOUT);
        stale.setStatus(OtpStatus.PENDING);
        stale.setSessionToken("stale-token");
        stale.setHttpSessionId(session.getId());
        stale.setOtpHash(new BCryptPasswordEncoder(4).encode("123456"));
        stale.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        OtpVerification latest = new OtpVerification();
        latest.setMobileNumber(stale.getMobileNumber());
        latest.setPurpose(OtpPurpose.GUEST_CHECKOUT);
        latest.setSessionToken("latest-token");
        when(repository.findBySessionTokenForUpdate("stale-token")).thenReturn(Optional.of(stale));
        when(repository.findTopByMobileNumberAndPurposeOrderByIdDesc(
                stale.getMobileNumber(), OtpPurpose.GUEST_CHECKOUT)).thenReturn(Optional.of(latest));

        GuestOtpResponse response = service.verifyOtp(
                "stale-token",
                "123456",
                null,
                new MockHttpServletRequest(),
                session);

        assertFalse(response.isSuccess());
        verify(repository, never()).save(stale);
    }

    private void prepareSendDefaults() {
        when(storeOperationModeService.isGuestCheckoutAllowed()).thenReturn(true);
        when(storeOperationModeService.isGuestMobileOtpVerificationEnabled()).thenReturn(true);
        when(storeOperationModeService.guestOtpDailySendLimit()).thenReturn(5);
        when(storeOperationModeService.guestOtpExpiryMinutes()).thenReturn(5);
        when(storeOperationModeService.guestOtpResendCooldownSeconds()).thenReturn(60);
        when(repository.findTopByMobileNumberAndPurposeAndStatusOrderByCreatedAtDesc(
                "8801712345678", OtpPurpose.GUEST_CHECKOUT, OtpStatus.PENDING
        )).thenReturn(Optional.empty());
        when(repository.findByMobileNumberAndPurposeAndStatus(
                "8801712345678", OtpPurpose.GUEST_CHECKOUT, OtpStatus.PENDING
        )).thenReturn(List.of());
        when(repository.save(any(OtpVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderVelocityService.claimAllWithinLimits(any()))
                .thenReturn(true);
    }

    private GuestCheckoutOtpService service() {
        return new GuestCheckoutOtpService(
                repository,
                new MobileNumberNormalizationService(),
                new BCryptPasswordEncoder(4),
                messageDispatchService,
                userResolver,
                sessionService,
                storeOperationModeService,
                orderVelocityService
        );
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
