package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.CodRiskProfile;
import com.ecommerce.app.module.fraud.repository.CodRiskProfileRepository;
import com.ecommerce.app.module.fraud.repository.CustomerRiskProfileRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.services.FraudConfigurationService;
import com.ecommerce.app.module.fraud.services.FraudEventPublisher;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultCodRiskServiceTest {

    private CodRiskProfileRepository codRiskProfileRepository;
    private SalesOrderRepository salesOrderRepository;
    private DefaultCodRiskService service;

    @BeforeEach
    void setUp() {
        codRiskProfileRepository = mock(CodRiskProfileRepository.class);
        CustomerRiskProfileRepository customerRiskProfileRepository = mock(CustomerRiskProfileRepository.class);
        salesOrderRepository = mock(SalesOrderRepository.class);
        FraudConfigurationService configurationService = mock(FraudConfigurationService.class);
        when(configurationService.getMoney(anyString(), any(BigDecimal.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        service = new DefaultCodRiskService(
                codRiskProfileRepository,
                customerRiskProfileRepository,
                salesOrderRepository,
                configurationService,
                mock(FraudEventLogRepository.class),
                mock(FraudEventPublisher.class)
        );
    }

    @Test
    void prepaidCheckoutDoesNotRequireCodMobileVerification() {
        FraudGuardResult result = service.checkCodCheckoutEligibility(
                10L,
                20L,
                new BigDecimal("20000.00"),
                "FULL_PREPAID",
                false,
                new FraudContext()
        );

        assertTrue(result.isAllowed());
    }

    @Test
    void codCheckoutFailsClosedWhenRequiredMobileProofIsNotSatisfied() {
        FraudGuardResult result = service.checkCodCheckoutEligibility(
                10L,
                null,
                new BigDecimal("100.00"),
                "FULL_COD",
                false,
                new FraudContext()
        );

        assertFalse(result.isAllowed());
        assertTrue(result.getReason().contains("Mobile OTP verification"));
    }

    @Test
    void aggregateFirstOrderLimitCannotBeBypassedByVendorSplitting() {
        when(salesOrderRepository.countByCustomer_Id(10L)).thenReturn(0L);

        FraudGuardResult result = service.checkCodCheckoutEligibility(
                10L,
                null,
                new BigDecimal("5000.01"),
                "FULL_COD",
                true,
                new FraudContext()
        );

        assertFalse(result.isAllowed());
        assertTrue(result.getReason().contains("First Cash on Delivery order"));
    }

    @Test
    void vendorSpecificLimitIsAppliedToVendorNetPayable() {
        when(salesOrderRepository.countByCustomer_Id(10L)).thenReturn(1L);
        CodRiskProfile vendorProfile = new CodRiskProfile();
        vendorProfile.setVendorId(20L);
        vendorProfile.setVendorCodLimit(new BigDecimal("999.99"));
        when(codRiskProfileRepository.findByVendorId(20L)).thenReturn(Optional.of(vendorProfile));

        FraudGuardResult result = service.checkCodCheckoutEligibility(
                10L,
                20L,
                new BigDecimal("1000.00"),
                "FULL_COD",
                true,
                new FraudContext()
        );

        assertFalse(result.isAllowed());
        assertTrue(result.getReason().contains("vendor COD limit"));
    }
}
