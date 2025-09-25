/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.order.services;

import com.ecommerce.app.order.repository.SalesOrderRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class SalesOrderCodeGeneratorService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Transactional
    public String generateNextDailyOrderCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SO-" + today + "-";

        var latestCodes = salesOrderRepository.findLatestOrderCode(prefix + "%", PageRequest.of(0, 1));

        int nextNumber = 1;

        if (!latestCodes.isEmpty()) {
            String latestCode = latestCodes.get(0); // SO-20250709-00042
            String[] parts = latestCode.split("-");
            if (parts.length == 3) {
                try {
                    nextNumber = Integer.parseInt(parts[2]) + 1;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return String.format("%s%05d", prefix, nextNumber);
    }
}
