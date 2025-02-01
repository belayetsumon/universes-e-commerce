/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app;

<<<<<<< HEAD
import com.ecommerce.app.product.services.ProductService;
=======
import com.ecommerce.app.globalComponant.SlagGenerator;
>>>>>>> 8be69ac5b0b4aff187039abad5bb6d2f07da813f
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Testproject {

    @Autowired
<<<<<<< HEAD
    ProductService productService;

    @Test
    public void contextLoads() {

   //System.out.println(" Test ............." + productService.all_Product_for_admin_By_Id(1l).size());
=======
    private SlagGenerator slagGenerator;

    @Test
    public void contextLoads() {
        
       
        
        System.out.println(" Test ............." + slagGenerator.generateSlug("bangladesh 2024"));
>>>>>>> 8be69ac5b0b4aff187039abad5bb6d2f07da813f
    }

}
