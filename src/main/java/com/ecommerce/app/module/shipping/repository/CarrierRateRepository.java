/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.ecommerce.app.module.shipping.repository;

import com.ecommerce.app.globalServices.District;
import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.CarrierRate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author libertyerp_local
 */
public interface CarrierRateRepository extends JpaRepository<CarrierRate, Long> {

    List<CarrierRate> findByCarrierAndDistrict(Carrier carrier, District district);

    List<CarrierRate> findByCarrierAndDistrictIn(Carrier carrier, List<District> districts);

    List<CarrierRate> findByCarrier(Carrier carrier);

    long countByCarrier(Carrier carrier);

    Optional<CarrierRate> findByUuid(String uuid);
}
