/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.vendor.model.Vendorprofile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.upload.dir}")
    String imagePath;

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
                productRoot.get("emiavailable").alias("productEmiAvailable"),
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

    public List<Map<String, Object>> all_Product_for_admin() {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Step 2: Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        // Step 3: Select desired fields using Multiselect (Select with alias)
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("vendorprofile").get("companyName").alias("productVendorProfile"),
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
                productRoot.get("emiavailable").alias("productEmiAvailable"),
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
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
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
                productRoot.get("emiavailable").alias("productEmiAvailable"),
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
        resultMap.put("userId", tuple.get("productUserId"));
        resultMap.put("category", tuple.get("productCategory"));
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

    public List<Map<String, Object>> all_Product_for_admin_By_Vendor_Id(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        // Select desired fields using Multiselect
        cq.multiselect(
                productRoot.get("id").alias("productId"),
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
                productRoot.get("emiavailable").alias("productEmiAvailable"),
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

    public Map<String, Object> product_details_for_front_view_single_product_page_by_Id(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        Join<Product, Vendorprofile> vendorJoin = productRoot.join("vendorprofile", JoinType.LEFT);
        // Select desired fields using multiselect
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                vendorJoin.get("id").alias("vendorProfileId"),
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
                productRoot.get("emiavailable").alias("productEmiAvailable"),
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
        resultMap.put("userId", tuple.get("productUserId"));
        resultMap.put("vendorProfileId", tuple.get("vendorProfileId"));
        resultMap.put("vendorProfileName", tuple.get("vendorProfileName"));
        resultMap.put("category", tuple.get("productCategory"));
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
        resultMap.put("emiAvailable", tuple.get("productEmiAvailable"));
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
                productRoot.get("emiavailable").alias("productEmiAvailable"),
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

        // Filter by Vendor Profile ID
        if (vendorprofileId != null) {
            predicates.add(cb.equal(productRoot.get("vendorProfileId").get("id"), vendorprofileId));
        }

        // Filter by Product Category
        if (productcategory != null) {
            predicates.add(cb.equal(productRoot.get("productCategory").get("id"), productcategory));
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
            predicates.add(cb.equal(productRoot.get("emiAvailable"), emiavailable));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));
        cq.orderBy(cb.desc(productRoot.get("id")));

        // Step 5: Execute the query and get results as List<Tuple>
        List<Tuple> resultTuples = em.createQuery(cq).getResultList();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Tuple tuple : resultTuples) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", tuple.get("productId"));
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
