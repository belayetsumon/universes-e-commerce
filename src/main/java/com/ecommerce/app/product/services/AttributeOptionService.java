package com.ecommerce.app.product.services;

import com.ecommerce.app.exception.ForeignKeyConstraintException;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.exception.UniqueConstraintViolationException;
import com.ecommerce.app.product.model.Attribute;
import com.ecommerce.app.product.model.AttributeOption;
import com.ecommerce.app.product.ripository.AttributeOptionRepository;
import com.ecommerce.app.product.ripository.AttributeRepository;
import com.ecommerce.app.product.ripository.ProductAttributeRepository;
import com.ecommerce.app.product.ripository.ProductVariantOptionRepository;
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
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttributeOptionService {

    @Autowired
    private AttributeOptionRepository repository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private ProductAttributeRepository productAttributeRepository;

    @Autowired
    private ProductVariantOptionRepository productVariantOptionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<AttributeOption> findByAttributeUuid(String attributeUuid) {
        return findByAttributeUuid(attributeUuid, false);
    }

    @Transactional(readOnly = true)
    public List<AttributeOption> findActiveByAttributeUuid(String attributeUuid) {
        return findByAttributeUuid(attributeUuid, true);
    }

    @Transactional
    public AttributeOption save(String attributeUuid, AttributeOption attributeOption) {
        if (attributeOption == null) {
            throw new IllegalArgumentException("Attribute option data is required.");
        }
        if (attributeUuid == null || attributeUuid.isBlank()) {
            throw new IllegalArgumentException("The parent attribute is required.");
        }

        Attribute attribute = attributeRepository.findByUuid(attributeUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Parent attribute not found."));

        String normalizedCode = normalizeCode(attributeOption.getCode(), attributeOption.getLabel(), "option label");
        attributeOption.setAttribute(attribute);
        attributeOption.setCode(normalizedCode);

        repository.findByAttribute_IdAndCodeIgnoreCase(attribute.getId(), normalizedCode)
                .filter(existing -> !existing.getUuid().equals(attributeOption.getUuid()))
                .ifPresent(existing -> {
                    throw new UniqueConstraintViolationException("An option with code '" + normalizedCode + "' already exists for this attribute.");
                });

        return repository.save(attributeOption);
    }

    @Transactional(readOnly = true)
    public AttributeOption findByUuid(String uuid) {
        AttributeOption attributeOption = findOneByUuid(uuid);
        if (attributeOption == null) {
            throw new ResourceNotFoundException("Catalog attribute option not found.");
        }
        return attributeOption;
    }

    @Transactional(readOnly = true)
    public boolean isActiveOptionForAttribute(String optionUuid, Long attributeId) {
        if (optionUuid == null || optionUuid.isBlank() || attributeId == null) {
            return false;
        }
        return repository.existsByUuidAndAttribute_IdAndActiveTrue(optionUuid, attributeId);
    }

    @Transactional(readOnly = true)
    public AttributeOption findActiveByUuidAndAttributeId(String optionUuid, Long attributeId) {
        if (optionUuid == null || optionUuid.isBlank() || attributeId == null) {
            throw new ResourceNotFoundException("Catalog attribute option not found.");
        }
        return repository.findByUuidAndAttribute_IdAndActiveTrue(optionUuid, attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog attribute option not found."));
    }

    @Transactional
    public void deleteByUuid(String uuid) {
        AttributeOption attributeOption = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog attribute option not found."));

        if (productAttributeRepository.existsByAttributeOption_Id(attributeOption.getId())) {
            throw new ForeignKeyConstraintException("This attribute option cannot be deleted because it is already used in saved product specifications.");
        }
        if (productVariantOptionRepository.existsByAttributeOption_Id(attributeOption.getId())) {
            throw new ForeignKeyConstraintException("This attribute option cannot be deleted because it is already used in catalog variants.");
        }

        repository.delete(attributeOption);
    }

    private List<AttributeOption> findByAttributeUuid(String attributeUuid, boolean onlyActive) {
        if (attributeUuid == null || attributeUuid.isBlank()) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<AttributeOption> root = cq.from(AttributeOption.class);
        Join<AttributeOption, Attribute> attributeJoin = root.join("attribute", JoinType.INNER);

        Predicate predicate = cb.equal(attributeJoin.get("uuid"), attributeUuid);
        if (onlyActive) {
            predicate = cb.and(predicate, cb.isTrue(root.get("active")));
        }

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("label").alias("label"),
                root.get("code").alias("code"),
                root.get("value").alias("value"),
                root.get("description").alias("description"),
                root.get("sortOrder").alias("sortOrder"),
                root.get("active").alias("active"),
                root.get("version").alias("version"),
                root.get("createdOn").alias("createdOn"),
                root.get("createdBy").alias("createdBy"),
                root.get("updatedOn").alias("updatedOn"),
                root.get("updatedBy").alias("updatedBy"),
                attributeJoin.get("id").alias("attributeId"),
                attributeJoin.get("uuid").alias("attributeUuid"),
                attributeJoin.get("name").alias("attributeName"),
                attributeJoin.get("code").alias("attributeCode")
        );
        cq.where(predicate);
        cq.orderBy(cb.asc(root.get("sortOrder")), cb.asc(root.get("id")));

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(this::mapAttributeOption)
                .toList();
    }

    private AttributeOption findOneByUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return null;
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<AttributeOption> root = cq.from(AttributeOption.class);
        Join<AttributeOption, Attribute> attributeJoin = root.join("attribute", JoinType.INNER);

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("label").alias("label"),
                root.get("code").alias("code"),
                root.get("value").alias("value"),
                root.get("description").alias("description"),
                root.get("sortOrder").alias("sortOrder"),
                root.get("active").alias("active"),
                root.get("version").alias("version"),
                root.get("createdOn").alias("createdOn"),
                root.get("createdBy").alias("createdBy"),
                root.get("updatedOn").alias("updatedOn"),
                root.get("updatedBy").alias("updatedBy"),
                attributeJoin.get("id").alias("attributeId"),
                attributeJoin.get("uuid").alias("attributeUuid"),
                attributeJoin.get("name").alias("attributeName"),
                attributeJoin.get("code").alias("attributeCode")
        );
        cq.where(cb.equal(root.get("uuid"), uuid));

        return entityManager.createQuery(cq)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(this::mapAttributeOption)
                .orElse(null);
    }

    private AttributeOption mapAttributeOption(Tuple tuple) {
        AttributeOption attributeOption = new AttributeOption();
        attributeOption.setId(tuple.get("id", Long.class));
        attributeOption.setUuid(tuple.get("uuid", String.class));
        attributeOption.setLabel(tuple.get("label", String.class));
        attributeOption.setCode(tuple.get("code", String.class));
        attributeOption.setValue(tuple.get("value", String.class));
        attributeOption.setDescription(tuple.get("description", String.class));
        attributeOption.setSortOrder(tuple.get("sortOrder", Integer.class));
        attributeOption.setActive(tuple.get("active", Boolean.class));
        attributeOption.setVersion(tuple.get("version", Long.class));
        attributeOption.setCreatedOn(tuple.get("createdOn", Instant.class));
        attributeOption.setCreatedBy(tuple.get("createdBy", String.class));
        attributeOption.setUpdatedOn(tuple.get("updatedOn", Instant.class));
        attributeOption.setUpdatedBy(tuple.get("updatedBy", String.class));

        Attribute attribute = new Attribute();
        attribute.setId(tuple.get("attributeId", Long.class));
        attribute.setUuid(tuple.get("attributeUuid", String.class));
        attribute.setName(tuple.get("attributeName", String.class));
        attribute.setCode(tuple.get("attributeCode", String.class));
        attributeOption.setAttribute(attribute);

        return attributeOption;
    }

    private String normalizeCode(String code, String fallbackSource, String fallbackLabel) {
        String source = code == null || code.isBlank() ? fallbackSource : code;
        if (source == null) {
            throw new IllegalArgumentException("A valid " + fallbackLabel + " is required to generate the option code.");
        }

        String normalized = source.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Option code must include at least one letter or number.");
        }
        return normalized;
    }
}
