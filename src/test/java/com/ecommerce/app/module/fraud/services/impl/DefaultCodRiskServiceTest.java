package com.ecommerce.app.module.fraud.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudGuardResult;
import com.ecommerce.app.module.fraud.model.CodRiskProfile;
import com.ecommerce.app.module.fraud.repository.CodRiskProfileRepository;
import com.ecommerce.app.module.fraud.repository.CustomerRiskProfileRepository;
import com.ecommerce.app.module.fraud.repository.FraudEventLogRepository;
import com.ecommerce.app.module.fraud.services.FraudConfigurationService;
import com.ecommerce.app.module.fraud.services.FraudEventPublisher;
import com.ecommerce.app.module.fraud.support.FraudHashingSupport;
import com.ecommerce.app.module.order.model.OrderPaymentPlan;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.shipping.model.ShipmentStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultCodRiskServiceTest {

    private CodRiskProfileRepository codRiskProfileRepository;
    private SalesOrderRepository salesOrderRepository;
    private FraudConfigurationService configurationService;
    private DefaultCodRiskService service;

    @BeforeEach
    void setUp() {
        codRiskProfileRepository = mock(CodRiskProfileRepository.class);
        CustomerRiskProfileRepository customerRiskProfileRepository = mock(CustomerRiskProfileRepository.class);
        salesOrderRepository = mock(SalesOrderRepository.class);
        configurationService = mock(FraudConfigurationService.class);
        when(configurationService.getMoney(anyString(), any(BigDecimal.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(configurationService.getInt(anyString(), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        service = new DefaultCodRiskService(
                codRiskProfileRepository,
                customerRiskProfileRepository,
                salesOrderRepository,
                configurationService,
                mock(FraudEventLogRepository.class),
                mock(FraudEventPublisher.class),
                mock(CodRiskProfileDeviceResolver.class)
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

    @Test
    void deviceRiskProfileLookupsNeverUseRawIdentifier() {
        when(salesOrderRepository.countByCustomer_Id(10L)).thenReturn(1L);
        FraudContext context = new FraudContext();
        context.setDeviceIdentifier("raw-browser-token");
        String expectedHash = FraudHashingSupport.sha256("raw-browser-token");

        service.checkCodCheckoutEligibility(
                10L,
                null,
                new BigDecimal("100.00"),
                "FULL_COD",
                true,
                context
        );

        verify(codRiskProfileRepository).existsByDeviceIdentifierAndCodDisabledTrue(expectedHash);
        verify(codRiskProfileRepository).findByDeviceIdentifier(expectedHash);
    }

    @Test
    void codDisabledLegacyLocalMobileProfileMatchesCanonicalCheckoutMobile() {
        when(salesOrderRepository.countByCustomer_Id(10L)).thenReturn(1L);
        String legacyLocalHash = FraudHashingSupport.sha256("01712345678");
        when(codRiskProfileRepository.existsByMobileHashAndCodDisabledTrue(legacyLocalHash))
                .thenReturn(true);
        FraudContext context = new FraudContext();
        context.getMetadata().put("mobileNumber", "+8801712345678");

        FraudGuardResult result = service.checkCodCheckoutEligibility(
                10L,
                null,
                new BigDecimal("100.00"),
                "FULL_COD",
                true,
                context
        );

        assertFalse(result.isAllowed());
        verify(codRiskProfileRepository)
                .existsByMobileHashAndCodDisabledTrue(legacyLocalHash);
    }

    @Test
    void partialPrepaymentAggregatesCanonicalAndLegacyMobileRisk() {
        when(salesOrderRepository.countByCustomer_Id(10L)).thenReturn(1L);
        when(configurationService.getInt(
                org.mockito.ArgumentMatchers.eq("fraud.cod.high_risk_partial_prepayment_rto_count"),
                anyInt())).thenReturn(2);
        CodRiskProfile canonical = mobileProfile(
                FraudHashingSupport.canonicalBangladeshMobileHash("01712345678"), 1, 0);
        CodRiskProfile legacy = mobileProfile(FraudHashingSupport.sha256("01712345678"), 1, 0);
        when(codRiskProfileRepository.findAllByMobileHashIn(any())).thenReturn(List.of(canonical, legacy));
        FraudContext context = mobileContext();

        FraudGuardResult result = service.checkCodCheckoutEligibility(
                10L,
                null,
                new BigDecimal("100.00"),
                "FULL_COD",
                true,
                context);

        assertFalse(result.isAllowed());
        assertTrue(result.getReason().contains("partial advance"));
    }

    @Test
    void shipmentOutcomeWritesCanonicalOnlyAndDisablesOnAggregateLegacyRisk() {
        CodRiskProfile canonical = mobileProfile(
                FraudHashingSupport.canonicalBangladeshMobileHash("01712345678"), 0, 0);
        CodRiskProfile legacy = mobileProfile(FraudHashingSupport.sha256("01712345678"), 1, 0);
        when(codRiskProfileRepository.findAllByMobileHashIn(any())).thenReturn(List.of(canonical, legacy));
        SalesOrder order = order(1L, OrderPaymentPlan.FULL_COD);

        service.recordCodShipmentOutcome(order, ShipmentStatus.RETURNED, "recipient unavailable", mobileContext());

        assertTrue(canonical.isCodDisabled());
        assertTrue(canonical.getCodRtoCount() == 1L);
        assertTrue(legacy.getCodRtoCount() == 1L);
        verify(codRiskProfileRepository).save(canonical);
    }

    @Test
    void prepaidRecoveryClearsEquivalentLegacyDisabledFlagWithoutDuplicatingCounters() {
        CodRiskProfile canonical = mobileProfile(
                FraudHashingSupport.canonicalBangladeshMobileHash("01712345678"), 0, 0);
        canonical.setSuccessfulPrepaidOrderCount(2L);
        CodRiskProfile legacy = mobileProfile(FraudHashingSupport.sha256("01712345678"), 0, 0);
        legacy.setCodDisabled(true);
        when(codRiskProfileRepository.findAllByMobileHashIn(any())).thenReturn(List.of(canonical, legacy));
        SalesOrder order = order(2L, OrderPaymentPlan.FULL_PREPAID);

        service.recordSuccessfulPrepaidOrder(order, mobileContext());

        assertFalse(canonical.isCodDisabled());
        assertFalse(legacy.isCodDisabled());
        assertTrue(canonical.getSuccessfulPrepaidOrderCount() == 3L);
        assertTrue(legacy.getSuccessfulPrepaidOrderCount() == 0L);
        verify(codRiskProfileRepository).save(legacy);
    }

    @Test
    void successfulCodDeliveryDoesNotReDisableARecoveredProfileFromHistoricFailures() {
        CodRiskProfile canonical = mobileProfile(
                FraudHashingSupport.canonicalBangladeshMobileHash("01712345678"), 3, 0);
        canonical.setCodDisabled(false);
        when(codRiskProfileRepository.findAllByMobileHashIn(any())).thenReturn(List.of(canonical));
        SalesOrder order = order(3L, OrderPaymentPlan.FULL_COD);

        service.recordCodShipmentOutcome(order, ShipmentStatus.DELIVERED, null, mobileContext());

        assertFalse(canonical.isCodDisabled());
        assertTrue(canonical.getCodSuccessCount() == 1L);
    }

    private FraudContext mobileContext() {
        FraudContext context = new FraudContext();
        context.getMetadata().put("mobileNumber", "+8801712345678");
        return context;
    }

    private CodRiskProfile mobileProfile(String hash, long rtoCount, long refusalCount) {
        CodRiskProfile profile = new CodRiskProfile();
        profile.setMobileHash(hash);
        profile.setCodRtoCount(rtoCount);
        profile.setDeliveryRefusalCount(refusalCount);
        return profile;
    }

    private SalesOrder order(Long id, OrderPaymentPlan paymentPlan) {
        SalesOrder order = new SalesOrder();
        order.setId(id);
        order.setPaymentPlan(paymentPlan);
        return order;
    }
}
