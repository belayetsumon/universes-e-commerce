package com.ecommerce.app.module.user.componant;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.SessionCredentialVersionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class CredentialVersionFilterTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void matchingVersionPassesThrough() throws Exception {
        CredentialVersionFilter filter = new CredentialVersionFilter(usersRepository);
        MockHttpServletRequest request = requestWithAuthentication(7L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(usersRepository.findCredentialVersionByEmailIgnoreCase("owner@example.com"))
                .thenReturn(Optional.of(7L));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(request.getSession(false) != null);
    }

    @Test
    void changedVersionClearsContextInvalidatesSessionAndRedirects() throws Exception {
        CredentialVersionFilter filter = new CredentialVersionFilter(usersRepository);
        MockHttpServletRequest request = requestWithAuthentication(7L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(usersRepository.findCredentialVersionByEmailIgnoreCase("owner@example.com"))
                .thenReturn(Optional.of(8L));

        filter.doFilter(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertNull(request.getSession(false));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(response.getRedirectedUrl().contains("sessionExpired=true"));
    }

    private MockHttpServletRequest requestWithAuthentication(long version) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/customer/index");
        request.getSession(true).setAttribute(
                SessionCredentialVersionService.SESSION_CREDENTIAL_VERSION_ATTRIBUTE, version);
        User principal = new User("owner@example.com", "encoded", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        return request;
    }
}
