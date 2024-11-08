/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app;

import com.ecommerce.app.globalComponant.SlagGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Testproject {

    @Autowired
    private SlagGenerator slagGenerator;

    @Test
    public void contextLoads() {
        
       
        
        System.out.println(" Test ............." + slagGenerator.generateSlug("bangladesh 2024"));
    }

}
