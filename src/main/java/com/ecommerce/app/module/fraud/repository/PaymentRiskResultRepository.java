package com.ecommerce.app.module.fraud.repository;

import com.ecommerce.app.module.fraud.model.PaymentRiskProviderStatus;
import com.ecommerce.app.module.fraud.model.PaymentRiskResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRiskResultRepository extends JpaRepository<PaymentRiskResult, Long> {

    Optional<PaymentRiskResult> findTopByOrderIdOrderByIdDesc(Long orderId);

    List<PaymentRiskResult> findByPaymentTokenHash(String paymentTokenHash);

    long countByProviderStatus(PaymentRiskProviderStatus providerStatus);
}
