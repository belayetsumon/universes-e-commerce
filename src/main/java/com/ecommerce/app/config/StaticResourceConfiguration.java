/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.config;

import com.ecommerce.app.services.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 *
 * @author User
 */
@Configuration
public class StaticResourceConfiguration implements WebMvcConfigurer {

    @Autowired
    StorageProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations(properties.url())
                .setCachePeriod(0);
    }
}
