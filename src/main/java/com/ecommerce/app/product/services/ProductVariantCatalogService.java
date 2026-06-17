package com.ecommerce.app.product.services;

import com.ecommerce.app.product.dto.CatalogAttributeOptionView;
import com.ecommerce.app.product.dto.CatalogVariantOptionView;
import com.ecommerce.app.product.dto.CatalogVariantSelectionView;
import com.ecommerce.app.product.dto.CatalogVariantSummaryView;
import com.ecommerce.app.product.model.AttributeOption;
import com.ecommerce.app.product.model.CategoryAttribute;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductDimension;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.ProductVariantOption;
import com.ecommerce.app.product.ripository.AttributeOptionRepository;
import com.ecommerce.app.product.ripository.CategoryAttributeRepository;
import com.ecommerce.app.product.ripository.ProductDimensionRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 2026-05-15: Live generic-variant orchestration layer for admin forms,
 * storefront selectors, and inventory.
 */
@Service
public class ProductVariantCatalogService {

    @Autowired
    private ProductVariantRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductDimensionRepository productDimensionRepository;

    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;

    @Autowired
    private AttributeOptionRepository attributeOptionRepository;

    @Transactional(readOnly = true)
    public List<ProductVariant> findByProductUuid(String productUuid) {
        return repository.findDisplayRowsByProductUuid(productUuid);
    }

    @Transactional(readOnly = true)
    public List<ProductVariant> findByProductIds(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return repository.findDisplayRowsByProductIds(new ArrayList<>(productIds));
    }

    @Transactional(readOnly = true)
    public boolean hasCatalogVariants(Long productId) {
        return productId != null && repository.existsByProduct_Id(productId);
    }

    @Transactional(readOnly = true)
    public ProductVariant findByUuid(String uuid) {
        return repository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Catalog variant not found for UUID: " + uuid));
    }

