package com.ecommerce.app.module.user.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class SessionCredentialVersionServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private SessionRegistry sessionRegistry;

    private SessionCredentialVersionService service;
    private Users user;

    @BeforeEach
    void setUp() {
        service = new SessionCredentialVersionService(usersRepository, sessionRegistry);
        user = new Users();
        user.setId(42L);
        user.setEmail("owner@example.com");
        user.setCredentialVersion(4L);
    }

    @Test
    void passwordChangeAdvancesVersionAndExpiresRegisteredSessions() {
        User principal = new User("owner@example.com", "encoded", List.of());
        SessionInformation session = new SessionInformation(principal, "session-1", new Date());
        when(usersRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(user));
        when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(principal));
        when(sessionRegistry.getAllSessions(principal, false)).thenReturn(List.of(session));

        assertTrue(service.updatePassword(42L, "new-encoded", true));

        assertEquals("new-encoded", user.getPassword());
        assertEquals(5L, user.getCredentialVersion());
        assertTrue(session.isExpired());
        verify(usersRepository).save(user);
    }

    @Test
    void accessEpochBumpAlsoExpiresSessionsWithoutTouchingPassword() {
        User principal = new User("owner@example.com", "encoded", List.of());
        SessionInformation session = new SessionInformation(principal, "session-2", new Date());
        when(usersRepository.findByIdForUpdate(42L)).thenReturn(Optional.of(user));
        when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(principal));
        when(sessionRegistry.getAllSessions(principal, false)).thenReturn(List.of(session));

        assertTrue(service.bumpCredentialVersion(42L));

        assertEquals(5L, user.getCredentialVersion());
        assertTrue(session.isExpired());
        verify(usersRepository).save(any(Users.class));
    }

    @Test
    void successfulLoginBindsCurrentEpochToSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                "owner@example.com", null, List.of());
        when(usersRepository.findCredentialVersionByEmailIgnoreCase("owner@example.com"))
                .thenReturn(Optional.of(4L));

        service.captureCurrentVersion(request, authentication);

        assertEquals(4L, request.getSession(false).getAttribute(
                SessionCredentialVersionService.SESSION_CREDENTIAL_VERSION_ATTRIBUTE));
    }
}
