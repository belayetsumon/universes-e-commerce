package com.ecommerce.app.vendor.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.communication.events.CommunicationRequestedEvent;
import com.ecommerce.app.vendor.model.VendorVerifications;
import com.ecommerce.app.vendor.model.Vendorprofile;
import com.ecommerce.app.vendor.repository.VendorVerificationsRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import com.ecommerce.app.vendor.services.VendorVerificationsService.EmailVerificationResult;
import com.ecommerce.app.vendor.services.VendorVerificationsService.MobileVerificationResult;
import com.ecommerce.app.vendor.user.componant.VendorUserContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class VendorVerificationsServiceTest {

    @Mock
    private VendorVerificationsRepository repository;

    @Mock
    private VendorprofileRepository vendorprofileRepository;

    @Mock
    private VendorUserContext vendorUserContext;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private BCryptPasswordEncoder passwordEncoder;
    private VendorVerificationsService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        service = new VendorVerificationsService(
                repository,
                vendorprofileRepository,
                vendorUserContext,
                eventPublisher,
                passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void emailVerificationStoresOnlyTokenHashAndPublishesRawTokenOnce() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("");
        request.setScheme("https");
        request.setServerName("shop.example.test");
        request.setServerPort(443);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Vendorprofile vendor = vendor();
        when(repository.findByVendorprofile_Id(42L)).thenReturn(Optional.empty());
        when(repository.save(any(VendorVerifications.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendorVerifications saved = service.createEmailVerification(vendor);

        assertTrue(saved.getToken().matches("[0-9a-f]{64}"));
        ArgumentCaptor<CommunicationRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(CommunicationRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        String rawLink = String.valueOf(eventCaptor.getValue().getVariables().get("verificationLink"));
        String rawToken = rawLink.substring(rawLink.indexOf("token=") + 6);
        assertFalse(rawToken.equals(saved.getToken()));
        assertEquals(sha256(rawToken), saved.getToken());
    }

    @Test
    void emailVerificationLooksUpHashAndClearsTokenAfterSuccess() {
        String rawToken = "email-token";
        VendorVerifications verification = verification(vendor());
        verification.setToken(sha256(rawToken));
        verification.setTokenCreatedAt(LocalDateTime.now());
        when(repository.findByToken(sha256(rawToken))).thenReturn(Optional.of(verification));

        EmailVerificationResult result = service.verifyEmail(rawToken);

        assertEquals(EmailVerificationResult.VERIFIED, result);
        assertTrue(verification.isEmailVerified());
        assertNull(verification.getToken());
        assertNull(verification.getTokenCreatedAt());
        verify(repository).save(verification);
    }

    @Test
    void mobileOtpStoresHashAndClearsItAfterSuccess() {
        Vendorprofile vendor = vendor();
        when(repository.findByVendorprofile_Id(42L)).thenReturn(Optional.empty());
        when(repository.save(any(VendorVerifications.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VendorVerifications saved = service.createMobileOtp(vendor);

        ArgumentCaptor<CommunicationRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(CommunicationRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        String rawOtp = String.valueOf(eventCaptor.getValue().getVariables().get("otp"));
        assertTrue(rawOtp.matches("\\d{6}"));
        assertFalse(rawOtp.equals(saved.getOtp()));
        assertTrue(passwordEncoder.matches(rawOtp, saved.getOtp()));

        when(repository.findByVendorprofile_Id(42L)).thenReturn(Optional.of(saved));
        MobileVerificationResult result = service.verifyMobile(vendor, rawOtp);

        assertEquals(MobileVerificationResult.VERIFIED, result);
        assertTrue(saved.isMobileVerified());
        assertNull(saved.getOtp());
        assertNull(saved.getOtpCreatedAt());
    }

    private static Vendorprofile vendor() {
        Vendorprofile vendor = new Vendorprofile();
        vendor.setId(42L);
        vendor.setEmail("vendor@example.test");
        vendor.setPhone("01712345678");
        vendor.setCompanyName("Test Vendor");
        return vendor;
    }

    private static VendorVerifications verification(Vendorprofile vendor) {
        VendorVerifications verification = new VendorVerifications();
        verification.setVendorprofile(vendor);
        verification.setEmail(vendor.getEmail());
        verification.setMobile(vendor.getPhone());
        return verification;
    }

    private static String sha256(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
