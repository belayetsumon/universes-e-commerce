/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.config;

import com.ecommerce.app.globalComponant.DistrictInterceptor;
import com.ecommerce.app.services.StorageProperties;
import jakarta.annotation.PostConstruct;
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

    @Autowired
    private DistrictInterceptor districtInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations(properties.url())
                .setCachePeriod(0);
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(districtInterceptor)
//                .addPathPatterns("/cart/**"); // apply to all paths
//    }
}
