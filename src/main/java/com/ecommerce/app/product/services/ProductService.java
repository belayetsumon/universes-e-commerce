/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.AvailableDeliveryArea;
import com.ecommerce.app.product.model.DeliveryCharge;
import com.ecommerce.app.product.model.DeliveryTimeline;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductAttribute;
import com.ecommerce.app.product.model.ProductDimension;
import com.ecommerce.app.product.model.ProductImage;
import com.ecommerce.app.product.model.ProductStatusEnum;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.Productcategory;
import com.ecommerce.app.product.model.SortingType;
import com.ecommerce.app.product.model.Warranty;
import static com.ecommerce.app.product.model.SortingType.ASC;
import static com.ecommerce.app.product.model.SortingType.DESC;
import static com.ecommerce.app.product.model.SortingType.RANDOM;
import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ProductService {

    @Autowired
    EntityManager em;

    @Autowired
    ProductVariantCatalogService productVariantCatalogService;

    private List<Map<String, Object>> enrichProductAvailability(List<Map<String, Object>> products) {
        if (products == null || products.isEmpty()) {
            return products == null ? List.of() : products;
        }

        Map<Long, Boolean> productIds = new HashMap<>();
        for (Map<String, Object> product : products) {
            Long productId = getLong(product, "id");
            if (productId > 0) {
                productIds.put(productId, Boolean.TRUE);
            }
        }

        if (productIds.isEmpty()) {
            return products;
        }

        Map<Long, BigDecimal> catalogVariantAvailableByProductId = new HashMap<>();
        for (com.ecommerce.app.product.model.ProductVariant variant : productVariantCatalogService.findByProductIds(productIds.keySet())) {
            if (variant == null || variant.getProduct() == null || variant.getProduct().getId() == null) {
                continue;
            }

            Long productId = variant.getProduct().getId();
            BigDecimal stockQuantity = variant.getStockQuantity() == null ? BigDecimal.ZERO : variant.getStockQuantity();
            catalogVariantAvailableByProductId.merge(productId, stockQuantity, BigDecimal::add);
        }

        for (Map<String, Object> product : products) {
            Long productId = getLong(product, "id");
            boolean manageStock = getBoolean(product, "manageStock");
            boolean manageProductVariants = getBoolean(product, "manageProductVariants");
            boolean allowPreorder = getBoolean(product, "allowPreorder");
            BigDecimal variantAvailableStock = catalogVariantAvailableByProductId.getOrDefault(productId, BigDecimal.ZERO);
            BigDecimal productAvailableStock = getBigDecimal(product, "stockAvailableQuantity");

            BigDecimal availableStock = manageProductVariants
                    ? variantAvailableStock
                    : (productAvailableStock.compareTo(BigDecimal.ZERO) > 0 ? productAvailableStock : variantAvailableStock);

            boolean soldOut = manageStock && availableStock.compareTo(BigDecimal.ZERO) <= 0;

            product.put("availableStockQuantity", availableStock);
            product.put("showPreorderBadge", soldOut && allowPreorder);
            product.put("showOutOfStockBadge", soldOut && !allowPreorder);
            product.put("availabilityLabel", soldOut ? (allowPreorder ? "Preorder" : "Out of stock") : "In stock");
        }

        return products;
    }

    private BigDecimal getBigDecimal(Map<String, Object> product, String key) {
        if (product == null || key == null) {
            return BigDecimal.ZERO;
        }

        Object value = product.get(key);
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return new BigDecimal(text.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        return BigDecimal.ZERO;
    }

    private Long getLong(Map<String, Object> product, String key) {
        if (product == null || key == null) {
            return 0L;
        }

        Object value = product.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.valueOf(text.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        return 0L;
    }

    private boolean getBoolean(Map<String, Object> product, String key) {
        if (product == null || key == null) {
            return false;
        }

        Object value = product.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.parseBoolean(text.trim());
        }

        return false;
    }

    public List<Map<String, Object>> product_List_For_Dropdown() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Step 2: Define root entity
        Root<Product> productRoot = cq.from(Product.class);
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("uuid").alias("uuid"),
                productRoot.get("title").alias("productTitle")
        );
        cq.orderBy(cb.desc(productRoot.get("id")));

        List<Tuple> resultTuples = em.createQuery(cq).getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("uuid", tuple.get("uuid"));
//            resultMap.put("userId", tuple.get("productUserId"));
            // resultMap.put("vendorProfile", tuple.get("productVendorProfile"));
//            resultMap.put("category", tuple.get("productCategory"));
            resultMap.put("title", tuple.get("productTitle"));
            resultList.add(resultMap);
        }
        return resultList;
    }

    public List<Map<String, Object>> allProduct() {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Step 2: Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        Expression<String> imageUrl = cb.concat(
                cb.literal(baseUrl + "/files/"),
                productRoot.get("imageName"));

        // Step 3: Select desired fields using Multiselect (Select with alias)
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("uuid").alias("uuid"),
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("vendorprofile").alias("productVendorProfile"),
                productRoot.get("productcategory").alias("productCategory"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                imageUrl.alias("imageUrl"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        cq.orderBy(cb.desc(productRoot.get("id")));

        // Step 5: Execute the query and get results as List<Tuple>
        List<Tuple> resultTuples = em.createQuery(cq).getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("uuid", tuple.get("uuid"));
            resultMap.put("userId", tuple.get("productUserId"));
            // resultMap.put("vendorProfile", tuple.get("productVendorProfile"));
            resultMap.put("category", tuple.get("productCategory"));
            resultMap.put("title", tuple.get("productTitle"));
            resultMap.put("slug", tuple.get("productSlug"));
            resultMap.put("orderno", tuple.get("productOrderno"));
            resultMap.put("shortDescription", tuple.get("productShortDescription"));
            resultMap.put("description", tuple.get("productDescription"));
            resultMap.put("video", tuple.get("productVideo"));
            resultMap.put("price", tuple.get("productPrice"));
            resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
            resultMap.put("salesPrice", tuple.get("productSalesPrice"));
            resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
            resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
            resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
            resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
            resultMap.put("uom", tuple.get("productUOM"));
            resultMap.put("imageUrl", tuple.get("imageUrl"));
            resultMap.put("imageName", tuple.get("productImageName"));
            resultMap.put("newProduct", tuple.get("productNewProduct"));
            resultMap.put("onlineShow", tuple.get("productOnlineShow"));
            resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
            resultMap.put("manageStock", tuple.get("productManageStock"));
            resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
            resultMap.put("status", tuple.get("productStatus"));
            resultMap.put("metaTitle", tuple.get("productMetaTitle"));
            resultMap.put("metaDescription", tuple.get("productMetaDescription"));
            resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
            resultMap.put("createdBy", tuple.get("productCreatedBy"));
            resultMap.put("created", tuple.get("productCreated"));
            resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
            resultMap.put("modified", tuple.get("productModified"));

            resultList.add(resultMap);
        }
        return resultList;
    }

    public List<Map<String, Object>> all_Product_for_admin(
            String keyword,
            Long categoryId,
            Long vendorId,
            ProductStatusEnum status,
            com.ecommerce.app.product.model.ProductTypeEnum productType,
            Boolean onlineShow,
            BigDecimal minSalesPrice,
            BigDecimal maxSalesPrice,
            Boolean featuredProduct,
            Boolean newProduct,
            Boolean manageStock,
            Boolean allowPreorder,
            Boolean manageProductVariants,
            LocalDate createdFrom,
            LocalDate createdTo,
            Long uomId,
            BigDecimal minPurchasePrice,
            BigDecimal maxPurchasePrice,
            BigDecimal minMarketPlaceDiscount,
            BigDecimal maxMarketPlaceDiscount,
            BigDecimal minVendorDiscount,
            BigDecimal maxVendorDiscount,
            LocalDate discountStartFrom,
            LocalDate discountStartTo,
            LocalDate discountEndFrom,
            LocalDate discountEndTo,
            LocalDate preorderFrom,
            LocalDate preorderTo,
            Boolean hasSpecifications,
            Boolean hasImage,
            Boolean hasCatalogVariants,
            Boolean hasDimensions,
            Boolean hasDeliveryAreas,
            Boolean hasDeliveryCharges,
            Boolean hasDeliveryTimelines,
            Boolean hasWarranty
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        Root<Product> productRoot = cq.from(Product.class);
        Join<Product, Vendorprofile> vendorJoin = productRoot.join("vendorprofile", JoinType.LEFT);

        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").get("id").alias("productUserId"),
                vendorJoin.get("companyName").alias("productVendorProfile"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        List<Predicate> predicates = new ArrayList<>();
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && !normalizedKeyword.isBlank()) {
            String likePattern = "%" + normalizedKeyword.toLowerCase(Locale.ROOT) + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(productRoot.get("title")), likePattern),
                    cb.like(cb.lower(productRoot.get("slug")), likePattern),
                    cb.like(productRoot.get("sku").as(String.class), "%" + normalizedKeyword + "%")
            ));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(productRoot.get("productcategory").get("id"), categoryId));
        }
        if (vendorId != null) {
            predicates.add(cb.equal(vendorJoin.get("id"), vendorId));
        }
        if (status != null) {
            predicates.add(cb.equal(productRoot.get("status"), status));
        }
        if (productType != null) {
            predicates.add(cb.equal(productRoot.get("productType"), productType));
        }
        if (onlineShow != null) {
            predicates.add(cb.equal(productRoot.get("onlineShow"), onlineShow));
        }
        if (featuredProduct != null) {
            predicates.add(cb.equal(productRoot.get("featuredProduct"), featuredProduct));
        }
        if (newProduct != null) {
            predicates.add(cb.equal(productRoot.get("newProduct"), newProduct));
        }
        if (manageStock != null) {
            predicates.add(cb.equal(productRoot.get("manageStock"), manageStock));
        }
        if (allowPreorder != null) {
            predicates.add(cb.equal(productRoot.get("allowPreorder"), allowPreorder));
        }
        if (manageProductVariants != null) {
            predicates.add(cb.equal(productRoot.get("manageProductVariants"), manageProductVariants));
        }
        if (uomId != null) {
            predicates.add(cb.equal(productRoot.get("uom").get("id"), uomId));
        }

        addBigDecimalRangePredicates(predicates, cb, productRoot.get("salesPrice"), minSalesPrice, maxSalesPrice);
        addBigDecimalRangePredicates(predicates, cb, productRoot.get("purchasePrice"), minPurchasePrice, maxPurchasePrice);
        addBigDecimalRangePredicates(predicates, cb, productRoot.get("marketPlaceDiscount"), minMarketPlaceDiscount, maxMarketPlaceDiscount);
        addBigDecimalRangePredicates(predicates, cb, productRoot.get("vendordiscount"), minVendorDiscount, maxVendorDiscount);
        addLocalDateRangePredicates(predicates, cb, productRoot.get("discountStartDate"), discountStartFrom, discountStartTo);
        addLocalDateRangePredicates(predicates, cb, productRoot.get("discountEndDate"), discountEndFrom, discountEndTo);
        addLocalDateRangePredicates(predicates, cb, productRoot.get("preorderAvailableFrom"), preorderFrom, preorderTo);
        addCreatedDateRangePredicates(predicates, cb, productRoot.get("created"), createdFrom, createdTo);
        addStringPresencePredicate(predicates, cb, productRoot.get("imageName"), hasImage);
        addRelatedEntityPresencePredicate(predicates, cb, cq, productRoot, hasSpecifications, ProductAttribute.class);
        addRelatedEntityPresencePredicate(predicates, cb, cq, productRoot, hasCatalogVariants, ProductVariant.class);
        addRelatedEntityPresencePredicate(predicates, cb, cq, productRoot, hasDimensions, ProductDimension.class);
        addRelatedEntityPresencePredicate(predicates, cb, cq, productRoot, hasDeliveryAreas, AvailableDeliveryArea.class);
        addRelatedEntityPresencePredicate(predicates, cb, cq, productRoot, hasDeliveryCharges, DeliveryCharge.class);
        addRelatedEntityPresencePredicate(predicates, cb, cq, productRoot, hasDeliveryTimelines, DeliveryTimeline.class);
        addRelatedEntityPresencePredicate(predicates, cb, cq, productRoot, hasWarranty, Warranty.class);

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(Predicate[]::new)));
        }
        cq.orderBy(cb.desc(productRoot.get("id")));

        List<Tuple> resultTuples = em.createQuery(cq).getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>(resultTuples.size());

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("userId", tuple.get("productUserId"));
            resultMap.put("vendorProfile", tuple.get("productVendorProfile"));
            resultMap.put("category", tuple.get("productCategory"));
            resultMap.put("title", tuple.get("productTitle"));
            resultMap.put("slug", tuple.get("productSlug"));
            resultMap.put("orderno", tuple.get("productOrderno"));
            resultMap.put("shortDescription", tuple.get("productShortDescription"));
            resultMap.put("description", tuple.get("productDescription"));
            resultMap.put("video", tuple.get("productVideo"));
            resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
            resultMap.put("salesPrice", tuple.get("productSalesPrice"));
            resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
            resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
            resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
            resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
            resultMap.put("uom", tuple.get("productUOM"));
            resultMap.put("imageName", tuple.get("productImageName"));
            resultMap.put("newProduct", tuple.get("productNewProduct"));
            resultMap.put("onlineShow", tuple.get("productOnlineShow"));
            resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
            resultMap.put("manageStock", tuple.get("productManageStock"));
            resultMap.put("allowPreorder", tuple.get("productAllowPreorder"));
            resultMap.put("preorderAvailableFrom", tuple.get("productPreorderAvailableFrom"));
            resultMap.put("manageProductVariants", tuple.get("productManageProductVariants"));
            resultMap.put("stockAvailableQuantity", tuple.get("productStockAvailableQuantity"));
            resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
            resultMap.put("status", tuple.get("productStatus"));
            resultMap.put("metaTitle", tuple.get("productMetaTitle"));
            resultMap.put("metaDescription", tuple.get("productMetaDescription"));
            resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
            resultMap.put("createdBy", tuple.get("productCreatedBy"));
            resultMap.put("created", tuple.get("productCreated"));
            resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
            resultMap.put("modified", tuple.get("productModified"));
            resultList.add(resultMap);
        }
        return resultList;
    }

    public Map<String, Object> all_Product_for_admin_By_Id(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        // Select desired fields using Multiselect
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("uuid").alias("productUuid"),
                productRoot.get("userId").get("id").alias("productUserId"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                productRoot.get("productcategory").get("uuid").alias("productCategoryUuid"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("vatRate").alias("vatRate"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        // Apply where condition
        cq.where(cb.equal(productRoot.get("id"), id));

        // Order the results
        cq.orderBy(cb.desc(productRoot.get("id")));

        // Execute the query and get results as List<Tuple>
        List<Tuple> resultTuples = em.createQuery(cq).getResultList();

        if (resultTuples.isEmpty()) {
            return new HashMap<>(); // If no products found, return an empty map
        }

        // Get the first result (or any specific result you want)
        Tuple tuple = resultTuples.get(0);

        // Map to hold the single product data
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", tuple.get("productId"));
        resultMap.put("sku", tuple.get("productSku"));
        resultMap.put("uuid", tuple.get("productUuid"));
        resultMap.put("userId", tuple.get("productUserId"));
        resultMap.put("category", tuple.get("productCategory"));
        resultMap.put("categoryUuid", tuple.get("productCategoryUuid"));
        resultMap.put("title", tuple.get("productTitle"));
        resultMap.put("slug", tuple.get("productSlug"));
        resultMap.put("orderno", tuple.get("productOrderno"));
        resultMap.put("shortDescription", tuple.get("productShortDescription"));
        resultMap.put("description", tuple.get("productDescription"));
        resultMap.put("video", tuple.get("productVideo"));
        resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
        resultMap.put("salesPrice", tuple.get("productSalesPrice"));
        resultMap.put("vatRate", tuple.get("vatRate"));
        resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
        resultMap.put("productType", tuple.get("productType"));
        resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
        resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
        resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
        resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
        resultMap.put("uom", tuple.get("productUOM"));
        resultMap.put("imageName", tuple.get("productImageName"));
        resultMap.put("newProduct", tuple.get("productNewProduct"));
        resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
        resultMap.put("onlineShow", tuple.get("productOnlineShow"));
        resultMap.put("manageStock", tuple.get("productManageStock"));
        resultMap.put("allowPreorder", tuple.get("productAllowPreorder"));
        resultMap.put("preorderAvailableFrom", tuple.get("productPreorderAvailableFrom"));
        resultMap.put("manageProductVariants", tuple.get("productManageProductVariants"));
        resultMap.put("stockAvailableQuantity", tuple.get("productStockAvailableQuantity"));
        resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
        resultMap.put("status", tuple.get("productStatus"));
        resultMap.put("metaTitle", tuple.get("productMetaTitle"));
        resultMap.put("metaDescription", tuple.get("productMetaDescription"));
        resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
        resultMap.put("createdBy", tuple.get("productCreatedBy"));
        resultMap.put("created", tuple.get("productCreated"));
        resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
        resultMap.put("modified", tuple.get("productModified"));

        return resultMap; // Return the single map with product details
    }

    private void addBigDecimalRangePredicates(List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<BigDecimal> path,
            BigDecimal minValue,
            BigDecimal maxValue) {
        if (path == null) {
            return;
        }
        if (minValue != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, minValue));
        }
        if (maxValue != null) {
            predicates.add(cb.lessThanOrEqualTo(path, maxValue));
        }
    }

    private void addLocalDateRangePredicates(List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<LocalDate> path,
            LocalDate fromDate,
            LocalDate toDate) {
        if (path == null) {
            return;
        }
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, fromDate));
        }
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(path, toDate));
        }
    }

    private void addCreatedDateRangePredicates(List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<LocalDateTime> path,
            LocalDate fromDate,
            LocalDate toDate) {
        if (path == null) {
            return;
        }
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            predicates.add(cb.lessThan(path, toDate.plusDays(1).atStartOfDay()));
        }
    }

    private void addStringPresencePredicate(List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<String> path,
            Boolean hasValue) {
        if (path == null || hasValue == null) {
            return;
        }

        Predicate presentPredicate = cb.and(
                cb.isNotNull(path),
                cb.notEqual(path, "")
        );
        predicates.add(Boolean.TRUE.equals(hasValue) ? presentPredicate : cb.not(presentPredicate));
    }

    private <T> void addRelatedEntityPresencePredicate(List<Predicate> predicates,
            CriteriaBuilder cb,
            CriteriaQuery<?> cq,
            Root<Product> productRoot,
            Boolean hasRecords,
            Class<T> entityClass) {
        if (hasRecords == null || entityClass == null) {
            return;
        }

        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<T> relatedRoot = subquery.from(entityClass);
        subquery.select(cb.literal(1L));
        subquery.where(cb.equal(relatedRoot.get("product").get("id"), productRoot.get("id")));

        Predicate existsPredicate = cb.exists(subquery);
        predicates.add(Boolean.TRUE.equals(hasRecords) ? existsPredicate : cb.not(existsPredicate));
    }

    public List<Map<String, Object>> all_Product_for_admin_By_Vendor_Id(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        // Select desired fields using Multiselect
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("uuid").alias("productUuid"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                productRoot.get("vendorprofile").get("id").alias("vendorprofileId"),
                productRoot.get("vendorprofile").get("companyName").alias("vendorprName"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        // Apply where condition
        cq.where(cb.equal(productRoot.get("vendorprofile").get("id"), id));

        // Order the results
        cq.orderBy(cb.desc(productRoot.get("id")));

        // Execute the query and get results as List<Tuple>
        List<Tuple> resultTuples = em.createQuery(cq).getResultList();

        // Step 5: Execute the query and get results as List<Tuple>
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("uuid", tuple.get("productUuid"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("userId", tuple.get("productUserId"));
//            resultMap.put("vendorProfile", tuple.get("productVendorProfile"));
            resultMap.put("category", tuple.get("productCategory"));
            resultMap.put("title", tuple.get("productTitle"));
            resultMap.put("slug", tuple.get("productSlug"));
            resultMap.put("orderno", tuple.get("productOrderno"));
            resultMap.put("shortDescription", tuple.get("productShortDescription"));
            resultMap.put("description", tuple.get("productDescription"));
            resultMap.put("video", tuple.get("productVideo"));
            resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
            resultMap.put("salesPrice", tuple.get("productSalesPrice"));
            resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
            resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
            resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
            resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
            resultMap.put("uom", tuple.get("productUOM"));
            resultMap.put("imageName", tuple.get("productImageName"));
            resultMap.put("newProduct", tuple.get("productNewProduct"));
            resultMap.put("onlineShow", tuple.get("productOnlineShow"));
            resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
            resultMap.put("manageStock", tuple.get("productManageStock"));
            resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
            resultMap.put("status", tuple.get("productStatus"));
            resultMap.put("metaTitle", tuple.get("productMetaTitle"));
            resultMap.put("metaDescription", tuple.get("productMetaDescription"));
            resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
            resultMap.put("createdBy", tuple.get("productCreatedBy"));
            resultMap.put("created", tuple.get("productCreated"));
            resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
            resultMap.put("modified", tuple.get("productModified"));
            resultList.add(resultMap);
        }
        return resultList;
    }

    public List<Map<String, Object>> vendor_random_product_by_category(
            Long vendorId,
            Long categoryId,
            SortingType sortingType,
            Integer limit
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        Root<Product> productRoot = cq.from(Product.class);

        // Base URL
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        Expression<String> imageUrl = cb.concat(
                cb.literal(baseUrl + "/files/"),
                productRoot.get("imageName")
        );

        // Fields
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("uuid").alias("productUuid"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                productRoot.get("vendorprofile").get("id").alias("vendorprofileId"),
                productRoot.get("vendorprofile").get("companyName").alias("vendorprName"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                imageUrl.alias("imageUrl"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        // WHERE vendor + category
        cq.where(
                cb.and(
                        cb.equal(productRoot.get("vendorprofile").get("id"), vendorId),
                        cb.equal(productRoot.get("productcategory").get("id"), categoryId)
                )
        );

        // ORDER BY
        // ORDER BY
        if (sortingType != null) {

            switch (sortingType) {

                case ASC:
                    cq.orderBy(cb.asc(productRoot.get("id")));
                    break;

                case DESC:
                    cq.orderBy(cb.desc(productRoot.get("id")));
                    break;

                case RANDOM:
                    cq.orderBy(cb.asc(cb.function("RAND", Double.class)));
                    break;
            }

        } else {
            // sortingType == null → default id DESC
            cq.orderBy(cb.desc(productRoot.get("id")));
        }

        // Execute query
        TypedQuery<Tuple> query = em.createQuery(cq);

        // LIMIT
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }

        List<Tuple> resultTuples = query.getResultList();

        // Mapping result
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("uuid", tuple.get("productUuid"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("userId", tuple.get("productUserId"));
//            resultMap.put("vendorProfile", tuple.get("productVendorProfile"));
            resultMap.put("category", tuple.get("productCategory"));
            resultMap.put("title", tuple.get("productTitle"));
            resultMap.put("slug", tuple.get("productSlug"));
            resultMap.put("orderno", tuple.get("productOrderno"));
            resultMap.put("shortDescription", tuple.get("productShortDescription"));
            resultMap.put("description", tuple.get("productDescription"));
            resultMap.put("video", tuple.get("productVideo"));

            BigDecimal vendorDiscount = tuple.get("productVendorDiscount", BigDecimal.class);
            BigDecimal marketPlaceDiscount = tuple.get("marketPlaceDiscount", BigDecimal.class);
            BigDecimal salesPrice = tuple.get("productSalesPrice", BigDecimal.class);

            BigDecimal totalDiscountPercent = totalDiscountPercentCalculate(vendorDiscount, marketPlaceDiscount);
            BigDecimal totalDiscountAmount = totalDiscountCalculate(salesPrice, totalDiscountPercent);
            BigDecimal afterDiscountAmount = finalNetPrice(salesPrice, totalDiscountPercent);

            resultMap.put("totalDiscountPercent", totalDiscountPercent);
            resultMap.put("totalDiscountedAmount", totalDiscountAmount);
            resultMap.put("afterDiscountRemainingAmount", afterDiscountAmount);

            resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
            resultMap.put("salesPrice", tuple.get("productSalesPrice"));
            resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
            resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
            resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
            resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
            resultMap.put("uom", tuple.get("productUOM"));
            resultMap.put("imageUrl", tuple.get("imageUrl"));
            resultMap.put("imageName", tuple.get("productImageName"));
            resultMap.put("newProduct", tuple.get("productNewProduct"));
            resultMap.put("onlineShow", tuple.get("productOnlineShow"));
            resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
            resultMap.put("manageStock", tuple.get("productManageStock"));
            resultMap.put("allowPreorder", tuple.get("productAllowPreorder"));
            resultMap.put("preorderAvailableFrom", tuple.get("productPreorderAvailableFrom"));
            resultMap.put("manageProductVariants", tuple.get("productManageProductVariants"));
            resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
            resultMap.put("stockAvailableQuantity", tuple.get("productStockAvailableQuantity"));
            resultMap.put("status", tuple.get("productStatus"));
            resultMap.put("metaTitle", tuple.get("productMetaTitle"));
            resultMap.put("metaDescription", tuple.get("productMetaDescription"));
            resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
            resultMap.put("createdBy", tuple.get("productCreatedBy"));
            resultMap.put("created", tuple.get("productCreated"));
            resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
            resultMap.put("modified", tuple.get("productModified"));
            resultList.add(resultMap);
        }

        return enrichProductAvailability(resultList);
    }

    public List<Map<String, Object>> product_By_Vendor(
            Long vendorId,
            SortingType sortingType,
            Integer limit
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        Root<Product> productRoot = cq.from(Product.class);

        // Base URL
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        Expression<String> imageUrl = cb.concat(
                cb.literal(baseUrl + "/files/"),
                productRoot.get("imageName")
        );

        // Fields
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("uuid").alias("productUuid"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                productRoot.get("vendorprofile").get("id").alias("vendorprofileId"),
                productRoot.get("vendorprofile").get("companyName").alias("vendorprName"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                imageUrl.alias("imageUrl"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        // WHERE vendor + category
        cq.where(
                cb.equal(productRoot.get("vendorprofile").get("id"), vendorId)
        );

        // ORDER BY
        // ORDER BY
        if (sortingType != null) {

            switch (sortingType) {

                case ASC:
                    cq.orderBy(cb.asc(productRoot.get("id")));
                    break;

                case DESC:
                    cq.orderBy(cb.desc(productRoot.get("id")));
                    break;

                case RANDOM:
                    cq.orderBy(cb.asc(cb.function("RAND", Double.class)));
                    break;
            }

        } else {
            // sortingType == null → default id DESC
            cq.orderBy(cb.desc(productRoot.get("id")));
        }

        // Execute query
        TypedQuery<Tuple> query = em.createQuery(cq);

        // LIMIT
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }

        List<Tuple> resultTuples = query.getResultList();

        // Mapping result
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("uuid", tuple.get("productUuid"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("userId", tuple.get("productUserId"));
//            resultMap.put("vendorProfile", tuple.get("productVendorProfile"));
            resultMap.put("category", tuple.get("productCategory"));
            resultMap.put("title", tuple.get("productTitle"));
            resultMap.put("slug", tuple.get("productSlug"));
            resultMap.put("orderno", tuple.get("productOrderno"));
            resultMap.put("shortDescription", tuple.get("productShortDescription"));
            resultMap.put("description", tuple.get("productDescription"));
            resultMap.put("video", tuple.get("productVideo"));

            BigDecimal vendorDiscount = tuple.get("productVendorDiscount", BigDecimal.class);
            BigDecimal marketPlaceDiscount = tuple.get("marketPlaceDiscount", BigDecimal.class);
            BigDecimal salesPrice = tuple.get("productSalesPrice", BigDecimal.class);

            BigDecimal totalDiscountPercent = totalDiscountPercentCalculate(vendorDiscount, marketPlaceDiscount);
            BigDecimal totalDiscountAmount = totalDiscountCalculate(salesPrice, totalDiscountPercent);
            BigDecimal afterDiscountAmount = finalNetPrice(salesPrice, totalDiscountPercent);

            resultMap.put("totalDiscountPercent", totalDiscountPercent);
            resultMap.put("totalDiscountedAmount", totalDiscountAmount);
            resultMap.put("afterDiscountRemainingAmount", afterDiscountAmount);

            resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
            resultMap.put("salesPrice", tuple.get("productSalesPrice"));
            resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
            resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
            resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
            resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
            resultMap.put("uom", tuple.get("productUOM"));
            resultMap.put("imageUrl", tuple.get("imageUrl"));
            resultMap.put("imageName", tuple.get("productImageName"));
            resultMap.put("newProduct", tuple.get("productNewProduct"));
            resultMap.put("onlineShow", tuple.get("productOnlineShow"));
            resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
            resultMap.put("manageStock", tuple.get("productManageStock"));
            resultMap.put("allowPreorder", tuple.get("productAllowPreorder"));
            resultMap.put("preorderAvailableFrom", tuple.get("productPreorderAvailableFrom"));
            resultMap.put("manageProductVariants", tuple.get("productManageProductVariants"));
            resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
            resultMap.put("stockAvailableQuantity", tuple.get("productStockAvailableQuantity"));
            resultMap.put("status", tuple.get("productStatus"));
            resultMap.put("metaTitle", tuple.get("productMetaTitle"));
            resultMap.put("metaDescription", tuple.get("productMetaDescription"));
            resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
            resultMap.put("createdBy", tuple.get("productCreatedBy"));
            resultMap.put("created", tuple.get("productCreated"));
            resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
            resultMap.put("modified", tuple.get("productModified"));
            resultList.add(resultMap);
        }

        return enrichProductAvailability(resultList);
    }

    public List<Map<String, Object>> all_random_product(
            SortingType sortingType,
            Integer limit
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        Root<Product> productRoot = cq.from(Product.class);

        // Base URL
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        Expression<String> imageUrl = cb.concat(
                cb.literal(baseUrl + "/files/"),
                productRoot.get("imageName")
        );

        // Fields
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("uuid").alias("productUuid"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                productRoot.get("vendorprofile").get("id").alias("vendorprofileId"),
                productRoot.get("vendorprofile").get("companyName").alias("vendorprName"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                imageUrl.alias("imageUrl"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        // WHERE vendor + category
//        cq.where(
//                cb.equal(productRoot.get("vendorprofile").get("id"), vendorId)
//        );
        // ORDER BY
        // ORDER BY
        if (sortingType != null) {

            switch (sortingType) {

                case ASC:
                    cq.orderBy(cb.asc(productRoot.get("id")));
                    break;

                case DESC:
                    cq.orderBy(cb.desc(productRoot.get("id")));
                    break;

                case RANDOM:
                    cq.orderBy(cb.asc(cb.function("RAND", Double.class)));
                    break;
            }

        } else {
            // sortingType == null → default id DESC
            cq.orderBy(cb.desc(productRoot.get("id")));
        }

        // Execute query
        TypedQuery<Tuple> query = em.createQuery(cq);

        // LIMIT
        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }

        List<Tuple> resultTuples = query.getResultList();

        // Mapping result
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("uuid", tuple.get("productUuid"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("userId", tuple.get("productUserId"));
//            resultMap.put("vendorProfile", tuple.get("productVendorProfile"));
            resultMap.put("category", tuple.get("productCategory"));
            resultMap.put("title", tuple.get("productTitle"));
            resultMap.put("slug", tuple.get("productSlug"));
            resultMap.put("orderno", tuple.get("productOrderno"));
            resultMap.put("shortDescription", tuple.get("productShortDescription"));
            resultMap.put("description", tuple.get("productDescription"));
            resultMap.put("video", tuple.get("productVideo"));

            BigDecimal vendorDiscount = tuple.get("productVendorDiscount", BigDecimal.class);
            BigDecimal marketPlaceDiscount = tuple.get("marketPlaceDiscount", BigDecimal.class);
            BigDecimal salesPrice = tuple.get("productSalesPrice", BigDecimal.class);

            BigDecimal totalDiscountPercent = totalDiscountPercentCalculate(vendorDiscount, marketPlaceDiscount);
            BigDecimal totalDiscountAmount = totalDiscountCalculate(salesPrice, totalDiscountPercent);
            BigDecimal afterDiscountAmount = finalNetPrice(salesPrice, totalDiscountPercent);

            resultMap.put("totalDiscountPercent", totalDiscountPercent);
            resultMap.put("totalDiscountedAmount", totalDiscountAmount);
            resultMap.put("afterDiscountRemainingAmount", afterDiscountAmount);

            resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
            resultMap.put("salesPrice", tuple.get("productSalesPrice"));
            resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
            resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
            resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
            resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
            resultMap.put("uom", tuple.get("productUOM"));
            resultMap.put("imageUrl", tuple.get("imageUrl"));
            resultMap.put("imageName", tuple.get("productImageName"));
            resultMap.put("newProduct", tuple.get("productNewProduct"));
            resultMap.put("onlineShow", tuple.get("productOnlineShow"));
            resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
            resultMap.put("manageStock", tuple.get("productManageStock"));
            resultMap.put("allowPreorder", tuple.get("productAllowPreorder"));
            resultMap.put("preorderAvailableFrom", tuple.get("productPreorderAvailableFrom"));
            resultMap.put("manageProductVariants", tuple.get("productManageProductVariants"));
            resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
            resultMap.put("stockAvailableQuantity", tuple.get("productStockAvailableQuantity"));
            resultMap.put("status", tuple.get("productStatus"));
            resultMap.put("metaTitle", tuple.get("productMetaTitle"));
            resultMap.put("metaDescription", tuple.get("productMetaDescription"));
            resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
            resultMap.put("createdBy", tuple.get("productCreatedBy"));
            resultMap.put("created", tuple.get("productCreated"));
            resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
            resultMap.put("modified", tuple.get("productModified"));
            resultList.add(resultMap);
        }

        return enrichProductAvailability(resultList);
    }

    public Map<String, Object> product_details_for_front_view_single_product_page_by_Uuid(String productUuid) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        Join<Product, Vendorprofile> vendorJoin = productRoot.join("vendorprofile", JoinType.LEFT);
        // Select desired fields using multiselect
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("uuid").alias("productUuid"),
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                productRoot.get("productcategory").get("id").alias("productCategoryId"),
                productRoot.get("productcategory").get("uuid").alias("productCategoryUuid"),
                vendorJoin.get("id").alias("vendorProfileId"),
                vendorJoin.get("uuid").alias("vendorProfileUuid"),
                vendorJoin.get("companyName").alias("vendorProfileName"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("manufacturer").get("id").alias("manufacturerId"),
                productRoot.get("manufacturer").get("name").alias("manufacturerName"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderNo"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("vatRate").alias("vatRate"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        // Apply where condition
        cq.where(cb.equal(productRoot.get("uuid"), productUuid));

        // Execute query safely
        Tuple tuple = null;
        try {
            tuple = em.createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            return Collections.emptyMap(); // Return empty map if product not found
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", tuple.get("productId"));
        resultMap.put("sku", tuple.get("productSku"));
        resultMap.put("uuid", tuple.get("productUuid"));
        resultMap.put("userId", tuple.get("productUserId"));
        resultMap.put("vendorProfileId", tuple.get("vendorProfileId"));
        resultMap.put("vendorProfileUuid", tuple.get("vendorProfileUuid"));
        resultMap.put("vendorProfileName", tuple.get("vendorProfileName"));
        resultMap.put("category", tuple.get("productCategory"));
        resultMap.put("categoryId", tuple.get("productCategoryId"));
        resultMap.put("categoryUuid", tuple.get("productCategoryUuid"));
        resultMap.put("title", tuple.get("productTitle", String.class));
        resultMap.put("manufacturerName", tuple.get("manufacturerName", String.class));
        resultMap.put("slug", tuple.get("productSlug"));
        resultMap.put("orderno", tuple.get("productOrderNo"));
        resultMap.put("shortDescription", tuple.get("productShortDescription"));
        resultMap.put("description", tuple.get("productDescription"));
        resultMap.put("video", tuple.get("productVideo"));
        resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
        resultMap.put("salesPrice", tuple.get("productSalesPrice"));
        resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
        resultMap.put("productType", tuple.get("productType"));
        resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
        resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
        BigDecimal vendorDiscount = tuple.get("productVendorDiscount", BigDecimal.class);
        BigDecimal marketPlaceDiscount = tuple.get("marketPlaceDiscount", BigDecimal.class);
        BigDecimal salesPrice = tuple.get("productSalesPrice", BigDecimal.class);

        BigDecimal totalDiscountPercent = totalDiscountPercentCalculate(vendorDiscount, marketPlaceDiscount);
        BigDecimal totalDiscountAmount = totalDiscountCalculate(salesPrice, totalDiscountPercent);
        BigDecimal afterDiscountAmount = finalNetPrice(salesPrice, totalDiscountPercent);

        resultMap.put("totalDiscountPercent", totalDiscountPercent);
        resultMap.put("totalDiscountedAmount", totalDiscountAmount);
        resultMap.put("afterDiscountRemainingAmount", afterDiscountAmount);

        BigDecimal vatRate = tuple.get("vatRate", BigDecimal.class);

        resultMap.put("vatRate", vatRate);
        resultMap.put("uom", tuple.get("productUOM"));
        resultMap.put("imageName", tuple.get("productImageName", String.class));
        resultMap.put("newProduct", tuple.get("productNewProduct"));
        resultMap.put("onlineShow", tuple.get("productOnlineShow"));
        resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
        resultMap.put("manageStock", tuple.get("productManageStock"));
        resultMap.put("allowPreorder", tuple.get("productAllowPreorder"));
        resultMap.put("preorderAvailableFrom", tuple.get("productPreorderAvailableFrom"));
        resultMap.put("manageProductVariants", tuple.get("productManageProductVariants"));
        resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
        resultMap.put("stockAvailableQuantity", tuple.get("productStockAvailableQuantity"));
        resultMap.put("status", tuple.get("productStatus"));
        resultMap.put("metaTitle", tuple.get("productMetaTitle"));
        resultMap.put("metaDescription", tuple.get("productMetaDescription"));
        resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
        resultMap.put("createdBy", tuple.get("productCreatedBy"));
        resultMap.put("created", tuple.get("productCreated"));
        resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
        resultMap.put("modified", tuple.get("productModified"));

        return resultMap;
    }

    public List<Map<String, Object>> all_Product_front_view(
            Long vendorprofileId,
            Long productcategory,
            LocalDate discountEndDate,
            Boolean newProduct,
            Boolean featuredProduct,
            Boolean emiavailable
    ) {
        return all_Product_front_view(
                vendorprofileId,
                productcategory,
                discountEndDate,
                newProduct,
                featuredProduct,
                emiavailable,
                null,
                null
        );
    }

    public List<Map<String, Object>> all_Product_front_view(
            Long vendorprofileId,
            Long productcategory,
            LocalDate discountEndDate,
            Boolean newProduct,
            Boolean featuredProduct,
            Boolean emiavailable,
            Boolean discountedOnly,
            Integer maxResults
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Step 2: Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        Join<Product, Vendorprofile> vendorJoin = productRoot.join("vendorprofile", JoinType.LEFT);

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        Expression<String> imageUrl = cb.concat(
                cb.literal(baseUrl + "/files/"),
                productRoot.get("imageName"));

        // Step 3: Select desired fields using Multiselect (Select with alias)
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("uuid").alias("productUuid"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                vendorJoin.get("id").alias("vendorProfileId"),
                vendorJoin.get("companyName").alias("vendorProfileName"),
                productRoot.get("productcategory").alias("productCategory"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                imageUrl.alias("imageUrl"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        List<Predicate> predicates = new ArrayList<>();

        // 2026-04-29: Public product listings should only load active products
        // that are marked for online display.
        predicates.add(cb.equal(productRoot.get("status"), ProductStatusEnum.Active));
        predicates.add(cb.isTrue(productRoot.get("onlineShow")));

        // Filter by Vendor Profile ID
        if (vendorprofileId != null) {
            predicates.add(cb.equal(productRoot.get("vendorprofile").get("id"), vendorprofileId));
        }

        // Filter by Product Category
        if (productcategory != null) {
            predicates.add(cb.equal(productRoot.get("productcategory").get("id"), productcategory));
        }

        // Compare Discount End Date with Current Date
//        predicates.add(cb.greaterThanOrEqualTo(productRoot.get("discountEndDate"), LocalDate.now()));
        // Filter by New Product
        if (newProduct != null) {
            predicates.add(cb.equal(productRoot.get("newProduct"), newProduct));
        }

        // Filter by Featured Product
        if (featuredProduct != null) {
            predicates.add(cb.equal(productRoot.get("featuredProduct"), featuredProduct));
        }

        // Filter by EMI Availability
        if (emiavailable != null) {
            predicates.add(cb.equal(productRoot.get("emiavailable"), emiavailable));
        }

        if (Boolean.TRUE.equals(discountedOnly)) {
            Expression<BigDecimal> marketPlaceDiscountExpr = cb.coalesce(productRoot.get("marketPlaceDiscount"), BigDecimal.ZERO);
            Expression<BigDecimal> vendorDiscountExpr = cb.coalesce(productRoot.get("vendordiscount"), BigDecimal.ZERO);
            predicates.add(cb.or(
                    cb.greaterThan(marketPlaceDiscountExpr, BigDecimal.ZERO),
                    cb.greaterThan(vendorDiscountExpr, BigDecimal.ZERO)
            ));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(productRoot.get("id")));

        // Step 5: Execute the query and get results as List<Tuple>
        TypedQuery<Tuple> query = em.createQuery(cq);
        if (maxResults != null && maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        List<Tuple> resultTuples = query.getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("uuid", tuple.get("productUuid"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("userId", tuple.get("productUserId"));
            resultMap.put("vendorProfile", tuple.get("vendorProfileName"));
            resultMap.put("category", tuple.get("productCategory"));
            resultMap.put("title", tuple.get("productTitle"));
            resultMap.put("slug", tuple.get("productSlug"));
            resultMap.put("orderno", tuple.get("productOrderno"));
            resultMap.put("shortDescription", tuple.get("productShortDescription"));
            resultMap.put("description", tuple.get("productDescription"));
            resultMap.put("video", tuple.get("productVideo"));

            BigDecimal vendorDiscount = tuple.get("productVendorDiscount", BigDecimal.class);
            BigDecimal marketPlaceDiscount = tuple.get("marketPlaceDiscount", BigDecimal.class);
            BigDecimal salesPrice = tuple.get("productSalesPrice", BigDecimal.class);

            BigDecimal totalDiscountPercent = totalDiscountPercentCalculate(vendorDiscount, marketPlaceDiscount);
            BigDecimal totalDiscountAmount = totalDiscountCalculate(salesPrice, totalDiscountPercent);
            BigDecimal afterDiscountAmount = finalNetPrice(salesPrice, totalDiscountPercent);

            resultMap.put("totalDiscountPercent", totalDiscountPercent);
            resultMap.put("totalDiscountedAmount", totalDiscountAmount);
            resultMap.put("afterDiscountRemainingAmount", afterDiscountAmount);

            resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
            resultMap.put("salesPrice", tuple.get("productSalesPrice"));
            resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
            resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
            resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
            resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
            resultMap.put("uom", tuple.get("productUOM"));
            resultMap.put("imageUrl", tuple.get("imageUrl"));
            resultMap.put("imageName", tuple.get("productImageName"));
            resultMap.put("newProduct", tuple.get("productNewProduct"));
            resultMap.put("onlineShow", tuple.get("productOnlineShow"));
            resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
            resultMap.put("manageStock", tuple.get("productManageStock"));
            resultMap.put("allowPreorder", tuple.get("productAllowPreorder"));
            resultMap.put("preorderAvailableFrom", tuple.get("productPreorderAvailableFrom"));
            resultMap.put("manageProductVariants", tuple.get("productManageProductVariants"));
            resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
            resultMap.put("stockAvailableQuantity", tuple.get("productStockAvailableQuantity"));
            resultMap.put("status", tuple.get("productStatus"));
            resultMap.put("metaTitle", tuple.get("productMetaTitle"));
            resultMap.put("metaDescription", tuple.get("productMetaDescription"));
            resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
            resultMap.put("createdBy", tuple.get("productCreatedBy"));
            resultMap.put("created", tuple.get("productCreated"));
            resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
            resultMap.put("modified", tuple.get("productModified"));

            resultList.add(resultMap);
        }
        return enrichProductAvailability(resultList);
    }

    public List<Map<String, Object>> categoryProductsForFrontView(
            String categoryUuid,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<Long> manufacturerIds,
            Boolean discountedOnly,
            Boolean newProduct,
            Boolean emiavailable
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        Root<Product> productRoot = cq.from(Product.class);
        Join<Product, Vendorprofile> vendorJoin = productRoot.join("vendorprofile", JoinType.LEFT);
        Join<Product, ?> manufacturerJoin = productRoot.join("manufacturer", JoinType.LEFT);

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        Expression<String> imageUrl = cb.concat(
                cb.literal(baseUrl + "/files/"),
                productRoot.get("imageName")
        );

        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("uuid").alias("productUuid"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                vendorJoin.get("id").alias("vendorProfileId"),
                vendorJoin.get("companyName").alias("vendorProfileName"),
                productRoot.get("productcategory").get("id").alias("productCategoryId"),
                productRoot.get("productcategory").get("uuid").alias("productCategoryUuid"),
                productRoot.get("productcategory").get("name").alias("productCategoryName"),
                manufacturerJoin.get("id").alias("manufacturerId"),
                manufacturerJoin.get("name").alias("manufacturerName"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("marketPlaceCommissionRate").alias("marketPlaceCommissionRate"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("marketPlaceDiscount").alias("marketPlaceDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").get("name").alias("productUOM"),
                imageUrl.alias("imageUrl"),
                productRoot.get("imageName").alias("productImageName"),
                productRoot.get("newProduct").alias("productNewProduct"),
                productRoot.get("onlineShow").alias("productOnlineShow"),
                productRoot.get("featuredProduct").alias("productFeaturedProduct"),
                productRoot.get("manageStock").alias("productManageStock"),
                productRoot.get("allowPreorder").alias("productAllowPreorder"),
                productRoot.get("preorderAvailableFrom").alias("productPreorderAvailableFrom"),
                productRoot.get("manageProductVariants").alias("productManageProductVariants"),
                productRoot.get("emiavailable").alias("productEmiAvailable"),
                productRoot.get("stockAvailableQuantity").alias("productStockAvailableQuantity"),
                productRoot.get("status").alias("productStatus"),
                productRoot.get("metaTitle").alias("productMetaTitle"),
                productRoot.get("metaDescription").alias("productMetaDescription"),
                productRoot.get("metaKeywords").alias("productMetaKeywords"),
                productRoot.get("createdBy").alias("productCreatedBy"),
                productRoot.get("created").alias("productCreated"),
                productRoot.get("modifiedBy").alias("productModifiedBy"),
                productRoot.get("modified").alias("productModified")
        );

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(
                cb.upper(productRoot.get("status").as(String.class)),
                ProductStatusEnum.Active.name().toUpperCase()
        ));

        if (categoryUuid != null && !categoryUuid.isBlank()) {
            var categorySubquery = cq.subquery(Long.class);
            Root<Productcategory> categoryRoot = categorySubquery.from(Productcategory.class);

            categorySubquery.select(categoryRoot.get("id"));
            categorySubquery.where(
                    cb.or(
                            cb.equal(categoryRoot.get("uuid"), categoryUuid),
                            cb.equal(categoryRoot.get("parent").get("uuid"), categoryUuid)
                    )
            );

            predicates.add(productRoot.get("productcategory").get("id").in(categorySubquery));
        }

        if (manufacturerIds != null && !manufacturerIds.isEmpty()) {
            predicates.add(manufacturerJoin.get("id").in(manufacturerIds));
        }

        if (Boolean.TRUE.equals(newProduct)) {
            predicates.add(cb.isTrue(productRoot.get("newProduct")));
        }

        if (Boolean.TRUE.equals(emiavailable)) {
            predicates.add(cb.isTrue(productRoot.get("emiavailable")));
        }

        if (Boolean.TRUE.equals(discountedOnly)) {
            predicates.add(
                    cb.or(
                            cb.greaterThan(productRoot.get("vendordiscount"), BigDecimal.ZERO),
                            cb.greaterThan(productRoot.get("marketPlaceDiscount"), BigDecimal.ZERO)
                    )
            );
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(productRoot.get("id")));

        List<Tuple> resultTuples = em.createQuery(cq).getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            BigDecimal vendorDiscount = tuple.get("productVendorDiscount", BigDecimal.class);
            BigDecimal marketPlaceDiscount = tuple.get("marketPlaceDiscount", BigDecimal.class);
            BigDecimal salesPrice = tuple.get("productSalesPrice", BigDecimal.class);

            BigDecimal totalDiscountPercent = totalDiscountPercentCalculate(vendorDiscount, marketPlaceDiscount);
            BigDecimal totalDiscountAmount = totalDiscountCalculate(salesPrice, totalDiscountPercent);
            BigDecimal afterDiscountAmount = finalNetPrice(salesPrice, totalDiscountPercent);

            if (minPrice != null && afterDiscountAmount.compareTo(minPrice) < 0) {
                continue;
            }

            if (maxPrice != null && afterDiscountAmount.compareTo(maxPrice) > 0) {
                continue;
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
            resultMap.put("uuid", tuple.get("productUuid"));
            resultMap.put("sku", tuple.get("productSku"));
            resultMap.put("userId", tuple.get("productUserId"));
            resultMap.put("vendorProfileId", tuple.get("vendorProfileId"));
            resultMap.put("vendorProfile", tuple.get("vendorProfileName"));
            resultMap.put("categoryId", tuple.get("productCategoryId"));
            resultMap.put("categoryUuid", tuple.get("productCategoryUuid"));
            resultMap.put("category", tuple.get("productCategoryName"));
            resultMap.put("manufacturerId", tuple.get("manufacturerId"));
            resultMap.put("manufacturerName", tuple.get("manufacturerName"));
            resultMap.put("title", tuple.get("productTitle"));
            resultMap.put("slug", tuple.get("productSlug"));
            resultMap.put("orderno", tuple.get("productOrderno"));
            resultMap.put("shortDescription", tuple.get("productShortDescription"));
            resultMap.put("description", tuple.get("productDescription"));
            resultMap.put("video", tuple.get("productVideo"));
            resultMap.put("totalDiscountPercent", totalDiscountPercent);
            resultMap.put("totalDiscountedAmount", totalDiscountAmount);
            resultMap.put("afterDiscountRemainingAmount", afterDiscountAmount);
            resultMap.put("purchasePrice", tuple.get("productPurchasePrice"));
            resultMap.put("salesPrice", tuple.get("productSalesPrice"));
            resultMap.put("marketPlaceCommissionRate", tuple.get("marketPlaceCommissionRate"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("marketPlaceDiscount", tuple.get("marketPlaceDiscount"));
            resultMap.put("vendorDiscount", tuple.get("productVendorDiscount"));
            resultMap.put("discountStartDate", tuple.get("productDiscountStartDate"));
            resultMap.put("discountEndDate", tuple.get("productDiscountEndDate"));
            resultMap.put("uom", tuple.get("productUOM"));
            resultMap.put("imageUrl", tuple.get("imageUrl"));
            resultMap.put("imageName", tuple.get("productImageName"));
            resultMap.put("newProduct", tuple.get("productNewProduct"));
            resultMap.put("onlineShow", tuple.get("productOnlineShow"));
            resultMap.put("featuredProduct", tuple.get("productFeaturedProduct"));
            resultMap.put("manageStock", tuple.get("productManageStock"));
            resultMap.put("allowPreorder", tuple.get("productAllowPreorder"));
            resultMap.put("preorderAvailableFrom", tuple.get("productPreorderAvailableFrom"));
            resultMap.put("manageProductVariants", tuple.get("productManageProductVariants"));
            resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
            resultMap.put("stockAvailableQuantity", tuple.get("productStockAvailableQuantity"));
            resultMap.put("status", tuple.get("productStatus"));
            resultMap.put("metaTitle", tuple.get("productMetaTitle"));
            resultMap.put("metaDescription", tuple.get("productMetaDescription"));
            resultMap.put("metaKeywords", tuple.get("productMetaKeywords"));
            resultMap.put("createdBy", tuple.get("productCreatedBy"));
            resultMap.put("created", tuple.get("productCreated"));
            resultMap.put("modifiedBy", tuple.get("productModifiedBy"));
            resultMap.put("modified", tuple.get("productModified"));

            resultList.add(resultMap);
        }

        return resultList;
    }

    public BigDecimal totalDiscountPercentCalculate(BigDecimal vendorDiscount, BigDecimal marketPlaceDiscount) {
        if (vendorDiscount == null) {
            vendorDiscount = BigDecimal.ZERO;
        }
        if (marketPlaceDiscount == null) {
            marketPlaceDiscount = BigDecimal.ZERO;
        }

        return vendorDiscount.add(marketPlaceDiscount);
    }

    public BigDecimal totalDiscountCalculate(BigDecimal salesPrice, BigDecimal totalDiscountPercent) {
        if (salesPrice == null) {
            salesPrice = BigDecimal.ZERO;
        }
        if (totalDiscountPercent == null) {
            totalDiscountPercent = BigDecimal.ZERO;
        }

        // (salesPrice * totalDiscountPercent) / 100
        BigDecimal discount = salesPrice.multiply(totalDiscountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return discount;
    }

    public BigDecimal finalNetPrice(BigDecimal salesPrice, BigDecimal totalDiscountPercent) {
        BigDecimal discount = totalDiscountCalculate(salesPrice, totalDiscountPercent);
        return salesPrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

}
