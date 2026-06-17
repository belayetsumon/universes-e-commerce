package com.ecommerce.app.order.repository;

import com.ecommerce.app.order.model.EmiPaymentPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiPaymentPlanRepository extends JpaRepository<EmiPaymentPlan, Long> {

    Optional<EmiPaymentPlan> findBySalesOrder_Id(Long salesOrderId);

    Optional<EmiPaymentPlan> findByIdAndCustomer_Id(Long id, Long customerId);

    List<EmiPaymentPlan> findByCustomer_IdOrderByIdDesc(Long customerId);
}
