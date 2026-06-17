package com.ecommerce.app.product.services;

import com.ecommerce.app.product.dto.CatalogDiscoveryResult;
import com.ecommerce.app.product.dto.CatalogFilterGroupView;
import com.ecommerce.app.product.dto.CatalogFilterOptionView;
import com.ecommerce.app.product.model.Attribute;
import com.ecommerce.app.product.model.AttributeOption;
import com.ecommerce.app.product.model.CategoryAttribute;
import com.ecommerce.app.product.model.ProductAttribute;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.ProductVariantOption;
import com.ecommerce.app.product.ripository.CategoryAttributeRepository;
import com.ecommerce.app.product.ripository.ProductAttributeRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 2026-05-15: Dynamic storefront discovery support for generic attribute
 * filters and keyword search across catalog specs plus variants.
 */
@Service
public class CatalogProductDiscoveryService {

    private static final String ATTRIBUTE_FILTER_PREFIX = "attributeFilter_";

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @Autowired
    private ProductVariantCatalogService productVariantCatalogService;

    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;

    @Transactional(readOnly = true)
    public CatalogDiscoveryResult refineCategoryProducts(String categoryUuid,
            List<Map<String, Object>> baseProducts,
            String keyword,
            Map<String, List<String>> selectedFilters) {

        CatalogDiscoveryResult result = new CatalogDiscoveryResult();
        List<Map<String, Object>> safeProducts = baseProducts == null ? List.of() : baseProducts;
        Map<String, List<String>> safeSelectedFilters = selectedFilters == null ? Map.of() : selectedFilters;
        if (safeProducts.isEmpty()) {
            result.setFilteredProducts(List.of());
            result.setFilterGroups(List.of());
            return result;
        }

        Map<Long, Set<String>> searchTokensByProductId = initializeSearchTokensIndex(safeProducts);
        Map<Long, Map<String, Set<String>>> optionUuidsByProductId = initializeOptionIndex(safeProducts);
        populateDiscoveryIndexes(searchTokensByProductId, optionUuidsByProductId);
        List<Map<String, Object>> filteredProducts = safeProducts.stream()
                .filter(product -> matchesKeyword(product, searchTokensByProductId.get(resolveProductId(product)), keyword))
                .filter(product -> matchesDynamicFilters(optionUuidsByProductId.get(resolveProductId(product)), safeSelectedFilters))
                .toList();

        result.setFilteredProducts(filteredProducts);
        result.setFilterGroups(buildDynamicFilterGroups(categoryUuid, safeProducts, optionUuidsByProductId, safeSelectedFilters));
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> applyKeywordSearch(List<Map<String, Object>> baseProducts, String keyword) {
        List<Map<String, Object>> safeProducts = baseProducts == null ? List.of() : baseProducts;
        if (safeProducts.isEmpty() || keyword == null || keyword.isBlank()) {
            return safeProducts;
        }

        Map<Long, Set<String>> searchTokensByProductId = initializeSearchTokensIndex(safeProducts);
        Map<Long, Map<String, Set<String>>> optionUuidsByProductId = initializeOptionIndex(safeProducts);
        populateDiscoveryIndexes(searchTokensByProductId, optionUuidsByProductId);
        return safeProducts.stream()
                .filter(product -> matchesKeyword(product, searchTokensByProductId.get(resolveProductId(product)), keyword))
                .toList();
    }

    public Map<String, List<String>> extractSelectedAttributeFilters(Map<String, String[]> parameterMap) {
        Map<String, List<String>> selectedFilters = new LinkedHashMap<>();
        if (parameterMap == null || parameterMap.isEmpty()) {
            return selectedFilters;
        }

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.startsWith(ATTRIBUTE_FILTER_PREFIX)) {
                continue;
            }

            String attributeUuid = key.substring(ATTRIBUTE_FILTER_PREFIX.length());
            List<String> optionUuids = java.util.Arrays.stream(entry.getValue() == null ? new String[0] : entry.getValue())
                    .map(this::cleanText)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (!optionUuids.isEmpty()) {
                selectedFilters.put(attributeUuid, optionUuids);
            }
        }

