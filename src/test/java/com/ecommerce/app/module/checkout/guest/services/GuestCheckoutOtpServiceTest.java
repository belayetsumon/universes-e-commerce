package com.ecommerce.app.module.checkout.guest.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.checkout.guest.dto.GuestOtpResponse;
import com.ecommerce.app.module.checkout.guest.model.OtpPurpose;
import com.ecommerce.app.module.checkout.guest.model.OtpStatus;
import com.ecommerce.app.module.checkout.guest.model.OtpVerification;
import com.ecommerce.app.module.checkout.guest.repository.OtpVerificationRepository;
import com.ecommerce.app.module.communication.dto.MessageDispatchRequest;
import com.ecommerce.app.module.communication.services.MessageDispatchService;
import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import java.util.List;
import java.util.Optional;
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
        when(messageDispatchService.dispatch(any(MessageDispatchRequest.class)))
                .thenThrow(new IllegalStateException("provider secret failure"));
        GuestCheckoutOtpService service = new GuestCheckoutOtpService(
                repository,
                new MobileNumberNormalizationService(),
                new BCryptPasswordEncoder(4),
                messageDispatchService,
                userResolver,
                sessionService,
                storeOperationModeService
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
}
