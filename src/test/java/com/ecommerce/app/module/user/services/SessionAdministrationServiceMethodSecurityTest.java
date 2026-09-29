package com.ecommerce.app.module.user.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.user.model.LoginHistory;
import com.ecommerce.app.module.user.model.LoginStatus;
import com.ecommerce.app.module.user.ripository.LoginHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SessionAdministrationServiceMethodSecurityTest.TestConfiguration.class)
class SessionAdministrationServiceMethodSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        LoginHistoryRepository loginHistoryRepository() {
            return Mockito.mock(LoginHistoryRepository.class);
        }

        @Bean
        SessionAdministrationService sessionAdministrationService(LoginHistoryRepository loginHistoryRepository) {
            return new SessionAdministrationService(loginHistoryRepository);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private SessionAdministrationService sessionAdministrationService;

    @org.springframework.beans.factory.annotation.Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @BeforeEach
    void resetMocks() {
        reset(loginHistoryRepository);
    }

    @Test
    @WithMockUser(authorities = "platform.security.audit.read")
    void securityAuditPermissionAllowsLoginHistoryAdministrationRead() {
        LoginHistory history = new LoginHistory();
        LocalDateTime fromDateTime = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDateTime = LocalDateTime.of(2026, 8, 3, 0, 0);
        when(loginHistoryRepository.findForAdminListFilters(
                7L,
                "%alice%",
                LoginStatus.ACTIVE,
                fromDateTime,
                toDateTime)).thenReturn(List.of(history));

        List<LoginHistory> result = sessionAdministrationService.findLoginHistoryForAdmin(
                7L,
                " Alice ",
                LoginStatus.ACTIVE,
                "2026-08-01",
                "2026-08-02");

        assertEquals(List.of(history), result);
        verify(loginHistoryRepository).findForAdminListFilters(
                7L,
                "%alice%",
                LoginStatus.ACTIVE,
                fromDateTime,
                toDateTime);
    }

    @Test
    @WithMockUser(authorities = "platform.identity.user.read")
    void identityReadPermissionDoesNotAllowLoginHistoryAdministrationRead() {
        assertThrows(
                AccessDeniedException.class,
                () -> sessionAdministrationService.findLoginHistoryForAdmin(null, null, null, null, null));
        verify(loginHistoryRepository, never()).findForAdminListFilters(null, null, null, null, null);
    }

    @Test
    @WithMockUser(authorities = "admin")
    void legacyAdminBridgeStillAllowsLoginHistoryAdministrationReadDuringMigration() {
        when(loginHistoryRepository.findForAdminListFilters(null, null, null, null, null)).thenReturn(List.of());

        assertEquals(List.of(), sessionAdministrationService.findLoginHistoryForAdmin(null, null, null, null, null));
        verify(loginHistoryRepository).findForAdminListFilters(null, null, null, null, null);
    }

    @Test
    void dateInputNormalizationKeepsInvalidFiltersOutOfTheModel() {
        assertEquals("2026-08-01", sessionAdministrationService.normalizeDateInput(" 2026-08-01 "));
        assertEquals("", sessionAdministrationService.normalizeDateInput("not-a-date"));
        assertEquals("", sessionAdministrationService.normalizeDateInput(null));
    }
}