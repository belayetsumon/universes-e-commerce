package com.ecommerce.app.module.order.repository;

import com.ecommerce.app.module.order.model.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrder_IdOrderByIdDesc(Long orderId);
}
