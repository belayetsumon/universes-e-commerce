package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.module.shipping.model.CarrierRate;
import com.ecommerce.app.module.shipping.model.CarrierRateSlab;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrierRateSlabRepository extends JpaRepository<CarrierRateSlab, Long> {

    List<CarrierRateSlab> findByCarrierRateAndActiveTrueOrderByPriorityAscMinWeightAsc(CarrierRate carrierRate);

    List<CarrierRateSlab> findByCarrierRateOrderByPriorityAscMinWeightAsc(CarrierRate carrierRate);

    long countByCarrierRate(CarrierRate carrierRate);
}