    @Transactional(readOnly = true)
    public List<CatalogVariantSummaryView> buildVariantSummaries(String productUuid) {
        return repository.findDisplayRowsByProductUuid(productUuid).stream()
                .map(this::toSummaryView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogVariantSelectionView> buildVariantSelectionViews(String productUuid, String variantUuid) {
        Product product = loadProduct(productUuid);
        List<CategoryAttribute> mappings = loadVariantMappings(product);
        Map<String, String> selectedOptionByAttributeUuid = variantUuid == null || variantUuid.isBlank()
                ? Map.of()
                : findByUuid(variantUuid).getOptions().stream()
                        .filter(optionRow -> optionRow.getAttribute() != null && optionRow.getAttributeOption() != null)
                        .collect(Collectors.toMap(
                                optionRow -> optionRow.getAttribute().getUuid(),
                                optionRow -> optionRow.getAttributeOption().getUuid(),
                                (existing, replacement) -> existing,
                                LinkedHashMap::new
                        ));

        List<CatalogVariantSelectionView> views = new ArrayList<>();
        for (CategoryAttribute mapping : mappings) {
            if (mapping.getAttribute() == null) {
                continue;
            }

            CatalogVariantSelectionView view = new CatalogVariantSelectionView();
            view.setAttributeUuid(mapping.getAttribute().getUuid());
            view.setAttributeCode(mapping.getAttribute().getCode());
            view.setAttributeName(mapping.getAttribute().getName());
            view.setRequired(Boolean.TRUE.equals(mapping.getRequired()));
            view.setSelectedOptionUuid(selectedOptionByAttributeUuid.get(mapping.getAttribute().getUuid()));

            List<CatalogAttributeOptionView> optionViews = uniqueActiveOptions(mapping).stream()
                    .sorted(Comparator.comparing((AttributeOption optionRow) -> defaultInteger(optionRow.getSortOrder()))
                            .thenComparing(optionRow -> defaultText(optionRow.getLabel()), String.CASE_INSENSITIVE_ORDER))
                    .map(optionRow -> new CatalogAttributeOptionView(optionRow.getUuid(), optionRow.getLabel()))
                    .toList();
            view.setOptions(optionViews);

            views.add(view);
        }
        return views;
    }

    @Transactional(readOnly = true)
    public String buildVariantSummaryLabel(ProductVariant catalogVariant) {
        if (catalogVariant == null) {
            return "";
        }
        return buildVariantSummaryLabel(
                catalogVariant.getOptions() == null ? List.of() : catalogVariant.getOptions().stream()
                .map(this::toVariantOptionView)
                .sorted(Comparator.comparing(optionView -> defaultInteger(optionView.getSortOrder())))
                .toList()
        );
    }

    @Transactional
    public ProductVariant saveVariant(String productUuid,
            String variantUuid,
            String sku,
            String barcode,
            BigDecimal sellingPrice,
            BigDecimal specialPrice,
            BigDecimal stockQuantity,
            ProductStatusEnum status,
            Boolean active,
            Map<String, String[]> parameterMap) {

        Product product = loadProduct(productUuid);
        ProductVariant variant = variantUuid == null || variantUuid.isBlank()
                ? new ProductVariant()
                : findByUuid(variantUuid);

        if (variant.getProduct() != null
                && variant.getProduct().getUuid() != null
                && !variant.getProduct().getUuid().equals(productUuid)) {
            throw new IllegalArgumentException("Catalog variant does not belong to the requested product.");
        }

        List<CategoryAttribute> mappings = loadVariantMappings(product);
        Map<String, AttributeOption> selectedOptions = resolveSelectedOptions(mappings, parameterMap);

        if (!mappings.isEmpty() && selectedOptions.isEmpty()) {
            throw new IllegalArgumentException("Please choose at least one variant option.");
        }

        String normalizedSku = cleanText(sku);
        if (normalizedSku == null) {
            normalizedSku = buildGeneratedSku(product, selectedOptions.values(), variantUuid);
        }

        repository.findBySkuIgnoreCase(normalizedSku)
                .filter(existing -> !Objects.equals(existing.getUuid(), variantUuid))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Variant SKU already exists.");
                });

        String combinationKey = buildCombinationKey(selectedOptions.values());
        if (!combinationKey.isBlank()) {
            repository.findDisplayRowsByProductUuid(productUuid).stream()
                    .filter(existing -> !Objects.equals(existing.getUuid(), variantUuid))
                    .filter(existing -> combinationKey.equals(buildCombinationKeyFromVariant(existing)))
                    .findFirst()
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("This variant combination already exists.");
                    });
        }

        variant.setProduct(product);
        variant.setSku(normalizedSku);
        variant.setBarcode(cleanText(barcode));
        variant.setSellingPrice(defaultMoney(sellingPrice, product.getSalesPrice()));
        variant.setSpecialPrice(specialPrice);
        variant.setWeight(resolveVariantWeight(variant, product));
        variant.setStockQuantity(defaultQuantity(stockQuantity, variant.getStockQuantity()));
        variant.setReservedQuantity(defaultQuantity(variant.getReservedQuantity(), BigDecimal.ZERO));
        variant.setSoldQuantity(defaultQuantity(variant.getSoldQuantity(), BigDecimal.ZERO));
        variant.setStatus(status == null ? ProductStatusEnum.Active : status);
        variant.setActive(active == null ? Boolean.TRUE : active);

        syncVariantOptions(variant, mappings, selectedOptions);
        try {
            return repository.save(variant);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(resolveVariantSaveError(ex), ex);
        }
    }

    @Transactional
    public int autoGenerateVariants(String productUuid, Map<String, String[]> parameterMap) {
        Product product = loadProduct(productUuid);
        List<CategoryAttribute> mappings = loadVariantMappings(product);
        if (mappings.isEmpty()) {
            throw new IllegalArgumentException("No variant-capable category attributes are mapped to this product.");
        }

        List<List<AttributeOption>> optionMatrix = new ArrayList<>();
        for (CategoryAttribute mapping : mappings) {
            List<AttributeOption> selectedOptions = resolveGenerationOptions(mapping, parameterMap);
            if (selectedOptions.isEmpty()) {
                throw new IllegalArgumentException("Please choose at least one option for " + mapping.getAttribute().getName() + ".");
            }
            optionMatrix.add(selectedOptions);
        }

        Set<String> existingKeys = repository.findDisplayRowsByProductUuid(productUuid).stream()
                .map(this::buildCombinationKeyFromVariant)
                .filter(key -> !key.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<List<AttributeOption>> combinations = buildCartesianCombinations(optionMatrix);
        int createdCount = 0;
        for (List<AttributeOption> combination : combinations) {
            String combinationKey = buildCombinationKey(combination);
            if (existingKeys.contains(combinationKey)) {
                continue;
            }

            Map<String, String[]> generatedParams = new LinkedHashMap<>();
            for (AttributeOption optionRow : combination) {
                if (optionRow.getAttribute() == null) {
                    continue;
                }
                generatedParams.put(
                        buildVariantFieldName(optionRow.getAttribute().getUuid()),
                        new String[]{optionRow.getUuid()}
                );
            }

            saveVariant(
                    productUuid,
                    null,
                    buildGeneratedSku(product, combination, null),
                    null,
                    product.getSalesPrice(),
                    null,
                    BigDecimal.ZERO,
                    ProductStatusEnum.Active,
                    Boolean.TRUE,
                    generatedParams
            );
            existingKeys.add(combinationKey);
            createdCount++;
        }
        return createdCount;
    }

    @Transactional
    public void deleteByUuid(String uuid) {
        repository.delete(findByUuid(uuid));
    }

    private Product loadProduct(String productUuid) {
        return productRepository.findByUuid(productUuid)
                .orElseThrow(() -> new IllegalArgumentException("Product not found for UUID: " + productUuid));
    }

    private List<CategoryAttribute> loadVariantMappings(Product product) {
        if (product == null || product.getProductcategory() == null || product.getProductcategory().getUuid() == null) {
            return List.of();
        }
        Map<String, CategoryAttribute> mappingsByAttributeUuid = new LinkedHashMap<>();
        categoryAttributeRepository.findActiveMappingsWithAttributeOptions(product.getProductcategory().getUuid()).stream()
                .filter(mapping -> Boolean.TRUE.equals(mapping.getActive()))
                .filter(mapping -> Boolean.TRUE.equals(mapping.getVariantAttribute()))
                .filter(mapping -> mapping.getAttribute() != null)
                .sorted(Comparator.comparing(mapping -> defaultInteger(mapping.getDisplayOrder())))
                .forEach(mapping -> {
                    String attributeUuid = mapping.getAttribute().getUuid();
                    if (attributeUuid != null) {
                        mappingsByAttributeUuid.putIfAbsent(attributeUuid, mapping);
                    }
                });
        return new ArrayList<>(mappingsByAttributeUuid.values());
    }

    private void syncVariantOptions(ProductVariant variant,
            List<CategoryAttribute> mappings,
            Map<String, AttributeOption> selectedOptions) {
        if (variant.getOptions() == null) {
            variant.setOptions(new ArrayList<>());
        }

        Map<Long, ProductVariantOption> existingByAttributeId = new LinkedHashMap<>();
        List<ProductVariantOption> duplicateRows = new ArrayList<>();
        for (ProductVariantOption optionRow : variant.getOptions()) {
            Long attributeId = optionRow != null && optionRow.getAttribute() != null
                    ? optionRow.getAttribute().getId()
                    : null;
            if (attributeId == null) {
                duplicateRows.add(optionRow);
                continue;
            }
            if (existingByAttributeId.putIfAbsent(attributeId, optionRow) != null) {
                duplicateRows.add(optionRow);
            }
        }
        if (!duplicateRows.isEmpty()) {
            variant.getOptions().removeAll(duplicateRows);
        }

        Set<Long> selectedAttributeIds = mappings.stream()
                .filter(mapping -> mapping.getAttribute() != null)
                .filter(mapping -> selectedOptions.containsKey(mapping.getAttribute().getUuid()))
                .map(mapping -> mapping.getAttribute().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        variant.getOptions().removeIf(optionRow -> optionRow == null
                || optionRow.getAttribute() == null
                || optionRow.getAttribute().getId() == null
                || !selectedAttributeIds.contains(optionRow.getAttribute().getId()));

        Map<Long, ProductVariantOption> activeByAttributeId = variant.getOptions().stream()
                .filter(optionRow -> optionRow.getAttribute() != null && optionRow.getAttribute().getId() != null)
                .collect(Collectors.toMap(
                        optionRow -> optionRow.getAttribute().getId(),
                        optionRow -> optionRow,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        for (CategoryAttribute mapping : mappings) {
            if (mapping.getAttribute() == null || mapping.getAttribute().getId() == null) {
                continue;
            }

            AttributeOption selectedOption = selectedOptions.get(mapping.getAttribute().getUuid());
            if (selectedOption == null) {
                continue;
            }

            ProductVariantOption optionRow = activeByAttributeId.get(mapping.getAttribute().getId());
            if (optionRow == null) {
                optionRow = new ProductVariantOption();
                optionRow.setVariant(variant);
                variant.getOptions().add(optionRow);
                activeByAttributeId.put(mapping.getAttribute().getId(), optionRow);
            }

            optionRow.setAttribute(mapping.getAttribute());
            optionRow.setAttributeOption(selectedOption);
            optionRow.setTextValue(selectedOption.getValue());
            optionRow.setDisplayValue(selectedOption.getLabel());
            optionRow.setSortOrder(defaultInteger(mapping.getDisplayOrder()));
        }
    }

    private Map<String, AttributeOption> resolveSelectedOptions(List<CategoryAttribute> mappings, Map<String, String[]> parameterMap) {
        Map<String, AttributeOption> selectedOptions = new LinkedHashMap<>();
        for (CategoryAttribute mapping : mappings) {
            if (mapping.getAttribute() == null) {
                continue;
            }

            String[] values = parameterMap.get(buildVariantFieldName(mapping.getAttribute().getUuid()));
            String optionUuid = values != null && values.length > 0 ? cleanText(values[0]) : null;
            if (optionUuid == null) {
                if (Boolean.TRUE.equals(mapping.getRequired())) {
                    throw new IllegalArgumentException("Please select " + mapping.getAttribute().getName() + ".");
                }
                continue;
            }

            AttributeOption optionRow = attributeOptionRepository.findByUuid(optionUuid)
                    .filter(existing -> existing.getAttribute() != null
                    && Objects.equals(existing.getAttribute().getId(), mapping.getAttribute().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid option selected for " + mapping.getAttribute().getName() + "."));

            selectedOptions.put(mapping.getAttribute().getUuid(), optionRow);
        }
        return selectedOptions;
    }

    private List<AttributeOption> resolveGenerationOptions(CategoryAttribute mapping, Map<String, String[]> parameterMap) {
        if (mapping.getAttribute() == null) {
            return List.of();
        }

        String[] requestedValues = parameterMap.get(buildGenerateFieldName(mapping.getAttribute().getUuid()));
        List<String> requestedOptionUuids = requestedValues == null
                ? List.of()
                : java.util.Arrays.stream(requestedValues)
                        .map(this::cleanText)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        List<AttributeOption> availableOptions = uniqueActiveOptions(mapping).stream()
                .sorted(Comparator.comparing((AttributeOption optionRow) -> defaultInteger(optionRow.getSortOrder()))
                        .thenComparing(optionRow -> defaultText(optionRow.getLabel()), String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (requestedOptionUuids.isEmpty()) {
            return availableOptions;
        }

        return availableOptions.stream()
                .filter(optionRow -> requestedOptionUuids.contains(optionRow.getUuid()))
                .toList();
    }

    private List<List<AttributeOption>> buildCartesianCombinations(List<List<AttributeOption>> optionMatrix) {
        List<List<AttributeOption>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<>());

        for (List<AttributeOption> options : optionMatrix) {
            List<List<AttributeOption>> next = new ArrayList<>();
            for (List<AttributeOption> existing : combinations) {
                for (AttributeOption optionRow : options) {
                    List<AttributeOption> combined = new ArrayList<>(existing);
                    combined.add(optionRow);
                    next.add(combined);
                }
            }
            combinations = next;
        }
        return combinations;
    }

    private CatalogVariantSummaryView toSummaryView(ProductVariant variant) {
        CatalogVariantSummaryView view = new CatalogVariantSummaryView();
        view.setUuid(variant.getUuid());
        view.setSku(variant.getSku());
        view.setBarcode(variant.getBarcode());
        view.setSellingPrice(variant.getSellingPrice());
        view.setSpecialPrice(variant.getSpecialPrice());
        view.setStockQuantity(variant.getStockQuantity());
        view.setReservedQuantity(variant.getReservedQuantity());
        view.setSoldQuantity(variant.getSoldQuantity());
        view.setActive(variant.getActive());
        view.setStatus(variant.getStatus());

        List<CatalogVariantOptionView> optionViews = variant.getOptions() == null
                ? List.of()
                : variant.getOptions().stream()
                        .map(this::toVariantOptionView)
                        .sorted(Comparator.comparing(optionView -> defaultInteger(optionView.getSortOrder())))
                        .toList();
        view.setOptions(optionViews);
        view.setOptionSummary(buildVariantSummaryLabel(optionViews));
        return view;
    }

    private CatalogVariantOptionView toVariantOptionView(ProductVariantOption optionRow) {
        CatalogVariantOptionView optionView = new CatalogVariantOptionView();
        if (optionRow.getAttribute() != null) {
            optionView.setAttributeUuid(optionRow.getAttribute().getUuid());
            optionView.setAttributeCode(optionRow.getAttribute().getCode());
            optionView.setAttributeName(optionRow.getAttribute().getName());
        }
        if (optionRow.getAttributeOption() != null) {
            optionView.setOptionUuid(optionRow.getAttributeOption().getUuid());
            optionView.setLabel(optionRow.getAttributeOption().getLabel());
            optionView.setValue(cleanText(optionRow.getAttributeOption().getValue()));
        }
        optionView.setLabel(optionView.getLabel() == null ? cleanText(optionRow.getDisplayValue()) : optionView.getLabel());
        optionView.setValue(optionView.getValue() == null ? cleanText(optionRow.getTextValue()) : optionView.getValue());
        optionView.setSortOrder(optionRow.getSortOrder());
        return optionView;
    }

    private String buildVariantSummaryLabel(List<CatalogVariantOptionView> optionViews) {
        if (optionViews == null || optionViews.isEmpty()) {
            return "Product Stock";
        }

        return optionViews.stream()
                .map(optionView -> {
                    String attributeName = cleanText(optionView.getAttributeName());
                    String label = cleanText(optionView.getLabel());
                    if (attributeName == null && label == null) {
                        return null;
                    }
                    if (attributeName == null) {
                        return label;
                    }
                    if (label == null) {
                        return attributeName;
                    }
                    return attributeName + ": " + label;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" | "));
    }

    private String buildCombinationKeyFromVariant(ProductVariant variant) {
        if (variant == null || variant.getOptions() == null) {
            return "";
        }
        return buildCombinationKey(
                variant.getOptions().stream()
                        .map(ProductVariantOption::getAttributeOption)
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private String buildCombinationKey(Collection<AttributeOption> selectedOptions) {
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return "";
        }
        return selectedOptions.stream()
                .filter(Objects::nonNull)
                .map(AttributeOption::getUuid)
                .filter(Objects::nonNull)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining("|"));
    }

    private String buildGeneratedSku(Product product, Collection<AttributeOption> selectedOptions, String seed) {
        String baseSku = product == null || product.getSku() <= 0 ? "VARIANT" : String.valueOf(product.getSku());
        String suffix = selectedOptions == null || selectedOptions.isEmpty()
                ? null
                : selectedOptions.stream()
                        .filter(Objects::nonNull)
                        .map(optionRow -> cleanText(optionRow.getCode()) != null
                        ? optionRow.getCode()
                        : normalizeToken(optionRow.getLabel()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("-"));

        if (suffix == null && seed != null && !seed.isBlank()) {
            suffix = seed.substring(0, Math.min(8, seed.length()));
        }

        String candidate = suffix == null ? baseSku + "-variant" : baseSku + "-" + suffix;
        if (repository.findBySkuIgnoreCase(candidate)
                .filter(existing -> !Objects.equals(existing.getUuid(), seed))
                .isEmpty()) {
            return candidate;
        }

        int serial = 1;
        String serialCandidate = candidate;
        while (repository.findBySkuIgnoreCase(serialCandidate)
                .filter(existing -> !Objects.equals(existing.getUuid(), seed))
                .isPresent()) {
            serial++;
            serialCandidate = candidate + "-" + serial;
        }
        return serialCandidate;
    }

    private BigDecimal resolveVariantWeight(ProductVariant variant, Product product) {
        if (variant != null && variant.getWeight() != null && variant.getWeight().compareTo(BigDecimal.ZERO) > 0) {
            return variant.getWeight();
        }
        if (product == null || product.getId() == null) {
            return BigDecimal.ZERO;
        }
        ProductDimension dimension = productDimensionRepository.findByProduct_Id(product.getId());
        return dimension != null && dimension.getWeight() != null ? dimension.getWeight() : BigDecimal.ZERO;
    }

    private BigDecimal defaultMoney(BigDecimal value, BigDecimal fallback) {
        if (value != null) {
            return value;
        }
        return fallback != null ? fallback : BigDecimal.ZERO;
    }

    private BigDecimal defaultQuantity(BigDecimal value, BigDecimal fallback) {
        if (value != null) {
            return value;
        }
        return fallback != null ? fallback : BigDecimal.ZERO;
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private String normalizeToken(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null) {
            return null;
        }
        return cleaned.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    public String buildVariantFieldName(String attributeUuid) {
        return "variantAttribute_" + attributeUuid;
    }

    public String buildGenerateFieldName(String attributeUuid) {
        return "generateAttribute_" + attributeUuid;
    }

    private String resolveVariantSaveError(DataIntegrityViolationException ex) {
        String message = ex == null
                ? null
                : ex.getMostSpecificCause() != null
                        ? ex.getMostSpecificCause().getMessage()
                        : ex.getMessage();
        if (message != null && message.contains("uk_catalog_product_variant_option_variant_attribute")) {
            return "Each catalog variant can keep only one option for each attribute. Please remove duplicate attribute selections and try again.";
        }
        if (message != null && message.contains("uk_catalog_product_variant_sku")) {
            return "Variant SKU already exists.";
        }
        return "The catalog variant could not be saved. Please review the selected options and try again.";
    }

    private List<AttributeOption> uniqueActiveOptions(CategoryAttribute mapping) {
        if (mapping == null || mapping.getAttribute() == null || mapping.getAttribute().getOptions() == null) {
            return List.of();
        }

        Map<String, AttributeOption> optionsByUuid = new LinkedHashMap<>();
        for (AttributeOption optionRow : mapping.getAttribute().getOptions()) {
            if (optionRow == null || optionRow.getUuid() == null || !Boolean.TRUE.equals(optionRow.getActive())) {
                continue;
            }
            optionsByUuid.putIfAbsent(optionRow.getUuid(), optionRow);
        }
        return new ArrayList<>(optionsByUuid.values());
    }
}
