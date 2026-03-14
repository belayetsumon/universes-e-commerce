/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class FullDynamicDependencyService {

    private final EntityManager entityManager;

    public FullDynamicDependencyService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public String checkDependencies(Object entity) {
        Class<?> targetClass = entity.getClass();
        Metamodel metamodel = entityManager.getMetamodel();

        StringBuilder message = new StringBuilder();
        boolean hasDependencies = false;

        // Check singular attributes (ManyToOne, OneToOne)
        for (EntityType<?> managedType : metamodel.getEntities()) {
            Class<?> managedClass = managedType.getJavaType();

            for (SingularAttribute<?, ?> attr : managedType.getSingularAttributes()) {
                if ((attr.getType().getJavaType() == targetClass)
                        && (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                        || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE)) {

                    String jpql = String.format("SELECT COUNT(e) FROM %s e WHERE e.%s = :entity",
                            managedClass.getSimpleName(), attr.getName());
                    Long count = entityManager.createQuery(jpql, Long.class)
                            .setParameter("entity", entity)
                            .getSingleResult();

                    if (count > 0) {
                        hasDependencies = true;
                        if (message.length() > 0) {
                            message.append(", ");
                        }
                        message.append(count).append(" ").append(managedClass.getSimpleName()).append("(s)");
                    }
                }
            }

            // Check plural attributes (OneToMany, ManyToMany)
            for (PluralAttribute<?, ?, ?> attr : managedType.getPluralAttributes()) {
                if ((attr.getElementType().getJavaType() == targetClass)
                        && (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY
                        || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY)) {

                    String jpql = String.format("SELECT COUNT(e) FROM %s e JOIN e.%s c WHERE c = :entity",
                            managedClass.getSimpleName(), attr.getName());
                    Long count = entityManager.createQuery(jpql, Long.class)
                            .setParameter("entity", entity)
                            .getSingleResult();

                    if (count > 0) {
                        hasDependencies = true;
                        if (message.length() > 0) {
                            message.append(", ");
                        }
                        message.append(count).append(" ").append(managedClass.getSimpleName()).append("(s)");
                    }
                }
            }
        }

        if (hasDependencies) {
            return String.format("Cannot delete %s. It is linked with: %s.",
                    targetClass.getSimpleName(), message);
        }

        return null;
    }
}
