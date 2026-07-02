package com.ecommerce.app.product.services;

import com.ecommerce.app.product.dto.ProductStockReportRow;
import com.ecommerce.app.product.dto.ProductStockReportSummary;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import com.ecommerce.app.vendor.model.Vendorprofile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockInventoryReportService {

    private static final String FILTER_ALL = "all";
    private static final String FILTER_IN_STOCK = "in_stock";
    private static final String FILTER_LOW_STOCK = "low_stock";
    private static final String FILTER_OUT_OF_STOCK = "out_of_stock";
    private static final String FILTER_RESERVED = "reserved";
    private static final String FILTER_SOLD = "sold";
    private static final String FILTER_PRODUCT = "product";
    private static final String FILTER_VARIANT = "variant";

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public StockInventoryReportService(ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductStockReportRow> findCurrentStock(Long vendorId, Long categoryId, String q,
            String stockFilter, BigDecimal lowStockThreshold) {
        BigDecimal threshold = normalizeThreshold(lowStockThreshold);
        String normalizedFilter = normalizeFilter(stockFilter);
        String search = normalizeSearch(q);

        List<Product> products = loadProducts(vendorId).stream()
                .filter(product -> matchesCategory(product, categoryId))
                .collect(Collectors.toList());

        Map<Long, List<ProductVariant>> variantsByProductId = productVariantRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .filter(variant -> variant.getProduct() != null && variant.getProduct().getId() != null)
                .filter(variant -> vendorId == null || matchesVendor(variant.getProduct(), vendorId))
                .filter(variant -> categoryId == null || matchesCategory(variant.getProduct(), categoryId))
                .collect(Collectors.groupingBy(variant -> variant.getProduct().getId()));

        List<ProductStockReportRow> rows = new ArrayList<>();
        for (Product product : products) {
            List<ProductVariant> variants = variantsByProductId.getOrDefault(product.getId(), List.of());
            if (Boolean.TRUE.equals(product.getManageProductVariants()) && !variants.isEmpty()) {
                variants.stream()
                        .map(variant -> toVariantRow(product, variant, threshold))
                        .forEach(rows::add);
            } else {
                rows.add(toProductRow(product, threshold));
            }
        }

        return rows.stream()
                .filter(row -> matchesSearch(row, search))
                .filter(row -> matchesStockFilter(row, normalizedFilter, threshold))
                .sorted(Comparator.comparing(ProductStockReportRow::getProductTitle, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(ProductStockReportRow::getRowType, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(ProductStockReportRow::getVariantSku, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    public ProductStockReportSummary summarize(List<ProductStockReportRow> rows) {
        ProductStockReportSummary summary = new ProductStockReportSummary();
        List<ProductStockReportRow> safeRows = rows == null ? List.of() : rows;
        summary.setRowCount(safeRows.size());
        summary.setAvailableQuantity(sum(safeRows, "available"));
        summary.setReservedQuantity(sum(safeRows, "reserved"));
        summary.setSoldQuantity(sum(safeRows, "sold"));
        summary.setTotalOnHand(sum(safeRows, "total"));
        summary.setOutOfStockCount(safeRows.stream().filter(row -> "Out of stock".equals(row.getStockStatus())).count());
        summary.setLowStockCount(safeRows.stream().filter(row -> "Low stock".equals(row.getStockStatus())).count());
        return summary;
    }

    private List<Product> loadProducts(Long vendorId) {
        if (vendorId != null) {
            return productRepository.findByVendorprofile_IdOrderByIdDesc(vendorId);
        }
        return productRepository.findAll();
    }

    private ProductStockReportRow toProductRow(Product product, BigDecimal lowStockThreshold) {
        ProductStockReportRow row = baseRow(product);
        row.setRowType("Product");
        row.setAvailableQuantity(safe(product.getStockAvailableQuantity()));
        row.setReservedQuantity(safe(product.getStockReservedQuantity()));
        row.setSoldQuantity(safe(product.getStockSoldQuantity()));
        row.setTotalOnHand(row.getAvailableQuantity().add(row.getReservedQuantity()));
        row.setStockStatus(resolveStockStatus(row.getAvailableQuantity(), lowStockThreshold));
        return row;
    }

    private ProductStockReportRow toVariantRow(Product product, ProductVariant variant, BigDecimal lowStockThreshold) {
        ProductStockReportRow row = baseRow(product);
        row.setRowType("Variant");
        row.setVariantUuid(variant.getUuid());
        row.setVariantSku(variant.getSku());
        row.setVariantStatus(variant.getStatus() == null ? null : variant.getStatus().name());
        row.setAvailableQuantity(safe(variant.getStockQuantity()));
        row.setReservedQuantity(safe(variant.getReservedQuantity()));
        row.setSoldQuantity(safe(variant.getSoldQuantity()));
        row.setTotalOnHand(row.getAvailableQuantity().add(row.getReservedQuantity()));
        row.setStockStatus(resolveStockStatus(row.getAvailableQuantity(), lowStockThreshold));
        return row;
    }

    private ProductStockReportRow baseRow(Product product) {
        ProductStockReportRow row = new ProductStockReportRow();
        row.setProductId(product.getId());
        row.setProductTitle(product.getTitle());
        row.setProductSku(String.valueOf(product.getSku()));
        row.setProductStatus(product.getStatus() == null ? null : product.getStatus().name());
        row.setManageStock(product.getManageStock());
        row.setManageProductVariants(product.getManageProductVariants());
        if (product.getProductcategory() != null) {
            row.setCategoryId(product.getProductcategory().getId());
            row.setCategoryName(product.getProductcategory().getName());
        }
        Vendorprofile vendor = product.getVendorprofile();
        if (vendor != null) {
            row.setVendorId(vendor.getId());
            row.setVendorName(vendor.getCompanyName());
            row.setVendorCode(vendor.getVendorCode());
        }
        return row;
    }

    private boolean matchesCategory(Product product, Long categoryId) {
        if (categoryId == null) {
            return true;
        }
        return product.getProductcategory() != null && categoryId.equals(product.getProductcategory().getId());
    }

    private boolean matchesVendor(Product product, Long vendorId) {
        return product.getVendorprofile() != null
                && product.getVendorprofile().getId() != null
                && product.getVendorprofile().getId().equals(vendorId);
    }

    private boolean matchesSearch(ProductStockReportRow row, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        return contains(row.getProductTitle(), search)
                || contains(row.getProductSku(), search)
                || contains(row.getVariantSku(), search)
                || contains(row.getCategoryName(), search)
                || contains(row.getVendorName(), search)
                || contains(row.getVendorCode(), search);
    }

    private boolean matchesStockFilter(ProductStockReportRow row, String filter, BigDecimal lowStockThreshold) {
        BigDecimal available = safe(row.getAvailableQuantity());
        BigDecimal reserved = safe(row.getReservedQuantity());
        BigDecimal sold = safe(row.getSoldQuantity());
        return switch (filter) {
            case FILTER_IN_STOCK -> available.compareTo(BigDecimal.ZERO) > 0;
            case FILTER_LOW_STOCK -> available.compareTo(BigDecimal.ZERO) > 0 && available.compareTo(lowStockThreshold) <= 0;
            case FILTER_OUT_OF_STOCK -> available.compareTo(BigDecimal.ZERO) <= 0;
            case FILTER_RESERVED -> reserved.compareTo(BigDecimal.ZERO) > 0;
            case FILTER_SOLD -> sold.compareTo(BigDecimal.ZERO) > 0;
            case FILTER_PRODUCT -> "Product".equals(row.getRowType());
            case FILTER_VARIANT -> "Variant".equals(row.getRowType());
            default -> true;
        };
    }

    private String resolveStockStatus(BigDecimal available, BigDecimal lowStockThreshold) {
        BigDecimal safeAvailable = safe(available);
        if (safeAvailable.compareTo(BigDecimal.ZERO) <= 0) {
            return "Out of stock";
        }
        if (safeAvailable.compareTo(lowStockThreshold) <= 0) {
            return "Low stock";
        }
        return "In stock";
    }

    private BigDecimal sum(List<ProductStockReportRow> rows, String field) {
        return rows.stream()
                .map(row -> switch (field) {
            case "reserved" -> safe(row.getReservedQuantity());
            case "sold" -> safe(row.getSoldQuantity());
            case "total" -> safe(row.getTotalOnHand());
            default -> safe(row.getAvailableQuantity());
        })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal normalizeThreshold(BigDecimal lowStockThreshold) {
        if (lowStockThreshold == null || lowStockThreshold.compareTo(BigDecimal.ZERO) < 0) {
            return new BigDecimal("5");
        }
        return lowStockThreshold;
    }

    private String normalizeFilter(String stockFilter) {
        if (stockFilter == null || stockFilter.isBlank()) {
            return FILTER_ALL;
        }
        return stockFilter.trim().toLowerCase(Locale.ENGLISH);
    }

    private String normalizeSearch(String q) {
        return q == null ? null : q.trim().toLowerCase(Locale.ENGLISH);
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ENGLISH).contains(search);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
