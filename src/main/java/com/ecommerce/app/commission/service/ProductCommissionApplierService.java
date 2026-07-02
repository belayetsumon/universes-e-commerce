package com.ecommerce.app.commission.service;

import com.ecommerce.app.product.model.Product;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class ProductCommissionApplierService {

    private final CommissionSettingsService commissionSettingsService;

    public ProductCommissionApplierService(CommissionSettingsService commissionSettingsService) {
        this.commissionSettingsService = commissionSettingsService;
    }

    public void prefillCommissionForForm(Product product) {
        applyResolvedCommission(product);
    }

    public void applyCommissionBeforeSave(Product product) {
        applyResolvedCommission(product);
    }

    private void applyResolvedCommission(Product product) {
        if (product == null) {
            return;
        }

        BigDecimal resolvedRate = commissionSettingsService.getApplicableCommissionRate(
                product.getId(),
                product.getVendorprofile() != null ? product.getVendorprofile().getId() : null,
                product.getProductcategory() != null ? product.getProductcategory().getId() : null
        );
        product.setMarketPlaceCommissionRate(normalizeRate(resolvedRate));
    }

    private BigDecimal normalizeRate(BigDecimal rate) {
        BigDecimal effectiveRate = rate != null ? rate : CommissionSettingsService.DEFAULT_COMMISSION_RATE;
        return effectiveRate.setScale(2, RoundingMode.HALF_UP);
    }
}
