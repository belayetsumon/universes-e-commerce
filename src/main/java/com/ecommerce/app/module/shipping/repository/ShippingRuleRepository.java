package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.ShippingRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingRuleRepository extends JpaRepository<ShippingRule, Long> {

    List<ShippingRule> findByActiveTrueOrderByPriorityAscIdAsc();
}
