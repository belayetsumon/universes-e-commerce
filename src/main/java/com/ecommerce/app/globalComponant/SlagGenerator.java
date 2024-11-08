/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import java.text.Normalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class SlagGenerator {

    @Autowired
    UnixTimeComponent unixTimeComponent;

    public String generateSlug(String productName) {
        // Convert to lowercase
        String slug = productName.toLowerCase();

        // Remove accents and diacritical marks
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        slug = slug.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        // Replace spaces and special characters with hyphens
        slug = slug.replaceAll("[^a-z0-9]+", "-");

        // Remove leading and trailing hyphens
        slug = slug.replaceAll("^-|-$", "");
        
        String finalSlug = slug+"-" + String.valueOf(unixTimeComponent.unixTimeEpochSecond());

        return finalSlug;
    }

}
