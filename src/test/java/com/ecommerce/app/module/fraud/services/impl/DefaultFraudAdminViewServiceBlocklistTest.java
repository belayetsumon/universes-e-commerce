package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.dto.FraudBlocklistRequest;
import com.ecommerce.app.module.fraud.model.FraudBlockType;
import com.ecommerce.app.module.fraud.model.FraudBlocklist;
import com.ecommerce.app.module.fraud.model.FraudBlockScope;
import com.ecommerce.app.module.fraud.repository.FraudAssessmentRepository;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.repository.FraudCaseRepository;
import com.ecommerce.app.module.fraud.repository.FraudConfigurationRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.repository.FraudEvidenceRepository;
import com.ecommerce.app.module.fraud.repository.FraudReviewHistoryRepository;
import com.ecommerce.app.module.fraud.repository.FraudRuleExecutionRepository;
import com.ecommerce.app.module.fraud.repository.FraudRuleRepository;
import com.ecommerce.app.module.fraud.repository.FraudSignalRepository;
import com.ecommerce.app.module.fraud.repository.VendorRiskProfileRepository;
import com.ecommerce.app.module.fraud.services.FraudAuditService;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.fraud.exception.FraudValidationException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultFraudAdminViewServiceBlocklistTest {

    @Test
    void alreadyHashedDeviceBlocklistValueRemainsCanonical() {
        FraudBlocklistRepository blocklistRepository = mock(FraudBlocklistRepository.class);
        FraudAuditService auditService = mock(FraudAuditService.class);
        DefaultFraudAdminViewService service = new DefaultFraudAdminViewService(
                mock(FraudAssessmentRepository.class),
                mock(FraudCaseRepository.class),
                mock(FraudRuleRepository.class),
                blocklistRepository,
                mock(FraudConfigurationRepository.class),
                mock(FraudSignalRepository.class),
                mock(FraudRuleExecutionRepository.class),
                mock(FraudReviewHistoryRepository.class),
                mock(FraudEvidenceRepository.class),
                mock(FraudEventLogRepository.class),
                mock(VendorRiskProfileRepository.class),
                auditService
        );
        String deviceHash = FraudHashingSupport.sha256("raw-browser-token");
        when(blocklistRepository.findAllByBlockTypeAndHashedValueInAndScopeAndActiveTrue(
                FraudBlockType.DEVICE,
                List.of(deviceHash),
                FraudBlockScope.GLOBAL
        )).thenReturn(List.of());
        when(blocklistRepository.save(any(FraudBlocklist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        FraudBlocklistRequest request = new FraudBlocklistRequest();
        request.setBlockType(FraudBlockType.DEVICE);
        request.setBlockValue(deviceHash.toUpperCase());
        request.setReason("Confirmed abusive device");

        FraudBlocklist saved = service.addBlocklist(request, "fraud-admin");

        assertEquals(deviceHash, saved.getHashedValue());
        verify(blocklistRepository).findAllByBlockTypeAndHashedValueInAndScopeAndActiveTrue(
                FraudBlockType.DEVICE,
                List.of(deviceHash),
                FraudBlockScope.GLOBAL
        );
    }

    @Test
    void mobileBlocklistUsesCanonicalBangladeshHashAndRejectsInvalidInput() {
        FraudBlocklistRepository blocklistRepository = mock(FraudBlocklistRepository.class);
        DefaultFraudAdminViewService service = service(blocklistRepository);
        when(blocklistRepository.findAllByBlockTypeAndHashedValueInAndScopeAndActiveTrue(
                org.mockito.ArgumentMatchers.eq(FraudBlockType.MOBILE_NUMBER),
                org.mockito.ArgumentMatchers.anyCollection(),
                org.mockito.ArgumentMatchers.eq(FraudBlockScope.GLOBAL)
        )).thenReturn(List.of());
        when(blocklistRepository.save(any(FraudBlocklist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        FraudBlocklistRequest request = new FraudBlocklistRequest();
        request.setBlockType(FraudBlockType.MOBILE_NUMBER);
        request.setBlockValue("+880 1712-345678");
        request.setReason("Confirmed abusive mobile");

        FraudBlocklist saved = service.addBlocklist(request, "fraud-admin");

        assertEquals(
                FraudHashingSupport.canonicalBangladeshMobileHash("01712345678"),
                saved.getHashedValue()
        );

        request.setBlockValue("invalid-mobile");
        assertThrows(FraudValidationException.class, () -> service.addBlocklist(request, "fraud-admin"));
    }

    @Test
    void blocklistRejectsAmbiguousScopeAndExpiryContracts() {
        DefaultFraudAdminViewService service = service(mock(FraudBlocklistRepository.class));
        FraudBlocklistRequest request = new FraudBlocklistRequest();
        request.setBlockType(FraudBlockType.EMAIL);
        request.setBlockValue("abuse@example.com");
        request.setReason("Confirmed abuse");

        request.setTemporary(true);
        assertThrows(FraudValidationException.class, () -> service.addBlocklist(request, "fraud-admin"));

        request.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        assertThrows(FraudValidationException.class, () -> service.addBlocklist(request, "fraud-admin"));

        request.setTemporary(false);
        request.setExpiresAt(LocalDateTime.now().plusDays(1));
        assertThrows(FraudValidationException.class, () -> service.addBlocklist(request, "fraud-admin"));

        request.setExpiresAt(null);
        request.setScope(FraudBlockScope.CUSTOMER);
        assertThrows(FraudValidationException.class, () -> service.addBlocklist(request, "fraud-admin"));
    }

    @Test
    void mobileBlocklistRekeysCanonicalEntryAndDeactivatesOnlyGlobalLegacyDuplicate() {
        FraudBlocklistRepository repository = mock(FraudBlocklistRepository.class);
        DefaultFraudAdminViewService service = service(repository);
        String canonicalHash = FraudHashingSupport.canonicalBangladeshMobileHash("01712345678");
        FraudBlocklist legacy = new FraudBlocklist();
        legacy.setHashedValue(FraudHashingSupport.sha256("01712345678"));
        legacy.setScope(FraudBlockScope.GLOBAL);
        legacy.setActive(true);
        FraudBlocklist canonical = new FraudBlocklist();
        canonical.setHashedValue(canonicalHash);
        canonical.setScope(FraudBlockScope.GLOBAL);
        canonical.setActive(true);
        when(repository.findAllByBlockTypeAndHashedValueInAndScopeAndActiveTrue(
                org.mockito.ArgumentMatchers.eq(FraudBlockType.MOBILE_NUMBER),
                org.mockito.ArgumentMatchers.anyCollection(),
                org.mockito.ArgumentMatchers.eq(FraudBlockScope.GLOBAL)))
                .thenReturn(List.of(legacy, canonical));
        when(repository.save(any(FraudBlocklist.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        FraudBlocklistRequest request = new FraudBlocklistRequest();
        request.setBlockType(FraudBlockType.MOBILE_NUMBER);
        request.setBlockValue("+8801712345678");
        request.setReason("Confirmed abuse");

        FraudBlocklist saved = service.addBlocklist(request, "fraud-admin");

        assertEquals(canonicalHash, saved.getHashedValue());
        assertEquals(false, legacy.isActive());
        assertEquals(true, canonical.isActive());
        verify(repository).saveAll(List.of(legacy));
    }

    private DefaultFraudAdminViewService service(FraudBlocklistRepository blocklistRepository) {
        return new DefaultFraudAdminViewService(
                mock(FraudAssessmentRepository.class),
                mock(FraudCaseRepository.class),
                mock(FraudRuleRepository.class),
                blocklistRepository,
                mock(FraudConfigurationRepository.class),
                mock(FraudSignalRepository.class),
                mock(FraudRuleExecutionRepository.class),
                mock(FraudReviewHistoryRepository.class),
                mock(FraudEvidenceRepository.class),
                mock(FraudEventLogRepository.class),
                mock(VendorRiskProfileRepository.class),
                mock(FraudAuditService.class)
        );
    }
}
