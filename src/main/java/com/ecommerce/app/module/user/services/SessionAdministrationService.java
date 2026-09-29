package com.ecommerce.app.module.user.services;

import com.ecommerce.app.module.user.model.LoginHistory;
import com.ecommerce.app.module.user.model.LoginStatus;
import com.ecommerce.app.module.user.ripository.LoginHistoryRepository;
import com.ecommerce.app.security.permission.PlatformSecurityAuditPermissions;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionAdministrationService {

    private final LoginHistoryRepository loginHistoryRepository;

    public SessionAdministrationService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize(PlatformSecurityAuditPermissions.CAN_READ)
    public List<LoginHistory> findLoginHistoryForAdmin(
            Long userId,
            String q,
            LoginStatus loginStatus,
            String fromDate,
            String toDate) {
        return loginHistoryRepository.findForAdminListFilters(
                userId,
                normalizeKeyword(q),
                loginStatus,
                parseStartDate(fromDate),
                parseEndDate(toDate));
    }

    public String normalizeDateInput(String value) {
        LocalDate date = parseDateInput(value);
        return date == null ? "" : date.toString();
    }

    private LocalDateTime parseStartDate(String value) {
        LocalDate date = parseDateInput(value);
        return date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime parseEndDate(String value) {
        LocalDate date = parseDateInput(value);
        return date == null ? null : date.plusDays(1).atStartOfDay();
    }

    private LocalDate parseDateInput(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String normalizeKeyword(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
    }
}