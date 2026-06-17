package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.ProductVariantOption;
import com.ecommerce.app.product.ripository.ProductVariantOptionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductVariantOptionService {

    @Autowired
    private ProductVariantOptionRepository repository;

    public List<ProductVariantOption> findByVariantUuid(String variantUuid) {
        return repository.findByVariant_UuidOrderBySortOrderAscIdAsc(variantUuid);
    }

    public ProductVariantOption save(ProductVariantOption option) {
        return repository.save(option);
    }

    public ProductVariantOption findByUuid(String uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Catalog variant option not found for UUID: " + uuid));
    }

    @Transactional
    public void deleteByUuid(String uuid) {
        repository.delete(findByUuid(uuid));
    }
}
