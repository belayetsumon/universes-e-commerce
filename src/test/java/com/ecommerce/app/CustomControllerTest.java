/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Controller.java to edit this template
 */
package com.ecommerce.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 *
 * @author libertyerp_local
 */
//@WebMvcTest(UsersController.class)
public class CustomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDeleteUser_FKViolation() {

    }

}
