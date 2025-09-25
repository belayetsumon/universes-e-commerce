/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Service.java to edit this template
 */
package com.ecommerce.app.module.user.services;

import com.ecommerce.app.exception.ForeignKeyConstraintException;
import com.ecommerce.app.globalComponant.EntityNameResolver;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author libertyerp_local
 */
@Service
public class UsersService {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    private EntityNameResolver entityNameResolver;

    public String generateRefaraleCode() {

        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 8; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString().toUpperCase();

        return output;
    }

//    public void deleteById(Long id) {
//        try {
//            repository.deleteById(id);
//        } catch (DataIntegrityViolationException e) {
//            List<String> entities = entityNameResolver.resolveEntityNames(e);
//            String message = "Cannot delete record. It is referenced by: " + String.join(", ", entities);
//            throw new ForeignKeyConstraintException(message);
//        }
//    }
//    public void deleteById(Long id) {
//        try {
//            repository.deleteById(id);
//        } catch (DataIntegrityViolationException e) {
//            List<String> entities = entityNameResolver.resolveEntityNames(e, Users.class);
//            String message = "Cannot delete " + Users.class.getSimpleName()
//                    + ". It is referenced by: " + String.join(", ", entities);
//            throw new ForeignKeyConstraintException(message);
//        }
//    }
    @Transactional
    public void deleteById(Long id) {
        try {
            usersRepository.deleteById(id);
            usersRepository.flush(); // force SQL execution for FK check
        } catch (DataIntegrityViolationException ex) {
            // Detect referencing entities
            List<String> referencingEntities = entityNameResolver.detectReferencingEntities(ex, Users.class);

            // Build user-friendly message
            String message = "Cannot delete " + entityNameResolver.getDisplayName(Users.class)
                    + ". It is referenced by: " + String.join(", ", referencingEntities);

            throw new ForeignKeyConstraintException(message, ex);
        }
    }

}
