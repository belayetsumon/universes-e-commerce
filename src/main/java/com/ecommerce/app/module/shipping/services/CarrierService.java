/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.shipping.services;

import com.ecommerce.app.module.shipping.model.Carrier;
import com.ecommerce.app.module.shipping.model.Shipment;
import com.ecommerce.app.module.shipping.repository.CarrierRateRepository;
import com.ecommerce.app.module.shipping.repository.CarrierRepository;
import com.ecommerce.app.module.shipping.repository.ShipmentRepository;
import com.ecommerce.app.module.shipping.repository.ShippingProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class CarrierService {

    private final CarrierRepository repo;
    private final ObjectMapper objectMapper;

    @Autowired
    CarrierRateRepository carrierRateRepository;

    @Autowired
    ShipmentRepository shipmentRepository;
    @Autowired
    ShippingProfileRepository shippingProfileRepository;

    public CarrierService(CarrierRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    public List<Carrier> getAll() {
        return repo.findAll();

    }

    public List<Carrier> findAllById(List<Long> ids) {
        return repo.findAllById(ids);
    }

    public Carrier getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Carrier save(Carrier carrier) {
        return repo.save(carrier);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public void deleteByUuid(String uuid) {
        Carrier carrier = repo.findByUuid(uuid).orElseThrow();
        repo.delete(carrier); // This will throw DataIntegrityViolationException automatically if references exist
    }

    public long countDependencies(String uuid) {
        Carrier carrier = repo.findByUuid(uuid).orElseThrow();
        long carrierRate = carrierRateRepository.countByCarrier(carrier);
        long shipment = shipmentRepository.countByCarrier(carrier);
        long profiles = shippingProfileRepository.countByAllowedCarriers(carrier);
        return carrierRate + shipment + profiles; // or any related table
    }

    public Map<String, Long> getDependencies(String uuid) {
        Carrier carrier = repo.findByUuid(uuid).orElseThrow();

        long carrierRate = carrierRateRepository.countByCarrier(carrier);
        long shipment = shipmentRepository.countByCarrier(carrier);
        long profiles = shippingProfileRepository.countByAllowedCarriers(carrier);

        return Map.of(
                "carrierRate", carrierRate,
                "shipment", shipment,
                "profiles", profiles
        );
    }

    public String generateShipmentLabel(Long carrierId, Shipment shipment) throws Exception {
        Carrier carrier = repo.findById(carrierId).orElseThrow();

        // Parse configJson
        Map<String, String> config = objectMapper.readValue(carrier.getConfigJson(), Map.class);
        String apiKey = config.get("apiKey");
        String endpoint = config.get("endpoint");
        String accountNumber = config.get("accountNumber");

        // Prepare request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("trackingNumber", shipment.getTrackingNumber());
        payload.put("recipient", Map.of(
                "name", "Customer Name",
                "address", "Customer Address"
        ));
        payload.put("accountNumber", accountNumber);

        // Simulate API call (actual HTTP call using RestTemplate / WebClient)
        System.out.println("Calling " + endpoint + " with API Key: " + apiKey);
        System.out.println("Payload: " + payload);

        // Simulate label URL returned by carrier
        String labelUrl = "https://carrier.com/labels/" + shipment.getTrackingNumber() + ".pdf";
        return labelUrl;
    }

}
