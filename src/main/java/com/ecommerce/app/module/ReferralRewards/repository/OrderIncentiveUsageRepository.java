package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.OrderIncentiveUsage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderIncentiveUsageRepository extends JpaRepository<OrderIncentiveUsage, Long> {

    Optional<OrderIncentiveUsage> findByOrderId(String orderId);
}

