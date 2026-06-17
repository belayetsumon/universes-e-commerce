package com.ecommerce.app.product.services;

import com.ecommerce.app.product.dto.CatalogAttributeFieldView;
import com.ecommerce.app.product.dto.CatalogAttributeOptionView;
import com.ecommerce.app.product.dto.ProductSpecificationView;
import com.ecommerce.app.product.model.Attribute;
import com.ecommerce.app.product.model.AttributeInputType;
import com.ecommerce.app.product.model.AttributeOption;
import com.ecommerce.app.product.model.CategoryAttribute;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductAttribute;
import com.ecommerce.app.product.ripository.ProductAttributeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 2026-05-15: Central service for category-driven product specifications. It
 * renders form fields, validates submission payloads, stores normalized values,
 * and prepares read models for detail pages.
 */
@Service
public class CatalogProductAttributeService {

    @Autowired
    private CategoryAttributeService categoryAttributeService;

    @Autowired
    private ProductAttributeService productAttributeService;

    @Autowired
    private AttributeOptionService attributeOptionService;

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<CatalogAttributeFieldView> buildFieldsForProduct(String categoryUuid, String productUuid) {
        List<CategoryAttribute> mappings = loadMappings(categoryUuid);
        Map<String, List<ProductAttribute>> productValues = groupExistingValues(productUuid);
        return buildFieldViews(mappings, productValues, Map.of());
    }

    public List<CatalogAttributeFieldView> buildFieldsFromSubmission(String categoryUuid, Map<String, String[]> submittedParams) {
        List<CategoryAttribute> mappings = loadMappings(categoryUuid);
        return buildFieldViews(mappings, Map.of(), submittedParams == null ? Map.of() : submittedParams);
    }

    public List<String> validateSubmittedValues(String categoryUuid, Map<String, String[]> submittedParams) {
        List<String> errors = new ArrayList<>();
        for (CategoryAttribute mapping : loadMappings(categoryUuid)) {
            Attribute attribute = mapping.getAttribute();
            List<String> values = extractSubmittedValues(attribute, submittedParams);
            if (Boolean.TRUE.equals(mapping.getRequired()) && values.isEmpty()) {
                errors.add(attribute.getName() + " is required.");
                continue;
            }
            for (String value : values) {
                validateSubmittedValue(attribute, value, errors);
            }
        }
        return errors;
    }

    @Transactional
    public void replaceProductAttributes(Product product, String categoryUuid, Map<String, String[]> submittedParams) {
        if (product == null || product.getId() == null || product.getUuid() == null || categoryUuid == null || categoryUuid.isBlank()) {
            return;
        }

        Product managedProduct = entityManager.getReference(Product.class, product.getId());
        List<CategoryAttribute> mappings = loadMappings(categoryUuid);
        productAttributeRepository.deleteByProduct_Uuid(product.getUuid());

        for (CategoryAttribute mapping : mappings) {
            Attribute attribute = mapping.getAttribute();
            List<String> submittedValues = extractSubmittedValues(attribute, submittedParams);
            int sortOrder = 0;
            for (String submittedValue : submittedValues) {
                ProductAttribute row = buildAttributeRow(managedProduct, attribute, submittedValue, sortOrder++);
                if (row != null) {
                    productAttributeRepository.save(row);
                }
            }
        }
    }

    public List<ProductSpecificationView> buildSpecificationViews(String productUuid) {
        if (productUuid == null || productUuid.isBlank()) {
            return List.of();
        }

        Map<String, ProductSpecificationView> groupedRows = new LinkedHashMap<>();
        for (ProductAttribute row : productAttributeService.findByProductUuid(productUuid)) {
            String group = row.getAttribute() != null ? row.getAttribute().getName() : "";
            String key = group;
            String currentValue = groupedRows.containsKey(key) ? groupedRows.get(key).getValue() : "";
            String nextValue = row.getDisplayValue();

            if (nextValue == null || nextValue.isBlank()) {
                continue;
            }

            if (groupedRows.containsKey(key)) {
                Set<String> values = new LinkedHashSet<>(Arrays.stream(currentValue.split(", "))
                        .filter(text -> text != null && !text.isBlank())
                        .toList());
                values.add(nextValue);
                groupedRows.get(key).setValue(String.join(", ", values));
            } else {
                groupedRows.put(key, new ProductSpecificationView(
                        null,
                        row.getAttribute().getName(),
                        nextValue
                ));
            }
        }

        return new ArrayList<>(groupedRows.values());
    }

    private List<CategoryAttribute> loadMappings(String categoryUuid) {
        if (categoryUuid == null || categoryUuid.isBlank()) {
            return List.of();
        }
        return categoryAttributeService.findActiveByCategoryUuid(categoryUuid);
    }

