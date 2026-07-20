package com.ecommerce.app.module.fraud.services;

public interface CustomerRiskProfileService {

    void refreshCustomerProfile(Long customerId);

    boolean isTrustedCustomer(Long customerId);

    boolean isBlacklistedCustomer(Long customerId);
}
