/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ads.services;

import com.ecommerce.app.module.ads.model.Ads;
import com.ecommerce.app.module.ads.model.Placement;
import com.ecommerce.app.module.ads.model.TargetType;
import com.ecommerce.app.module.ads.repository.AdsRepository;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductcategoryRepository;
import com.ecommerce.app.vendor.repository.VendorprofileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author libertyerp_local
 */
@Service
public class AdsService {

    @Autowired
    EntityManager em;

    @Autowired
    private AdsRepository adsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductcategoryRepository productcategoryRepository;

    @Autowired
    private VendorprofileRepository vendorprofileRepository;

    public List<Ads> getAds(String title, String placement, String targetType) {
        return adsRepository.findAll(
                AdsSpecification.filter(title, placement, targetType),
                Sort.by(Sort.Order.desc("displayOrder"), Sort.Order.desc("id"))
        );
    }

    public Ads getById(Long id) {
        return adsRepository.findById(id).orElseThrow(() -> new RuntimeException("Ad not found"));
    }

    public Ads findByIdOrNull(Long id) {
        if (id == null) {
            return null;
        }
        return adsRepository.findById(id).orElse(null);
    }

    public Ads save(Ads ad) {
        return adsRepository.save(ad);
    }

    public void delete(Long id) {
        adsRepository.deleteById(id);
    }

    public boolean hasCategory(String uuid) {
        return uuid != null && !uuid.isBlank() && productcategoryRepository.findByUuid(uuid.trim()).isPresent();
    }

    public boolean hasProduct(String uuid) {
        return uuid != null && !uuid.isBlank() && productRepository.findByUuid(uuid.trim()).isPresent();
    }

    public boolean hasVendor(String uuid) {
        return uuid != null && !uuid.isBlank() && vendorprofileRepository.findByUuid(uuid.trim()).isPresent();
    }

    public List<Map<String, Object>> findAllAdsAsMap(Placement placement) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Ads> root = cq.from(Ads.class);

        // --- SELECT (added width, height) ---
        cq.multiselect(
                root.get("id").alias("id"),
                root.get("title").alias("title"),
                root.get("imageUrl").alias("imageUrl"),
                root.get("placement").alias("placement"),
                root.get("width").alias("width"),
                root.get("height").alias("height"),
                root.get("displayOrder").alias("displayOrder"),
                root.get("targetType").alias("targetType"),
                root.get("categoryId").alias("categoryId"),
                root.get("productId").alias("productId"),
                root.get("vendorId").alias("vendorId"),
                root.get("externalUrl").alias("externalUrl")
        );

        // --- WHERE ---
        cq.where(
                cb.and(
                        cb.equal(root.get("active"), true),
                        cb.equal(root.get("placement"), placement)
                )
        );

        // --- ORDER BY ---
        cq.orderBy(cb.desc(root.get("displayOrder")), cb.desc(root.get("id")));

        List<Tuple> tuples = em.createQuery(cq).getResultList();

        // --- MANUAL MAP ---
        return tuples.stream()
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", t.get("id"));
                    m.put("title", t.get("title"));
                    m.put("imageUrl", t.get("imageUrl"));
                    m.put("placement", t.get("placement"));
                    m.put("width", t.get("width"));
                    m.put("height", t.get("height"));
                    m.put("displayOrder", t.get("displayOrder"));
                    TargetType targetType = t.get("targetType", TargetType.class);
                    String categoryId = t.get("categoryId", String.class);
                    String productId = t.get("productId", String.class);
                    String vendorId = t.get("vendorId", String.class);
                    String externalUrl = t.get("externalUrl", String.class);
                    m.put("targetType", targetType);
                    m.put("targetUrl", resolveTargetUrl(targetType, categoryId, productId, vendorId, externalUrl));
                    m.put("openInNewTab", targetType == TargetType.EXTERNAL);
                    return m;
                })
                .toList();
    }

    private String resolveTargetUrl(
            TargetType targetType,
            String categoryUuid,
            String productUuid,
            String vendorUuid,
            String externalUrl) {

        if (targetType == null) {
            return null;
        }

        return switch (targetType) {
            case CATEGORY -> productcategoryRepository.findByUuid(trimToNull(categoryUuid))
                    .map(category -> UriComponentsBuilder.fromPath("/public/product-by-category/{uuid}")
                    .buildAndExpand(category.getUuid())
                    .toUriString())
                    .orElse(null);
            case PRODUCT -> productRepository.findByUuid(trimToNull(productUuid))
                    .map(product -> UriComponentsBuilder.fromPath("/public/single-product/{uuid}")
                    .buildAndExpand(product.getUuid())
                    .toUriString())
                    .orElse(null);
            case VENDOR -> vendorprofileRepository.findByUuid(trimToNull(vendorUuid))
                    .map(vendor -> UriComponentsBuilder.fromPath("/public/product")
                    .queryParam("vendor", vendor.getUuid())
                    .toUriString())
                    .orElse(null);
            case EXTERNAL -> trimToNull(externalUrl);
        };
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
