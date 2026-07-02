package com.ecommerce.app.commission.controller;

import com.ecommerce.app.commission.model.MarketplaceCommissionSettings;
import com.ecommerce.app.commission.service.CommissionSettingsService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commission")
public class CommissionSettingsApiController {

    private final CommissionSettingsService commissionSettingsService;

    public CommissionSettingsApiController(CommissionSettingsService commissionSettingsService) {
        this.commissionSettingsService = commissionSettingsService;
    }

    @GetMapping("/applicable-rate")
    public Map<String, Object> applicableRate(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) Long categoryId) {
        BigDecimal rate = commissionSettingsService.getApplicableCommissionRate(productId, vendorId, categoryId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("commissionRate", rate);
        response.put("defaultRate", CommissionSettingsService.DEFAULT_COMMISSION_RATE);
        response.put("source", resolveSource(productId, vendorId, categoryId));
        return response;
    }

    private String resolveSource(Long productId, Long vendorId, Long categoryId) {
        return commissionSettingsService.resolveApplicable(productId, vendorId, categoryId)
                .map(MarketplaceCommissionSettings::getTargetSummary)
                .orElse("Global fallback");
    }
}
