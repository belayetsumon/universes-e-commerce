package com.ecommerce.app.module.user.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.user.model.PasswordResetToken;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.PasswordResetTokenRepository;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordResetEmailSender emailSender;

    private PasswordResetService service;
    private Users user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                usersRepository,
                tokenRepository,
                new BCryptPasswordEncoder(4),
                emailSender,
                30);
        user = new Users();
        user.setId(42L);
        user.setEmail("owner@example.com");
        user.setFirstName("Account");
    }

    @Test
    void requestStoresOnlyDigestAndSendsRawTokenOnlyInLink() {
        when(usersRepository.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.requestReset(" OWNER@example.com ", "https://shop.example/forgotpassword/reset");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(any(), any(), urlCaptor.capture(), anyLong());

        String link = urlCaptor.getValue();
        String rawToken = link.substring(link.indexOf("token=") + "token=".length());
        assertTrue(rawToken.length() >= 40);
        assertNotEquals(rawToken, tokenCaptor.getValue().getTokenHash());
        assertTrue(tokenCaptor.getValue().getTokenHash().matches("[0-9a-f]{64}"));
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(tokenCaptor.getValue().getCreatedAt()));
    }

    @Test
    void resetConsumesTokenBeforeUpdatingPasswordAndRejectsRaces() {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(7L);
        token.setUser(user);
        token.setTokenHash("a".repeat(64));
        token.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(tokenRepository.consumeIfValid(anyLong(), any(), any())).thenReturn(1);
        when(usersRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(user));

        assertTrue(service.resetPassword("valid-token", "new-password-123"));
        verify(usersRepository).save(user);
        assertTrue(user.isPasswordConfigured());

        when(tokenRepository.consumeIfValid(anyLong(), any(), any())).thenReturn(0);
        assertFalse(service.resetPassword("valid-token", "another-password-123"));
    }

    @Test
    void unknownAndBlankRequestsRemainNeutralWithoutIssuingCapabilities() {
        when(usersRepository.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());

        service.requestReset("nobody@example.com", "https://shop.example/forgotpassword/reset");
        service.requestReset(" ", "https://shop.example/forgotpassword/reset");

        verify(tokenRepository, org.mockito.Mockito.never()).save(any(PasswordResetToken.class));
        verifyNoInteractions(emailSender);
    }
}
