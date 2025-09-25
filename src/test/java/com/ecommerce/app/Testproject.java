/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app;

import com.ecommerce.app.globalComponant.EntityNameResolver;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.user.services.UsersService;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class Testproject {

//    @Autowired
//    SalesOrderService salesOrderService;
    @Autowired
    SalesOrderRepository salesOrderRepository;
    @Autowired
    UsersService usersService;

    @Autowired
    EntityNameResolver entityNameResolver;

    @Autowired
    UsersRepository repository;

    @Test
    public void contextLoads() {

        DataIntegrityViolationException caughtException = null;
        try {
            repository.deleteById(95l);
            repository.flush(); // Force DB operation
        } catch (DataIntegrityViolationException ex) {
            caughtException = ex;
        }

        //  List<String> referencingEntities = entityNameResolver.detectReferencingEntities(caughtException, Users.class);
        // 5. Assertions
        // Optional: check friendly message
        List<String> referencingEntities = entityNameResolver.detectReferencingEntities(caughtException, Users.class);

        System.out.println("Referenced by: " + referencingEntities);
        //                + ". It is referenced by: " + String.join(", ", referencingEntities);
        //
        //        System.out.println(message);
        // Output: Cannot delete User. It is referenced by: Wallet

    }

}
