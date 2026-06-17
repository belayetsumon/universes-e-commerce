package com.ecommerce.app.module.ReferralRewards.repository;

import com.ecommerce.app.module.ReferralRewards.model.PromotionFraudFlag;
import com.ecommerce.app.module.user.model.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionFraudFlagRepository extends JpaRepository<PromotionFraudFlag, Long> {

    List<PromotionFraudFlag> findByCustomerOrderByIdDesc(Users customer);

    boolean existsByCustomerAndSourceTypeAndSourceReferenceAndStatus(
            Users customer,
            String sourceType,
            String sourceReference,
            String status
    );
}
