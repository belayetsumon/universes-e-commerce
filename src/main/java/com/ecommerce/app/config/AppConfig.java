/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.config;

import com.ecommerce.app.globalComponant.AppProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author libertyerp_local
 */
@Configuration
@PropertySource("classpath:app-config.properties")
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

}
