package com.ecommerce.app.product.services;

import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.product.model.Attribute;
import com.ecommerce.app.product.model.AttributeOption;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductAttribute;
import com.ecommerce.app.product.ripository.ProductAttributeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductAttributeService {

    @Autowired
    private ProductAttributeRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ProductAttribute> findByProductUuid(String productUuid) {
        if (productUuid == null || productUuid.isBlank()) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<ProductAttribute> root = cq.from(ProductAttribute.class);
        Join<ProductAttribute, Product> productJoin = root.join("product", JoinType.INNER);
        Join<ProductAttribute, Attribute> attributeJoin = root.join("attribute", JoinType.INNER);
        Join<ProductAttribute, AttributeOption> optionJoin = root.join("attributeOption", JoinType.LEFT);

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("textValue").alias("textValue"),
                root.get("longTextValue").alias("longTextValue"),
                root.get("integerValue").alias("integerValue"),
                root.get("decimalValue").alias("decimalValue"),
                root.get("booleanValue").alias("booleanValue"),
                root.get("dateValue").alias("dateValue"),
                root.get("displayValue").alias("displayValue"),
                root.get("sortOrder").alias("sortOrder"),
                root.get("version").alias("version"),
                root.get("createdOn").alias("createdOn"),
                root.get("createdBy").alias("createdBy"),
                root.get("updatedOn").alias("updatedOn"),
                root.get("updatedBy").alias("updatedBy"),
                productJoin.get("id").alias("productId"),
                productJoin.get("uuid").alias("productUuid"),
                attributeJoin.get("id").alias("attributeId"),
                attributeJoin.get("uuid").alias("attributeUuid"),
                attributeJoin.get("name").alias("attributeName"),
                attributeJoin.get("code").alias("attributeCode"),
                optionJoin.get("id").alias("optionId"),
                optionJoin.get("uuid").alias("optionUuid"),
                optionJoin.get("label").alias("optionLabel"),
                optionJoin.get("value").alias("optionValue")
        );
        cq.where(cb.equal(productJoin.get("uuid"), productUuid));
        cq.orderBy(cb.asc(attributeJoin.get("name")), cb.asc(root.get("sortOrder")), cb.asc(root.get("id")));

        return entityManager.createQuery(cq)
                .getResultList()
                .stream()
                .map(this::mapProductAttribute)
                .toList();
    }

    @Transactional
    public ProductAttribute save(ProductAttribute productAttribute) {
        if (productAttribute == null) {
            throw new IllegalArgumentException("Product attribute data is required.");
        }
        if (productAttribute.getProduct() == null || productAttribute.getProduct().getId() == null) {
            throw new IllegalArgumentException("A product is required before saving an attribute value.");
        }
        if (productAttribute.getAttribute() == null || productAttribute.getAttribute().getId() == null) {
            throw new IllegalArgumentException("An attribute is required before saving an attribute value.");
        }
        return repository.save(productAttribute);
    }

    @Transactional(readOnly = true)
    public ProductAttribute findByUuid(String uuid) {
        return findByProductUuidQuery(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found."));
    }

    @Transactional
    public void deleteByUuid(String uuid) {
        ProductAttribute productAttribute = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Product attribute value not found."));
        repository.delete(productAttribute);
    }

    private java.util.Optional<ProductAttribute> findByProductUuidQuery(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return java.util.Optional.empty();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<ProductAttribute> root = cq.from(ProductAttribute.class);
        Join<ProductAttribute, Product> productJoin = root.join("product", JoinType.INNER);
        Join<ProductAttribute, Attribute> attributeJoin = root.join("attribute", JoinType.INNER);
        Join<ProductAttribute, AttributeOption> optionJoin = root.join("attributeOption", JoinType.LEFT);

        cq.multiselect(
                root.get("id").alias("id"),
                root.get("uuid").alias("uuid"),
                root.get("textValue").alias("textValue"),
                root.get("longTextValue").alias("longTextValue"),
                root.get("integerValue").alias("integerValue"),
                root.get("decimalValue").alias("decimalValue"),
                root.get("booleanValue").alias("booleanValue"),
                root.get("dateValue").alias("dateValue"),
                root.get("displayValue").alias("displayValue"),
                root.get("sortOrder").alias("sortOrder"),
                root.get("version").alias("version"),
                root.get("createdOn").alias("createdOn"),
                root.get("createdBy").alias("createdBy"),
                root.get("updatedOn").alias("updatedOn"),
                root.get("updatedBy").alias("updatedBy"),
                productJoin.get("id").alias("productId"),
                productJoin.get("uuid").alias("productUuid"),
                attributeJoin.get("id").alias("attributeId"),
                attributeJoin.get("uuid").alias("attributeUuid"),
                attributeJoin.get("name").alias("attributeName"),
                attributeJoin.get("code").alias("attributeCode"),
                optionJoin.get("id").alias("optionId"),
                optionJoin.get("uuid").alias("optionUuid"),
                optionJoin.get("label").alias("optionLabel"),
                optionJoin.get("value").alias("optionValue")
        );
        cq.where(cb.equal(root.get("uuid"), uuid));

        return entityManager.createQuery(cq)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .map(this::mapProductAttribute);
    }

    private ProductAttribute mapProductAttribute(Tuple tuple) {
        ProductAttribute productAttribute = new ProductAttribute();
        productAttribute.setId(tuple.get("id", Long.class));
        productAttribute.setUuid(tuple.get("uuid", String.class));
        productAttribute.setTextValue(tuple.get("textValue", String.class));
        productAttribute.setLongTextValue(tuple.get("longTextValue", String.class));
        productAttribute.setIntegerValue(tuple.get("integerValue", Long.class));
        productAttribute.setDecimalValue(tuple.get("decimalValue", BigDecimal.class));
        productAttribute.setBooleanValue(tuple.get("booleanValue", Boolean.class));
        productAttribute.setDateValue(tuple.get("dateValue", LocalDate.class));
        productAttribute.setDisplayValue(tuple.get("displayValue", String.class));
        productAttribute.setSortOrder(tuple.get("sortOrder", Integer.class));
        productAttribute.setVersion(tuple.get("version", Long.class));
        productAttribute.setCreatedOn(tuple.get("createdOn", Instant.class));
        productAttribute.setCreatedBy(tuple.get("createdBy", String.class));
        productAttribute.setUpdatedOn(tuple.get("updatedOn", Instant.class));
        productAttribute.setUpdatedBy(tuple.get("updatedBy", String.class));

        Product product = new Product();
        product.setId(tuple.get("productId", Long.class));
        product.setUuid(tuple.get("productUuid", String.class));
        productAttribute.setProduct(product);

        Attribute attribute = new Attribute();
        attribute.setId(tuple.get("attributeId", Long.class));
        attribute.setUuid(tuple.get("attributeUuid", String.class));
        attribute.setName(tuple.get("attributeName", String.class));
        attribute.setCode(tuple.get("attributeCode", String.class));
        productAttribute.setAttribute(attribute);

        String optionUuid = tuple.get("optionUuid", String.class);
        if (optionUuid != null) {
            AttributeOption attributeOption = new AttributeOption();
            attributeOption.setId(tuple.get("optionId", Long.class));
            attributeOption.setUuid(optionUuid);
            attributeOption.setLabel(tuple.get("optionLabel", String.class));
            attributeOption.setValue(tuple.get("optionValue", String.class));
            productAttribute.setAttributeOption(attributeOption);
        }

        return productAttribute;
    }
}
