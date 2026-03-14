/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.ads.services;

import com.ecommerce.app.module.ads.model.Ads;
import com.ecommerce.app.module.ads.model.Placement;
import com.ecommerce.app.module.ads.repository.AdsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
public class AdsService {

    @Autowired
    EntityManager em;

    @Autowired
    private AdsRepository adsRepository;

    public List<Ads> getAds() {
        return adsRepository.findAll();
    }

    public Ads getById(Long id) {
        return adsRepository.findById(id).orElseThrow(() -> new RuntimeException("Ad not found"));
    }

    public Ads save(Ads ad) {
        return adsRepository.save(ad);
    }

    public void delete(Long id) {
        adsRepository.deleteById(id);
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
                root.get("displayOrder").alias("displayOrder")
        );

        // --- WHERE ---
        cq.where(
                cb.and(
                        cb.equal(root.get("active"), true),
                        cb.equal(root.get("placement"), placement)
                )
        );

        // --- ORDER BY ---
        cq.orderBy(cb.desc(root.get("displayOrder")));

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
                    return m;
                })
                .toList();
    }

}
