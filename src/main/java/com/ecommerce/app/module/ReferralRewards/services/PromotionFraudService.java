package com.ecommerce.app.module.ReferralRewards.services;

import com.ecommerce.app.module.ReferralRewards.model.PromotionFraudFlag;
import com.ecommerce.app.module.ReferralRewards.repository.PromotionFraudFlagRepository;
import com.ecommerce.app.module.user.model.Users;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionFraudService {

    public static final String STATUS_OPEN = "OPEN";
    public static final String SEVERITY_HIGH = "HIGH";

    private final PromotionFraudFlagRepository promotionFraudFlagRepository;

    public PromotionFraudService(PromotionFraudFlagRepository promotionFraudFlagRepository) {
        this.promotionFraudFlagRepository = promotionFraudFlagRepository;
    }

    @Transactional
    public boolean flagSelfReferralIfMatched(Users customer, Users referrer) {
        if (customer == null || referrer == null || customer.getId() == null || referrer.getId() == null) {
            return false;
        }
        if (!customer.getId().equals(referrer.getId())) {
            return false;
        }

        String sourceReference = "USER:" + customer.getId();
        boolean exists = promotionFraudFlagRepository.existsByCustomerAndSourceTypeAndSourceReferenceAndStatus(
                customer,
                "REFERRAL",
                sourceReference,
                STATUS_OPEN
        );
        if (!exists) {
            PromotionFraudFlag flag = new PromotionFraudFlag();
            flag.setCustomer(customer);
            flag.setSourceType("REFERRAL");
            flag.setSourceReference(sourceReference);
            flag.setSeverity(SEVERITY_HIGH);
            flag.setStatus(STATUS_OPEN);
            flag.setReason("Self-referral attempt blocked.");
            promotionFraudFlagRepository.save(flag);
        }
        return true;
    }
}