        return selectedFilters;
    }

    public boolean hasDynamicFilters(Map<String, List<String>> selectedFilters) {
        return selectedFilters != null && selectedFilters.values().stream().anyMatch(values -> values != null && !values.isEmpty());
    }

    public String buildFilterFieldName(String attributeUuid) {
        return ATTRIBUTE_FILTER_PREFIX + attributeUuid;
    }

    private Map<Long, Set<String>> initializeSearchTokensIndex(List<Map<String, Object>> products) {
        List<Long> productIds = products.stream()
                .map(this::resolveProductId)
                .filter(id -> id > 0)
                .distinct()
                .toList();

        Map<Long, Set<String>> searchTokensByProductId = new LinkedHashMap<>();
        for (Long productId : productIds) {
            searchTokensByProductId.put(productId, new LinkedHashSet<>());
        }

        return searchTokensByProductId;
    }

    private Map<Long, Map<String, Set<String>>> initializeOptionIndex(List<Map<String, Object>> products) {
        List<Long> productIds = products.stream()
                .map(this::resolveProductId)
                .filter(id -> id > 0)
                .distinct()
                .toList();

        Map<Long, Map<String, Set<String>>> optionUuidsByProductId = new LinkedHashMap<>();
        for (Long productId : productIds) {
            optionUuidsByProductId.put(productId, new LinkedHashMap<>());
        }

        return optionUuidsByProductId;
    }

    private void populateDiscoveryIndexes(Map<Long, Set<String>> searchTokensByProductId,
            Map<Long, Map<String, Set<String>>> optionUuidsByProductId) {

        if (searchTokensByProductId.isEmpty() || optionUuidsByProductId.isEmpty()) {
            return;
        }

        List<Long> productIds = searchTokensByProductId.keySet().stream().toList();
        for (ProductAttribute attributeValue : productAttributeRepository.findDisplayRowsByProductIds(productIds)) {
            if (attributeValue.getProduct() == null || attributeValue.getProduct().getId() == null) {
                continue;
            }
            Long productId = attributeValue.getProduct().getId();
            Set<String> searchTokens = searchTokensByProductId.get(productId);
            Map<String, Set<String>> optionUuidsByAttribute = optionUuidsByProductId.get(productId);
            if (searchTokens == null || optionUuidsByAttribute == null) {
                continue;
            }

            Attribute attribute = attributeValue.getAttribute();
            if (attribute != null) {
                String attributeUuid = cleanText(attribute.getUuid());
                String displayValue = cleanText(attributeValue.getDisplayValue());
                if (displayValue != null) {
                    searchTokens.add(displayValue.toLowerCase(Locale.ROOT));
                }
                if (attributeUuid != null && attributeValue.getAttributeOption() != null) {
                    optionUuidsByAttribute.computeIfAbsent(attributeUuid, key -> new LinkedHashSet<>())
                            .add(attributeValue.getAttributeOption().getUuid());
                }
            }
        }

        for (ProductVariant variant : productVariantCatalogService.findByProductIds(productIds)) {
            if (variant.getProduct() == null || variant.getProduct().getId() == null) {
                continue;
            }
            Long productId = variant.getProduct().getId();
            Set<String> searchTokens = searchTokensByProductId.get(productId);
            Map<String, Set<String>> optionUuidsByAttribute = optionUuidsByProductId.get(productId);
            if (searchTokens == null || optionUuidsByAttribute == null) {
                continue;
            }

            String sku = cleanText(variant.getSku());
            if (sku != null) {
                searchTokens.add(sku.toLowerCase(Locale.ROOT));
            }

            if (variant.getOptions() == null) {
                continue;
            }
            for (ProductVariantOption optionRow : variant.getOptions()) {
                if (optionRow.getAttribute() == null) {
                    continue;
                }
                String attributeUuid = cleanText(optionRow.getAttribute().getUuid());
                String displayValue = cleanText(optionRow.getDisplayValue());
                if (displayValue != null) {
                    searchTokens.add(displayValue.toLowerCase(Locale.ROOT));
                }
                if (attributeUuid != null && optionRow.getAttributeOption() != null) {
                    optionUuidsByAttribute.computeIfAbsent(attributeUuid, key -> new LinkedHashSet<>())
                            .add(optionRow.getAttributeOption().getUuid());
                }
            }
        }
    }

    private List<CatalogFilterGroupView> buildDynamicFilterGroups(String categoryUuid,
            List<Map<String, Object>> baseProducts,
            Map<Long, Map<String, Set<String>>> optionUuidsByProductId,
            Map<String, List<String>> selectedFilters) {

        if (categoryUuid == null || categoryUuid.isBlank()) {
            return List.of();
        }

        List<CategoryAttribute> mappings = categoryAttributeRepository.findActiveMappingsWithAttributeOptions(categoryUuid).stream()
                .filter(mapping -> Boolean.TRUE.equals(mapping.getActive()))
                .filter(mapping -> mapping.getAttribute() != null)
                .filter(mapping -> Boolean.TRUE.equals(mapping.getAttribute().isFilterable()))
                .sorted(java.util.Comparator.comparing(mapping -> mapping.getDisplayOrder() == null ? 0 : mapping.getDisplayOrder()))
                .toList();

        List<CatalogFilterGroupView> groups = new ArrayList<>();
        for (CategoryAttribute mapping : mappings) {
            Attribute attribute = mapping.getAttribute();
            List<AttributeOption> optionRows = attribute.getOptions() == null ? List.of()
                    : attribute.getOptions().stream()
                            .filter(optionRow -> Boolean.TRUE.equals(optionRow.getActive()))
                            .sorted(java.util.Comparator.comparing(optionRow -> optionRow.getSortOrder() == null ? 0 : optionRow.getSortOrder()))
                            .toList();
            if (optionRows.isEmpty()) {
                continue;
            }

            Map<String, Long> optionCounts = new LinkedHashMap<>();
            for (Map<String, Object> product : baseProducts) {
                Map<String, Set<String>> optionUuidsByAttribute = optionUuidsByProductId.get(resolveProductId(product));
                if (optionUuidsByAttribute == null) {
                    continue;
                }
                Set<String> optionUuids = optionUuidsByAttribute.getOrDefault(attribute.getUuid(), Set.of());
                for (String optionUuid : optionUuids) {
                    optionCounts.merge(optionUuid, 1L, Long::sum);
                }
            }

            List<String> selectedOptionUuids = selectedFilters.getOrDefault(attribute.getUuid(), List.of());
            List<CatalogFilterOptionView> optionViews = optionRows.stream()
                    .filter(optionRow -> optionCounts.containsKey(optionRow.getUuid()) || selectedOptionUuids.contains(optionRow.getUuid()))
                    .map(optionRow -> {
                        CatalogFilterOptionView optionView = new CatalogFilterOptionView();
                        optionView.setOptionUuid(optionRow.getUuid());
                        optionView.setLabel(optionRow.getLabel());
                        optionView.setCount(optionCounts.getOrDefault(optionRow.getUuid(), 0L));
                        optionView.setSelected(selectedOptionUuids.contains(optionRow.getUuid()));
                        return optionView;
                    })
                    .toList();

            if (optionViews.isEmpty()) {
                continue;
            }

            CatalogFilterGroupView groupView = new CatalogFilterGroupView();
            groupView.setAttributeUuid(attribute.getUuid());
            groupView.setAttributeCode(attribute.getCode());
            groupView.setAttributeName(attribute.getName());
            groupView.setSelectedOptionUuids(new ArrayList<>(selectedOptionUuids));
            groupView.setOptions(optionViews);
            groups.add(groupView);
        }

        return groups;
    }

    private boolean matchesKeyword(Map<String, Object> product, Set<String> searchTokens, String keyword) {
        String normalizedKeyword = cleanText(keyword);
        if (normalizedKeyword == null) {
            return true;
        }

        Set<String> haystackTokens = new LinkedHashSet<>();
        addSearchText(haystackTokens, valueAsString(product.get("title")));
        addSearchText(haystackTokens, valueAsString(product.get("slug")));
        addSearchText(haystackTokens, valueAsString(product.get("shortDescription")));
        addSearchText(haystackTokens, valueAsString(product.get("description")));
        addSearchText(haystackTokens, valueAsString(product.get("manufacturerName")));
        if (searchTokens != null) {
            haystackTokens.addAll(searchTokens);
        }

        String haystack = String.join(" ", haystackTokens);
        for (String token : normalizedKeyword.toLowerCase(Locale.ROOT).split("\\s+")) {
            if (!haystack.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesDynamicFilters(Map<String, Set<String>> optionUuidsByAttribute,
            Map<String, List<String>> selectedFilters) {
        if (selectedFilters == null || selectedFilters.isEmpty()) {
            return true;
        }
        if (optionUuidsByAttribute == null) {
            return false;
        }

        for (Map.Entry<String, List<String>> entry : selectedFilters.entrySet()) {
            List<String> selectedOptionUuids = entry.getValue();
            if (selectedOptionUuids == null || selectedOptionUuids.isEmpty()) {
                continue;
            }

            Set<String> availableOptions = optionUuidsByAttribute.getOrDefault(entry.getKey(), Set.of());
            boolean matched = selectedOptionUuids.stream().anyMatch(availableOptions::contains);
            if (!matched) {
                return false;
            }
        }

        return true;
    }

    private void addSearchText(Set<String> haystackTokens, String value) {
        String cleaned = cleanText(value);
        if (cleaned != null) {
            haystackTokens.add(cleaned.toLowerCase(Locale.ROOT));
        }
    }

    private Long resolveProductId(Map<String, Object> product) {
        if (product == null) {
            return 0L;
        }
        Object idValue = product.get("id");
        if (idValue instanceof Number number) {
            return number.longValue();
        }
        if (idValue instanceof String text) {
            try {
                return Long.valueOf(text.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueAsString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
