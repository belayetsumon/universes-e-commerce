package com.ecommerce.app.product.services;

import com.ecommerce.app.module.settings.services.StoreOperationModeService;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.vendor.model.Vendorprofile;
import org.springframework.stereotype.Service;

@Service
public class ProductVendorAssignmentService {

    private final StoreOperationModeService storeOperationModeService;

    public ProductVendorAssignmentService(StoreOperationModeService storeOperationModeService) {
        this.storeOperationModeService = storeOperationModeService;
    }

    public void applyAdminProductVendorRule(Product product) {
        if (product == null) {
            return;
        }
        if (storeOperationModeService.isSingleVendorMode()) {
            product.setVendorprofile(storeOperationModeService.requirePrimaryVendor());
        }
    }

    public void validateProductVendor(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }
        Vendorprofile vendor = product.getVendorprofile();
        if (vendor == null || vendor.getId() == null) {
            throw new IllegalArgumentException("Product vendor is required.");
        }
    }
}
