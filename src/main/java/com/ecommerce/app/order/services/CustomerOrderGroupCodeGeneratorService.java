package com.ecommerce.app.order.services;

import com.ecommerce.app.order.repository.CustomerOrderGroupRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerOrderGroupCodeGeneratorService {

    private final CustomerOrderGroupRepository repository;

    public CustomerOrderGroupCodeGeneratorService(CustomerOrderGroupRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String generateNextDailyOrderGroupCode() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CO-" + today + "-";

        var latestCodes = repository.findLatestOrderGroupCode(prefix + "%", PageRequest.of(0, 1));
        int nextNumber = 1;

        if (!latestCodes.isEmpty()) {
            String latestCode = latestCodes.get(0);
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