    private Map<String, List<ProductAttribute>> groupExistingValues(String productUuid) {
        if (productUuid == null || productUuid.isBlank()) {
            return Map.of();
        }
        return productAttributeService.findByProductUuid(productUuid).stream()
                .filter(row -> row.getAttribute() != null && row.getAttribute().getUuid() != null)
                .collect(Collectors.groupingBy(row -> row.getAttribute().getUuid(), LinkedHashMap::new, Collectors.toList()));
    }

    private List<CatalogAttributeFieldView> buildFieldViews(List<CategoryAttribute> mappings,
            Map<String, List<ProductAttribute>> productValues,
            Map<String, String[]> submittedParams) {
        List<CatalogAttributeFieldView> fields = new ArrayList<>();

        for (CategoryAttribute mapping : mappings) {
            Attribute attribute = mapping.getAttribute();
            List<ProductAttribute> existingRows = productValues.getOrDefault(attribute.getUuid(), List.of());
            CatalogAttributeFieldView field = new CatalogAttributeFieldView();
            field.setMappingUuid(mapping.getUuid());
            field.setAttributeUuid(attribute.getUuid());
            field.setAttributeName(attribute.getName());
            field.setAttributeCode(attribute.getCode());
            field.setAttributeGroup(mapping.getAttributeGroup());
            field.setHelperText(mapping.getHelperText());
            field.setInputType(attribute.getInputType());
            field.setValueType(attribute.getValueType());
            field.setRequired(mapping.getRequired());
            field.setAllowMultipleValues(attribute.isAllowMultipleValues());
            field.setVariantAttribute(mapping.getVariantAttribute());
            field.setOptions(attributeOptionService.findActiveByAttributeUuid(attribute.getUuid()).stream()
                    .map(option -> new CatalogAttributeOptionView(option.getUuid(), option.getLabel()))
                    .toList());

            List<String> submittedValues = extractSubmittedValues(attribute, submittedParams);
            if (!submittedValues.isEmpty()) {
                applySubmittedValues(field, attribute, submittedValues);
            } else {
                applyExistingValues(field, attribute, existingRows);
            }

            fields.add(field);
        }

        return fields;
    }

    private void applySubmittedValues(CatalogAttributeFieldView field, Attribute attribute, List<String> submittedValues) {
        if (isOptionBased(attribute)) {
            field.setSelectedOptionUuids(new ArrayList<>(submittedValues));
            return;
        }

        if (Boolean.TRUE.equals(attribute.isAllowMultipleValues()) && submittedValues.size() > 1) {
            field.setMultiLineValue(String.join(System.lineSeparator(), submittedValues));
        } else {
            field.setScalarValue(submittedValues.get(0));
            field.setMultiLineValue(submittedValues.get(0));
        }
    }

    private void applyExistingValues(CatalogAttributeFieldView field, Attribute attribute, List<ProductAttribute> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        if (isOptionBased(attribute)) {
            field.setSelectedOptionUuids(rows.stream()
                    .map(ProductAttribute::getAttributeOption)
                    .filter(Objects::nonNull)
                    .map(AttributeOption::getUuid)
                    .toList());
            return;
        }

        List<String> displayValues = rows.stream()
                .map(ProductAttribute::getDisplayValue)
                .filter(text -> text != null && !text.isBlank())
                .toList();

        if (displayValues.isEmpty()) {
            return;
        }

        if (Boolean.TRUE.equals(attribute.isAllowMultipleValues()) && displayValues.size() > 1) {
            field.setMultiLineValue(String.join(System.lineSeparator(), displayValues));
        } else {
            field.setScalarValue(displayValues.get(0));
            field.setMultiLineValue(displayValues.get(0));
        }
    }

    private List<String> extractSubmittedValues(Attribute attribute, Map<String, String[]> submittedParams) {
        if (attribute == null || submittedParams == null || submittedParams.isEmpty()) {
            return List.of();
        }

        String fieldName = fieldName(attribute.getUuid());
        String[] rawValues = submittedParams.get(fieldName);
        if (rawValues == null || rawValues.length == 0) {
            return List.of();
        }

        if (attribute.getInputType() == AttributeInputType.BOOLEAN) {
            String lastValue = Arrays.stream(rawValues)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(text -> !text.isBlank())
                    .reduce((first, second) -> second)
                    .orElse("false");
            return List.of(lastValue);
        }

        if (!isOptionBased(attribute) && Boolean.TRUE.equals(attribute.isAllowMultipleValues())) {
            return Arrays.stream(rawValues)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .flatMap(value -> Arrays.stream(value.split("\\r?\\n")))
                    .map(String::trim)
                    .filter(text -> !text.isBlank())
                    .distinct()
                    .toList();
        }

        return Arrays.stream(rawValues)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .distinct()
                .toList();
    }

