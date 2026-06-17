package com.ecommerce.app.order.repository;

import com.ecommerce.app.order.model.EmiInstallment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiInstallmentRepository extends JpaRepository<EmiInstallment, Long> {

    List<EmiInstallment> findByEmiPaymentPlan_IdOrderByInstallmentNumberAsc(Long emiPaymentPlanId);

    Optional<EmiInstallment> findByIdAndEmiPaymentPlan_Id(Long id, Long emiPaymentPlanId);

    Optional<EmiInstallment> findFirstByEmiPaymentPlan_IdAndPaidFalseOrderByInstallmentNumberAsc(Long emiPaymentPlanId);
}
