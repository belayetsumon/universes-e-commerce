/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.globalAOPComponent;

import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.lang.reflect.Field;
import java.util.Collection;

public class EntityDeleteValidator {

    public static void validateNoChildren(Object entity) {
        Class<?> clazz = entity.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(OneToOne.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    if (value != null) {
                        if (value instanceof Collection) {
                            Collection<?> collection = (Collection<?>) value;
                            if (!collection.isEmpty()) {
                                throw new IllegalStateException(
                                        String.format(
                                                "Cannot delete [%s] because it has related [%s].",
                                                clazz.getSimpleName(),
                                                field.getName()
                                        )
                                );
                            }
                        } else {
                            throw new IllegalStateException(
                                    String.format(
                                            "Cannot delete [%s] because it has related [%s].",
                                            clazz.getSimpleName(),
                                            field.getName()
                                    )
                            );
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
