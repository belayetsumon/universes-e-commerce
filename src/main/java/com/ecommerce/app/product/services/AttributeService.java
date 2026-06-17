package com.ecommerce.app.product.services;

import com.ecommerce.app.exception.ForeignKeyConstraintException;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.exception.UniqueConstraintViolationException;
import com.ecommerce.app.product.dto.CatalogAttributeListView;
import com.ecommerce.app.product.model.Attribute;
import com.ecommerce.app.product.model.AttributeInputType;
import com.ecommerce.app.product.model.AttributeValueType;
import com.ecommerce.app.product.model.CategoryAttribute;
import com.ecommerce.app.product.ripository.AttributeRepository;
import com.ecommerce.app.product.ripository.CategoryAttributeRepository;
import com.ecommerce.app.product.ripository.ProductAttributeRepository;
import com.ecommerce.app.product.ripository.ProductVariantOptionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttributeService {

    @Autowired
    private AttributeRepository repository;

    @Autowired
    private CategoryAttributeRepository categoryAttributeRepository;

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @Autowired
    private ProductVariantOptionRepository productVariantOptionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Attribute> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Attribute> root = cq.from(Attribute.class);

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("name").alias("name"),
                root.get("code").alias("code"),
                root.get("description").alias("description"),
                root.get("inputType").alias("inputType"),
                root.get("valueType").alias("valueType"),
                root.get("allowMultipleValues").alias("allowMultipleValues"),
                root.get("variantCapable").alias("variantCapable"),
                root.get("filterable").alias("filterable"),
                root.get("searchable").alias("searchable"),
                root.get("comparable").alias("comparable"),
                root.get("active").alias("active"),
                root.get("version").alias("version"),
                root.get("createdOn").alias("createdOn"),
                root.get("createdBy").alias("createdBy"),
                root.get("updatedOn").alias("updatedOn"),
                root.get("updatedBy").alias("updatedBy")
        );
        cq.orderBy(cb.asc(root.get("name")), cb.asc(root.get("id")));

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(this::mapAttribute)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogAttributeListView> findListRows(String keyword,
            Boolean active,
            boolean filterableOnly,
            boolean variantCapableOnly,
            boolean searchableOnly,
            boolean comparableOnly,
            boolean allowMultipleValuesOnly) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Attribute> root = cq.from(Attribute.class);

        Subquery<Long> mappingCountSubquery = cq.subquery(Long.class);
        Root<CategoryAttribute> mappingRoot = mappingCountSubquery.from(CategoryAttribute.class);
        mappingCountSubquery.select(cb.count(mappingRoot));
        mappingCountSubquery.where(cb.equal(mappingRoot.get("attribute").get("id"), root.get("id")));

        List<Predicate> predicates = new ArrayList<>();
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && !normalizedKeyword.isBlank()) {
            String likePattern = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
            predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
        }
        if (active != null) {
            predicates.add(cb.equal(root.get("active"), active));
        }
        if (filterableOnly) {
            predicates.add(cb.isTrue(root.get("filterable")));
        }
        if (variantCapableOnly) {
            predicates.add(cb.isTrue(root.get("variantCapable")));
        }
        if (searchableOnly) {
            predicates.add(cb.isTrue(root.get("searchable")));
        }
        if (comparableOnly) {
            predicates.add(cb.isTrue(root.get("comparable")));
        }
        if (allowMultipleValuesOnly) {
            predicates.add(cb.isTrue(root.get("allowMultipleValues")));
        }

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("name").alias("name"),
                root.get("description").alias("description"),
                root.get("inputType").alias("inputType"),
                root.get("valueType").alias("valueType"),
                root.get("allowMultipleValues").alias("allowMultipleValues"),
                root.get("variantCapable").alias("variantCapable"),
                root.get("filterable").alias("filterable"),
                root.get("searchable").alias("searchable"),
                root.get("comparable").alias("comparable"),
                root.get("active").alias("active"),
                mappingCountSubquery.alias("categoryMappingCount")
        );
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(Predicate[]::new));
        }
        cq.orderBy(cb.asc(root.get("name")), cb.asc(root.get("id")));

        List<CatalogAttributeListView> rows = entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(this::mapListView)
                .toList();
        attachCategoryNames(rows);
        return rows;
    }

    @Transactional
    public Attribute save(Attribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute data is required.");
        }

        String normalizedCode = normalizeCode(attribute.getCode(), attribute.getName(), "attribute name");
        validateInputValueCompatibility(attribute.getInputType(), attribute.getValueType());

        repository.findByCodeIgnoreCase(normalizedCode)
                .filter(existing -> attribute.getId() == null || !existing.getId().equals(attribute.getId()))
                .ifPresent(existing -> {
                    throw new UniqueConstraintViolationException("An attribute with code '" + normalizedCode + "' already exists.");
                });

        Attribute target = resolveAttributeForSave(attribute);
        target.setName(attribute.getName());
        target.setCode(normalizedCode);
        target.setDescription(attribute.getDescription());
        target.setInputType(attribute.getInputType());
        target.setValueType(attribute.getValueType());
        target.setAllowMultipleValues(attribute.isAllowMultipleValues());
        target.setVariantCapable(attribute.isVariantCapable());
        target.setFilterable(attribute.isFilterable());
        target.setSearchable(attribute.isSearchable());
        target.setComparable(attribute.isComparable());
        target.setActive(attribute.isActive());

        return repository.save(target);
    }

    @Transactional(readOnly = true)
    public Attribute findByUuid(String uuid) {
        Attribute attribute = findOneBy("uuid", uuid);
        if (attribute == null) {
            throw new ResourceNotFoundException("Catalog attribute not found.");
        }
        return attribute;
    }

    @Transactional(readOnly = true)
    public Attribute findByCode(String code) {
        String normalizedCode = normalizeCode(code, code, "attribute code");
        Attribute attribute = findOneBy("code", normalizedCode);
        if (attribute == null) {
            throw new ResourceNotFoundException("Catalog attribute not found for code '" + normalizedCode + "'.");
        }
        return attribute;
    }

    @Transactional
    public void deleteByUuid(String uuid) {
        Attribute attribute = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog attribute not found."));

        if (categoryAttributeRepository.existsByAttribute_Id(attribute.getId())) {
            throw new ForeignKeyConstraintException("This attribute cannot be deleted because it is linked to one or more category mappings.");
        }
        if (productAttributeRepository.existsByAttribute_Id(attribute.getId())) {
            throw new ForeignKeyConstraintException("This attribute cannot be deleted because it is already used in saved product specifications.");
        }
        if (productVariantOptionRepository.existsByAttribute_Id(attribute.getId())) {
            throw new ForeignKeyConstraintException("This attribute cannot be deleted because it is already used in catalog variants.");
        }

        repository.delete(attribute);
    }

    private Attribute resolveAttributeForSave(Attribute attribute) {
        if (attribute.getId() == null) {
            Attribute newAttribute = new Attribute();
            if (attribute.getUuid() != null && !attribute.getUuid().isBlank()) {
                newAttribute.setUuid(attribute.getUuid());
            }
            return newAttribute;
        }

        Attribute existing = repository.findById(attribute.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Catalog attribute not found."));

        if (attribute.getVersion() != null
                && existing.getVersion() != null
                && !attribute.getVersion().equals(existing.getVersion())) {
            throw new IllegalStateException("This attribute was updated by another user. Please reload the page and try again.");
        }

        return existing;
    }

    private Attribute findOneBy(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Attribute> root = cq.from(Attribute.class);
        Predicate predicate = cb.equal(root.get(fieldName), value);

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("name").alias("name"),
                root.get("code").alias("code"),
                root.get("description").alias("description"),
                root.get("inputType").alias("inputType"),
                root.get("valueType").alias("valueType"),
                root.get("allowMultipleValues").alias("allowMultipleValues"),
                root.get("variantCapable").alias("variantCapable"),
                root.get("filterable").alias("filterable"),
                root.get("searchable").alias("searchable"),
                root.get("comparable").alias("comparable"),
                root.get("active").alias("active"),
                root.get("version").alias("version"),
                root.get("createdOn").alias("createdOn"),
                root.get("createdBy").alias("createdBy"),
                root.get("updatedOn").alias("updatedOn"),
                root.get("updatedBy").alias("updatedBy")
        );
        cq.where(predicate);

        return entityManager.createQuery(cq)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(this::mapAttribute)
                .orElse(null);
    }

    private Attribute mapAttribute(Tuple tuple) {
        Attribute attribute = new Attribute();
        attribute.setId(tuple.get("id", Long.class));
        attribute.setUuid(tuple.get("uuid", String.class));
        attribute.setName(tuple.get("name", String.class));
        attribute.setCode(tuple.get("code", String.class));
        attribute.setDescription(tuple.get("description", String.class));
        attribute.setInputType(tuple.get("inputType", AttributeInputType.class));
        attribute.setValueType(tuple.get("valueType", AttributeValueType.class));
        attribute.setAllowMultipleValues(Boolean.TRUE.equals(tuple.get("allowMultipleValues", Boolean.class)));
        attribute.setVariantCapable(Boolean.TRUE.equals(tuple.get("variantCapable", Boolean.class)));
        attribute.setFilterable(Boolean.TRUE.equals(tuple.get("filterable", Boolean.class)));
        attribute.setSearchable(Boolean.TRUE.equals(tuple.get("searchable", Boolean.class)));
        attribute.setComparable(Boolean.TRUE.equals(tuple.get("comparable", Boolean.class)));
        attribute.setActive(Boolean.TRUE.equals(tuple.get("active", Boolean.class)));
        attribute.setVersion(tuple.get("version", Long.class));
        attribute.setCreatedOn(tuple.get("createdOn", Instant.class));
        attribute.setCreatedBy(tuple.get("createdBy", String.class));
        attribute.setUpdatedOn(tuple.get("updatedOn", Instant.class));
        attribute.setUpdatedBy(tuple.get("updatedBy", String.class));
        return attribute;
    }

    private CatalogAttributeListView mapListView(Tuple tuple) {
        CatalogAttributeListView view = new CatalogAttributeListView();
        view.setId(tuple.get("id", Long.class));
        view.setUuid(tuple.get("uuid", String.class));
        view.setName(tuple.get("name", String.class));
        view.setDescription(tuple.get("description", String.class));
        view.setInputType(tuple.get("inputType", AttributeInputType.class));
        view.setValueType(tuple.get("valueType", AttributeValueType.class));
        view.setAllowMultipleValues(Boolean.TRUE.equals(tuple.get("allowMultipleValues", Boolean.class)));
        view.setVariantCapable(Boolean.TRUE.equals(tuple.get("variantCapable", Boolean.class)));
        view.setFilterable(Boolean.TRUE.equals(tuple.get("filterable", Boolean.class)));
        view.setSearchable(Boolean.TRUE.equals(tuple.get("searchable", Boolean.class)));
        view.setComparable(Boolean.TRUE.equals(tuple.get("comparable", Boolean.class)));
        view.setActive(Boolean.TRUE.equals(tuple.get("active", Boolean.class)));

        Number categoryMappingCount = tuple.get("categoryMappingCount", Number.class);
        view.setCategoryMappingCount(categoryMappingCount == null ? 0L : categoryMappingCount.longValue());
        return view;
    }

    private void attachCategoryNames(List<CatalogAttributeListView> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }

        List<Long> attributeIds = rows.stream()
                .map(CatalogAttributeListView::getId)
                .filter(id -> id != null)
                .toList();
        if (attributeIds.isEmpty()) {
            return;
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<CategoryAttribute> root = cq.from(CategoryAttribute.class);

        cq.multiselect(
                root.get("attribute").get("id").alias("attributeId"),
                root.get("category").get("name").alias("categoryName")
        );
        cq.where(root.get("attribute").get("id").in(attributeIds));
        cq.orderBy(
                cb.asc(root.get("attribute").get("id")),
                cb.asc(root.get("category").get("name")),
                cb.asc(root.get("id"))
        );

        Map<Long, List<String>> categoryNamesByAttributeId = new HashMap<>();
        entityManager.createQuery(cq)
                .getResultList()
                .forEach(tuple -> {
                    Long attributeId = tuple.get("attributeId", Long.class);
                    String categoryName = tuple.get("categoryName", String.class);
                    if (attributeId == null || categoryName == null || categoryName.isBlank()) {
                        return;
                    }
                    List<String> categoryNames = categoryNamesByAttributeId.computeIfAbsent(attributeId, key -> new ArrayList<>());
                    if (!categoryNames.contains(categoryName)) {
                        categoryNames.add(categoryName);
                    }
                });

        rows.forEach(row -> row.setCategoryNames(categoryNamesByAttributeId.get(row.getId())));
    }

    private String normalizeCode(String code, String fallbackSource, String fallbackLabel) {
        String source = code == null || code.isBlank() ? fallbackSource : code;
        if (source == null) {
            throw new IllegalArgumentException("A valid " + fallbackLabel + " is required to generate the attribute code.");
        }

        String normalized = source.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Attribute code must include at least one letter or number.");
        }
        return normalized;
    }

    private void validateInputValueCompatibility(AttributeInputType inputType, AttributeValueType valueType) {
        if (inputType == null) {
            throw new IllegalArgumentException("Please select an input type.");
        }
        if (valueType == null) {
            throw new IllegalArgumentException("Please select a value type.");
        }

        switch (inputType) {
            case BOOLEAN -> {
                if (valueType != AttributeValueType.BOOLEAN) {
                    throw new IllegalArgumentException("Boolean attributes must use the BOOLEAN value type.");
                }
            }
            case DATE -> {
                if (valueType != AttributeValueType.DATE) {
                    throw new IllegalArgumentException("Date attributes must use the DATE value type.");
                }
            }
            case NUMBER -> {
                if (valueType != AttributeValueType.INTEGER) {
                    throw new IllegalArgumentException("Number attributes must use the INTEGER value type.");
                }
            }
            case DECIMAL -> {
                if (valueType != AttributeValueType.DECIMAL) {
                    throw new IllegalArgumentException("Decimal attributes must use the DECIMAL value type.");
                }
            }
            case TEXTAREA -> {
                if (valueType != AttributeValueType.TEXT && valueType != AttributeValueType.LONG_TEXT) {
                    throw new IllegalArgumentException("Textarea attributes must use the TEXT or LONG_TEXT value type.");
                }
            }
            case SINGLE_SELECT, MULTI_SELECT, TEXT -> {
                if (valueType != AttributeValueType.TEXT && valueType != AttributeValueType.LONG_TEXT) {
                    throw new IllegalArgumentException("Text and select attributes must use the TEXT or LONG_TEXT value type.");
                }
            }
        }
    }
}
