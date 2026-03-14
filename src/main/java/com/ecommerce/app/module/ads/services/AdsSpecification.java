/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.ads.services;

import com.ecommerce.app.module.ads.model.Ads;
import com.ecommerce.app.module.ads.model.Placement;
import com.ecommerce.app.module.ads.model.TargetType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author libertyerp_local
 */
public class AdsSpecification {

    public static Specification<Ads> filter(String title, String placement, String targetType) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (title != null && !title.isEmpty()) {
                p = cb.and(p, cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (placement != null && !placement.isEmpty()) {
                p = cb.and(p, cb.equal(root.get("placement"), Placement.valueOf(placement)));
            }
            if (targetType != null && !targetType.isEmpty()) {
                p = cb.and(p, cb.equal(root.get("targetType"), TargetType.valueOf(targetType)));
            }
            return p;
        };
    }
}
