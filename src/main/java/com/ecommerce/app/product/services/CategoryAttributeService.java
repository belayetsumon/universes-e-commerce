package com.ecommerce.app.product.services;

import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.exception.UniqueConstraintViolationException;
import com.ecommerce.app.product.model.Attribute;
import com.ecommerce.app.product.model.CategoryAttribute;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.ripository.AttributeRepository;
import com.ecommerce.app.product.ripository.CategoryAttributeRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryAttributeService {

    @Autowired
    private CategoryAttributeRepository repository;

    @Autowired
    private ProductcategoryRepository productcategoryRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<CategoryAttribute> findByCategoryUuid(String categoryUuid) {
        return findByCategoryUuid(categoryUuid, false);
    }

    @Transactional(readOnly = true)
    public List<CategoryAttribute> findActiveByCategoryUuid(String categoryUuid) {
        return findByCategoryUuid(categoryUuid, true);
    }

    @Transactional
    public CategoryAttribute save(String categoryUuid, String attributeUuid, CategoryAttribute categoryAttribute) {
        if (categoryAttribute == null) {
            throw new IllegalArgumentException("Category attribute mapping data is required.");
        }
        if (categoryUuid == null || categoryUuid.isBlank()) {
            throw new IllegalArgumentException("Please select a category.");
        }
        if (attributeUuid == null || attributeUuid.isBlank()) {
            throw new IllegalArgumentException("Please select an attribute.");
        }

        Productcategory category = productcategoryRepository.findByUuid(categoryUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Selected category not found."));
        Attribute attribute = attributeRepository.findByUuid(attributeUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Selected attribute not found."));

        repository.findByCategory_IdAndAttribute_Id(category.getId(), attribute.getId())
                .filter(existing -> !existing.getUuid().equals(categoryAttribute.getUuid()))
                .ifPresent(existing -> {
                    throw new UniqueConstraintViolationException("This attribute is already mapped to the selected category.");
                });

        categoryAttribute.setCategory(category);
        categoryAttribute.setAttribute(attribute);
        return repository.save(categoryAttribute);
    }

    @Transactional(readOnly = true)
    public CategoryAttribute findByUuid(String uuid) {
        CategoryAttribute categoryAttribute = findOneByUuid(uuid);
        if (categoryAttribute == null) {
            throw new ResourceNotFoundException("Category attribute mapping not found.");
        }
        return categoryAttribute;
    }

    @Transactional
    public void deleteByUuid(String uuid) {
        CategoryAttribute categoryAttribute = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Category attribute mapping not found."));
        repository.delete(categoryAttribute);
    }

    private List<CategoryAttribute> findByCategoryUuid(String categoryUuid, boolean activeOnly) {
        if (categoryUuid == null || categoryUuid.isBlank()) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<CategoryAttribute> root = cq.from(CategoryAttribute.class);
        Join<CategoryAttribute, Productcategory> categoryJoin = root.join("category", JoinType.INNER);
        Join<CategoryAttribute, Attribute> attributeJoin = root.join("attribute", JoinType.INNER);

        Predicate predicate = cb.equal(categoryJoin.get("uuid"), categoryUuid);
        if (activeOnly) {
            predicate = cb.and(predicate, cb.isTrue(root.get("active")), cb.isTrue(attributeJoin.get("active")));
        }

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("required").alias("required"),
                root.get("variantAttribute").alias("variantAttribute"),
                root.get("displayOrder").alias("displayOrder"),
                root.get("active").alias("active"),
                root.get("attributeGroup").alias("attributeGroup"),
                root.get("helperText").alias("helperText"),
                root.get("version").alias("version"),
                root.get("createdOn").alias("createdOn"),
                root.get("createdBy").alias("createdBy"),
                root.get("updatedOn").alias("updatedOn"),
                root.get("updatedBy").alias("updatedBy"),
                categoryJoin.get("id").alias("categoryId"),
                categoryJoin.get("uuid").alias("categoryUuid"),
                categoryJoin.get("name").alias("categoryName"),
                attributeJoin.get("id").alias("attributeId"),
                attributeJoin.get("uuid").alias("attributeUuid"),
                attributeJoin.get("name").alias("attributeName"),
                attributeJoin.get("code").alias("attributeCode"),
                attributeJoin.get("inputType").alias("attributeInputType"),
                attributeJoin.get("valueType").alias("attributeValueType"),
                attributeJoin.get("allowMultipleValues").alias("attributeAllowMultipleValues"),
                attributeJoin.get("variantCapable").alias("attributeVariantCapable"),
                attributeJoin.get("filterable").alias("attributeFilterable"),
                attributeJoin.get("searchable").alias("attributeSearchable"),
                attributeJoin.get("comparable").alias("attributeComparable"),
                attributeJoin.get("active").alias("attributeActive")
        );
        cq.where(predicate);
        cq.orderBy(cb.asc(root.get("displayOrder")), cb.asc(root.get("id")));

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(this::mapCategoryAttribute)
                .toList();
    }

    private CategoryAttribute findOneByUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return null;
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<CategoryAttribute> root = cq.from(CategoryAttribute.class);
        Join<CategoryAttribute, Productcategory> categoryJoin = root.join("category", JoinType.INNER);
        Join<CategoryAttribute, Attribute> attributeJoin = root.join("attribute", JoinType.INNER);

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("required").alias("required"),
                root.get("variantAttribute").alias("variantAttribute"),
                root.get("displayOrder").alias("displayOrder"),
                root.get("active").alias("active"),
                root.get("attributeGroup").alias("attributeGroup"),
                root.get("helperText").alias("helperText"),
                root.get("version").alias("version"),
                root.get("createdOn").alias("createdOn"),
                root.get("createdBy").alias("createdBy"),
                root.get("updatedOn").alias("updatedOn"),
                root.get("updatedBy").alias("updatedBy"),
                categoryJoin.get("id").alias("categoryId"),
                categoryJoin.get("uuid").alias("categoryUuid"),
                categoryJoin.get("name").alias("categoryName"),
                attributeJoin.get("id").alias("attributeId"),
                attributeJoin.get("uuid").alias("attributeUuid"),
                attributeJoin.get("name").alias("attributeName"),
                attributeJoin.get("code").alias("attributeCode"),
                attributeJoin.get("inputType").alias("attributeInputType"),
                attributeJoin.get("valueType").alias("attributeValueType"),
                attributeJoin.get("allowMultipleValues").alias("attributeAllowMultipleValues"),
                attributeJoin.get("variantCapable").alias("attributeVariantCapable"),
                attributeJoin.get("filterable").alias("attributeFilterable"),
                attributeJoin.get("searchable").alias("attributeSearchable"),
                attributeJoin.get("comparable").alias("attributeComparable"),
                attributeJoin.get("active").alias("attributeActive")
        );
        cq.where(cb.equal(root.get("uuid"), uuid));

        return entityManager.createQuery(cq)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(this::mapCategoryAttribute)
                .orElse(null);
    }

    private CategoryAttribute mapCategoryAttribute(Tuple tuple) {
        CategoryAttribute categoryAttribute = new CategoryAttribute();
        categoryAttribute.setId(tuple.get("id", Long.class));
        categoryAttribute.setUuid(tuple.get("uuid", String.class));
        categoryAttribute.setRequired(tuple.get("required", Boolean.class));
        categoryAttribute.setVariantAttribute(tuple.get("variantAttribute", Boolean.class));
        categoryAttribute.setDisplayOrder(tuple.get("displayOrder", Integer.class));
        categoryAttribute.setActive(tuple.get("active", Boolean.class));
        categoryAttribute.setAttributeGroup(tuple.get("attributeGroup", String.class));
        categoryAttribute.setHelperText(tuple.get("helperText", String.class));
        categoryAttribute.setVersion(tuple.get("version", Long.class));
        categoryAttribute.setCreatedOn(tuple.get("createdOn", Instant.class));
        categoryAttribute.setCreatedBy(tuple.get("createdBy", String.class));
        categoryAttribute.setUpdatedOn(tuple.get("updatedOn", Instant.class));
        categoryAttribute.setUpdatedBy(tuple.get("updatedBy", String.class));

        Productcategory category = new Productcategory();
        category.setId(tuple.get("categoryId", Long.class));
        category.setUuid(tuple.get("categoryUuid", String.class));
        category.setName(tuple.get("categoryName", String.class));
        categoryAttribute.setCategory(category);

        Attribute attribute = new Attribute();
        attribute.setId(tuple.get("attributeId", Long.class));
        attribute.setUuid(tuple.get("attributeUuid", String.class));
        attribute.setName(tuple.get("attributeName", String.class));
        attribute.setCode(tuple.get("attributeCode", String.class));
        attribute.setInputType(tuple.get("attributeInputType", com.ecommerce.app.product.model.AttributeInputType.class));
        attribute.setValueType(tuple.get("attributeValueType", com.ecommerce.app.product.model.AttributeValueType.class));
        attribute.setAllowMultipleValues(Boolean.TRUE.equals(tuple.get("attributeAllowMultipleValues", Boolean.class)));
        attribute.setVariantCapable(Boolean.TRUE.equals(tuple.get("attributeVariantCapable", Boolean.class)));
        attribute.setFilterable(Boolean.TRUE.equals(tuple.get("attributeFilterable", Boolean.class)));
        attribute.setSearchable(Boolean.TRUE.equals(tuple.get("attributeSearchable", Boolean.class)));
        attribute.setComparable(Boolean.TRUE.equals(tuple.get("attributeComparable", Boolean.class)));
        attribute.setActive(Boolean.TRUE.equals(tuple.get("attributeActive", Boolean.class)));
        categoryAttribute.setAttribute(attribute);

        return categoryAttribute;
    }
}
