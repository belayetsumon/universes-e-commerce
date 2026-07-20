package com.ecommerce.app.module.fraud.services.impl;

import com.ecommerce.app.module.checkout.guest.model.MobileVerificationStatus;
import com.ecommerce.app.module.fraud.dto.FraudContext;
import com.ecommerce.app.module.fraud.dto.FraudSignalResult;
import com.ecommerce.app.module.fraud.model.FraudReasonCode;
import com.ecommerce.app.module.fraud.model.FraudSignalCategory;
import com.ecommerce.app.module.fraud.model.FraudSignalSeverity;
import com.ecommerce.app.module.fraud.repository.CustomerRiskProfileRepository;
import com.ecommerce.app.module.fraud.repository.FraudBlocklistRepository;
import com.ecommerce.app.module.fraud.services.evaluator.CustomerHistorySignalEvaluator;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.module.user.model.Users;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultCustomerHistorySignalEvaluator extends AbstractFraudSignalEvaluator implements CustomerHistorySignalEvaluator {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRiskProfileRepository customerRiskProfileRepository;
    private final FraudBlocklistRepository fraudBlocklistRepository;

    public DefaultCustomerHistorySignalEvaluator(SalesOrderRepository salesOrderRepository,
            CustomerRiskProfileRepository customerRiskProfileRepository,
            FraudBlocklistRepository fraudBlocklistRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerRiskProfileRepository = customerRiskProfileRepository;
        this.fraudBlocklistRepository = fraudBlocklistRepository;
    }

    @Override
    public List<FraudSignalResult> evaluate(SalesOrder order, FraudContext context) {
        List<FraudSignalResult> signals = new ArrayList<>();
        Users customer = order == null ? null : order.getCustomer();
        Long customerId = customer == null ? null : customer.getId();

        boolean mobileVerified = customer != null && customer.isMobileVerified();
        if (order != null && order.getMobileVerificationStatus() != null) {
            mobileVerified = order.getMobileVerificationStatus() == MobileVerificationStatus.VERIFIED;
        }
        signals.add(signal("MOBILE_NOT_VERIFIED", category(), !mobileVerified, 25, FraudSignalSeverity.HIGH,
                FraudReasonCode.MOBILE_NOT_VERIFIED, String.valueOf(!mobileVerified), "customer-history", null));

        boolean emailVerified = customer != null && customer.isEmailVerified();
        signals.add(signal("EMAIL_NOT_VERIFIED", category(), customer != null && !emailVerified, 10, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.EMAIL_NOT_VERIFIED, String.valueOf(!emailVerified), "customer-history", null));

        boolean newAccount = isNewAccount(customer);
        signals.add(signal("ACCOUNT_AGE_HOURS", category(), newAccount, 10, FraudSignalSeverity.MEDIUM,
                FraudReasonCode.NEW_ACCOUNT_HIGH_VALUE, newAccount ? "24_OR_LESS" : "OLDER_THAN_24", "customer-history", null));

        long successfulOrders = customer == null ? 0 : salesOrderRepository.countByCustomer(customer);
        signals.add(signal("SUCCESSFUL_ORDER_COUNT", category(), successfulOrders >= 5, -20, FraudSignalSeverity.LOW,
                null, String.valueOf(successfulOrders), "customer-history", "{\"threshold\":5}"));

        boolean blacklistedCustomer = customerId != null && customerRiskProfileRepository.existsByCustomerIdAndBlacklistedTrue(customerId);
        signals.add(signal("CUSTOMER_BLACKLISTED", category(), blacklistedCustomer, 100, FraudSignalSeverity.CRITICAL,
                null, String.valueOf(blacklistedCustomer), "customer-risk-profile", null));

        String mobileHash = customer == null ? sha256(order == null ? null : order.getMobileNumber()) : sha256(customer.getMobile());
        boolean blacklistedMobile = mobileHash != null
                && fraudBlocklistRepository.existsByBlockTypeAndHashedValueAndActiveTrue(
                        com.ecommerce.app.module.fraud.model.FraudBlockType.MOBILE_NUMBER, mobileHash);
        signals.add(signal("MOBILE_BLACKLISTED", category(), blacklistedMobile, 100, FraudSignalSeverity.CRITICAL,
                FraudReasonCode.MOBILE_BLACKLISTED, String.valueOf(blacklistedMobile), "fraud-blocklist", null));

        return signals;
    }

    private boolean isNewAccount(Users customer) {
        if (customer == null || customer.getCreatedOn() == null) {
            return false;
        }
        Date createdOn = customer.getCreatedOn();
        long ageHours = Duration.between(createdOn.toInstant(), Instant.now()).toHours();
        return ageHours <= 24;
    }
}
