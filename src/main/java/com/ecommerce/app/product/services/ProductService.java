/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.product.services;

import com.ecommerce.app.product.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author libertyerp_local
 */
@Service
public class ProductService {

    @Autowired
    EntityManager em;

    public List<Map<String, Object>> allProduct() {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);

        // Step 2: Define root entity
        Root<Product> productRoot = cq.from(Product.class);

        // Step 3: Select desired fields using Multiselect (Select with alias)
        cq.multiselect(
                productRoot.get("id").alias("productId"),
                productRoot.get("sku").alias("productSku"),
                productRoot.get("userId").alias("productUserId"),
                //              productRoot.get("vendorprofile").alias("productVendorProfile"),
                productRoot.get("productcategory").alias("productCategory"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("companyProfit").alias("productCompanyProfit"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("companyDiscount").alias("productCompanyDiscount"),
                productRoot.get("vendordiscount").alias("productVendorDiscount"),
                productRoot.get("discountStartDate").alias("productDiscountStartDate"),
                productRoot.get("discountEndDate").alias("productDiscountEndDate"),
                productRoot.get("uom").alias("productUOM"),
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
            resultMap.put("sompanyProfit", tuple.get("productCompanyProfit"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("companyDiscount", tuple.get("productCompanyDiscount"));
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
                //                productRoot.get("vendorprofile").alias("productVendorProfile"),
                productRoot.get("productcategory").get("name").alias("productCategory"),
                productRoot.get("title").alias("productTitle"),
                productRoot.get("slug").alias("productSlug"),
                productRoot.get("orderno").alias("productOrderno"),
                productRoot.get("shortDescription").alias("productShortDescription"),
                productRoot.get("description").alias("productDescription"),
                productRoot.get("video").alias("productVideo"),
                productRoot.get("purchasePrice").alias("productPurchasePrice"),
                productRoot.get("salesPrice").alias("productSalesPrice"),
                productRoot.get("companyProfit").alias("productCompanyProfit"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("companyDiscount").alias("productCompanyDiscount"),
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
            resultMap.put("sompanyProfit", tuple.get("productCompanyProfit"));
            resultMap.put("productType", tuple.get("productType"));
            resultMap.put("companyDiscount", tuple.get("productCompanyDiscount"));
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
                productRoot.get("companyProfit").alias("productCompanyProfit"),
                productRoot.get("productType").alias("productType"),
                productRoot.get("companyDiscount").alias("productCompanyDiscount"),
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
        resultMap.put("companyProfit", tuple.get("productCompanyProfit"));
        resultMap.put("productType", tuple.get("productType"));
        resultMap.put("companyDiscount", tuple.get("productCompanyDiscount"));
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

}
