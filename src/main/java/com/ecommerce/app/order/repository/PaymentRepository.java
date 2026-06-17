package com.ecommerce.app.order.repository;

import com.ecommerce.app.order.model.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrder_IdOrderByIdDesc(Long orderId);
}