    private ProductAttribute buildAttributeRow(Product product, Attribute attribute, String submittedValue, int sortOrder) {
        if (submittedValue == null || submittedValue.isBlank()) {
            return null;
        }
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Save the basic product information before adding specifications.");
        }
        if (attribute == null || attribute.getId() == null) {
            throw new IllegalArgumentException("One of the selected category specifications is no longer linked to a valid attribute. Please refresh the page and try again.");
        }

        ProductAttribute row = new ProductAttribute();
        row.setProduct(product);
        row.setAttribute(entityManager.getReference(Attribute.class, attribute.getId()));
        row.setSortOrder(sortOrder);

        if (isOptionBased(attribute)) {
            AttributeOption option = attributeOptionService.findActiveByUuidAndAttributeId(submittedValue, attribute.getId());
            row.setAttributeOption(option);
            row.setDisplayValue(option.getLabel());
            row.setTextValue(option.getValue());
            return row;
        }

        switch (attribute.getValueType()) {
            case BOOLEAN -> {
                if (!"true".equalsIgnoreCase(submittedValue) && !"false".equalsIgnoreCase(submittedValue)) {
                    throw new IllegalArgumentException(attribute.getName() + " must be true or false.");
                }
                row.setBooleanValue(Boolean.parseBoolean(submittedValue));
            }
            case DATE -> {
                try {
                    row.setDateValue(LocalDate.parse(submittedValue));
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException(attribute.getName() + " must be a valid date in YYYY-MM-DD format.", ex);
                }
            }
            case INTEGER -> {
                try {
                    row.setIntegerValue(Long.valueOf(submittedValue));
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException(attribute.getName() + " must be a whole number.", ex);
                }
            }
            case DECIMAL -> {
                try {
                    row.setDecimalValue(new BigDecimal(submittedValue));
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException(attribute.getName() + " must be a valid decimal number.", ex);
                }
            }
            case LONG_TEXT ->
                row.setLongTextValue(submittedValue);
            case TEXT ->
                row.setTextValue(submittedValue);
        }

        row.setDisplayValue(formatDisplayValue(attribute, row, submittedValue));
        return row;
    }

    private String formatDisplayValue(Attribute attribute, ProductAttribute row, String submittedValue) {
        return switch (attribute.getValueType()) {
            case BOOLEAN ->
                Boolean.TRUE.equals(row.getBooleanValue()) ? "Yes" : "No";
            case DATE ->
                row.getDateValue() != null ? row.getDateValue().toString() : submittedValue;
            case INTEGER ->
                row.getIntegerValue() != null ? row.getIntegerValue().toString() : submittedValue;
            case DECIMAL ->
                row.getDecimalValue() != null ? row.getDecimalValue().stripTrailingZeros().toPlainString() : submittedValue;
            case LONG_TEXT, TEXT ->
                submittedValue;
        };
    }

    private void validateSubmittedValue(Attribute attribute, String submittedValue, List<String> errors) {
        if (attribute == null || submittedValue == null || submittedValue.isBlank()) {
            return;
        }

        if (isOptionBased(attribute)) {
            boolean validOption = attributeOptionService.isActiveOptionForAttribute(submittedValue, attribute.getId());
            if (!validOption) {
                errors.add(attribute.getName() + " contains an invalid option.");
            }
            return;
        }

        try {
            switch (attribute.getValueType()) {
                case BOOLEAN -> {
                    if (!"true".equalsIgnoreCase(submittedValue) && !"false".equalsIgnoreCase(submittedValue)) {
                        throw new IllegalArgumentException(attribute.getName() + " must be true or false.");
                    }
                }
                case DATE ->
                    LocalDate.parse(submittedValue);
                case INTEGER ->
                    Long.valueOf(submittedValue);
                case DECIMAL ->
                    new BigDecimal(submittedValue);
                case LONG_TEXT, TEXT -> {
                    // Free text values need no extra parsing.
                }
            }
        } catch (RuntimeException ex) {
            errors.add(attribute.getName() + " has an invalid value.");
        }
    }

    private boolean isOptionBased(Attribute attribute) {
        return attribute.getInputType() == AttributeInputType.SINGLE_SELECT
                || attribute.getInputType() == AttributeInputType.MULTI_SELECT;
    }

    public String fieldName(String attributeUuid) {
        return "attr_" + attributeUuid;
    }
}
