/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.vendor.services;

import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class VendorCodeGenerator {

    @Autowired
    private VendorprofileRepository vendorRepo;

    @Transactional
    public String generateNextVendorCode() {
        String year = String.valueOf(Year.now().getValue());
        String region = "BD"; // 👈 can be dynamic later (e.g., vendor.getRegionCode())
        String prefix = "VEN-" + region + "-" + year + "-";

        // Fetch the latest vendorCode like VEN-BD-2025-0042
        var latestCodes = vendorRepo.findLatestVendorCode(prefix, PageRequest.of(0, 1));

        int nextNumber = 1;

        if (!latestCodes.isEmpty()) {
            String latestCode = latestCodes.get(0); // e.g., VEN-BD-2025-0042
            String[] parts = latestCode.split("-");
            if (parts.length == 4) { // VEN, BD, 2025, 0042
                try {
                    nextNumber = Integer.parseInt(parts[3]) + 1;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return String.format("%s%04d", prefix, nextNumber);
    }

}
