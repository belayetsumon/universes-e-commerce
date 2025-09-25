/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.utility;

import org.springframework.dao.DataIntegrityViolationException;

/**
 *
 * @author libertyerp_local
 */
public class UniqueConstraintUtil {

    /**
     * Extracts the field name from a unique constraint exception message.
     * Example MySQL message: "Duplicate entry 'XL' for key
     * 'UK_product_size_name'"
     */
    public static String extractFieldName(DataIntegrityViolationException ex) {
        String message = ex.getRootCause().getMessage();
        if (message != null && message.contains("for key")) {
            // extract field from key name (assumes naming convention: UK_table_field)
            int idx = message.indexOf("for key");
            String key = message.substring(idx + 8).replace("'", "").trim();
            // e.g., UK_product_size_name -> name
            String[] parts = key.split("_");
            return parts[parts.length - 1];
        }
        return "value"; // fallback
    }
}
