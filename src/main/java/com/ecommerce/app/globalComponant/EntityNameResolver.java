/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Component.java to edit this template
 */
package com.ecommerce.app.globalComponant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import static org.springframework.core.NestedExceptionUtils.getRootCause;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 *
 * @author libertyerp_local
 */
@Component
public class EntityNameResolver {

    @PersistenceContext
    private EntityManager em;

//    public List<String> resolveEntityNames(DataIntegrityViolationException ex, Class<?> deletingEntity) {
//        Throwable cause = ex.getCause();
//        Set<String> matchedEntities = new LinkedHashSet<>();
//
//        String sqlMessage = cause != null ? cause.getMessage().toLowerCase() : "";
//
//        // Check for Hibernate ConstraintViolationException
//        if (cause instanceof ConstraintViolationException cve) {
//            String constraintName = cve.getConstraintName();
//            if (constraintName != null) {
//                String lcConstraint = constraintName.toLowerCase();
//                em.getMetamodel().getEntities().forEach(entity -> {
//                    if (!entity.getJavaType().equals(deletingEntity)) {
//                        String tableName = getTableName(entity).toLowerCase();
//                        String className = entity.getJavaType().getSimpleName().toLowerCase();
//                        if (lcConstraint.contains(tableName) || lcConstraint.contains(className)) {
//                            matchedEntities.add(getDisplayName(entity));
//                        }
//                    }
//                });
//            }
//        }
//
//        // Parse SQL message for table names
//        if (sqlMessage.contains("foreign key constraint fails")
//                || sqlMessage.contains("violates foreign key constraint")) {
//
//            em.getMetamodel().getEntities().forEach(entity -> {
//                if (!entity.getJavaType().equals(deletingEntity)) {
//                    String tableName = getTableName(entity).toLowerCase();
//                    String className = entity.getJavaType().getSimpleName().toLowerCase();
//                    if (sqlMessage.contains("`" + tableName + "`") || sqlMessage.contains("`" + className + "`")) {
//                        matchedEntities.add(getDisplayName(entity));
//                    }
//                }
//            });
//        }
//
//        // Fallback
////        if (matchedEntities.isEmpty() && deletingEntity != null) {
////            matchedEntities.add(getDisplayName(deletingEntity));
////        }
//        return new ArrayList<>(matchedEntities);
//    }
//    public String getTableName(EntityType<?> entity) {
//        Table table = entity.getJavaType().getAnnotation(Table.class);
//        if (table != null && !table.name().isBlank()) {
//            return table.name();
//        }
//        return entity.getName();
//    }
//    public String getDisplayName(EntityType<?> entity) {
//        String spaced = entity.getName().replaceAll("([a-z])([A-Z])", "$1 $2");
//        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
//    }
//
//    public String getDisplayName(Class<?> clazz) {
//        if (clazz == null) {
//            return "Unknown";
//        }
//        String spaced = clazz.getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2");
//        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
//    }
    public List<String> detectReferencingEntities(DataIntegrityViolationException ex, Class<?> deletingEntity) {
        Set<String> matchedEntities = new LinkedHashSet<>();
        Throwable root = getRootCause(ex);
        String sqlMessage = root.getMessage().toLowerCase();

        em.getMetamodel().getEntities().forEach(entity -> {
            if (entity.getJavaType().equals(deletingEntity)) {
                return;
            }

            String tableName = getTableName(entity).toLowerCase();
            if (sqlMessage.contains(tableName)) {
                matchedEntities.add(getDisplayName(entity));
            }
        });

        return new ArrayList<>(matchedEntities);
    }

    public String getTableName(EntityType<?> entity) {
        Table table = entity.getJavaType().getAnnotation(Table.class);
        if (table != null && !table.name().isBlank()) {
            return table.name();
        }
        return entity.getName();
    }

    public String getDisplayName(EntityType<?> entity) {
        String spaced = entity.getName().replaceAll("([a-z])([A-Z])", "$1 $2");
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }

    public String getDisplayName(Class<?> clazz) {
        if (clazz == null) {
            return "Unknown";
        }
        String spaced = clazz.getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2");
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
