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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author libertyerp_local
 */
public interface CarrierRateRepository extends JpaRepository<CarrierRate, Long> {

    @Query("select distinct r from CarrierRate r join r.district d where r.carrier = :carrier and d = :district")
    List<CarrierRate> findByCarrierAndDistrict(@Param("carrier") Carrier carrier, @Param("district") District district);

    @Query("select distinct r from CarrierRate r join r.district d where r.carrier = :carrier and d in :districts")
    List<CarrierRate> findByCarrierAndDistrictIn(@Param("carrier") Carrier carrier, @Param("districts") List<District> districts);

    List<CarrierRate> findByCarrier(Carrier carrier);

    long countByCarrier(Carrier carrier);

    Optional<CarrierRate> findByUuid(String uuid);

    List<CarrierRate> findByCarrierId(Long carrierId);
}
