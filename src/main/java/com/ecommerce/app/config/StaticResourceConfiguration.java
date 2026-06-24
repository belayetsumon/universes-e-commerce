/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.config;

import com.ecommerce.app.globalComponant.ShippingLocationInterceptor;
import com.ecommerce.app.module.user.componant.LoginActivityInterceptor;
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

    @Autowired
    private ShippingLocationInterceptor shippingLocationInterceptor;

    @Autowired
    private LoginActivityInterceptor loginActivityInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations(properties.url())
                .setCachePeriod(0);
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(shippingLocationInterceptor)
//                .addPathPatterns("/cart/**"); // apply to all paths
//    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginActivityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/assets/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/images/**",
                        "/plugin/**",
                        "/webjars/**",
                        "/files/**",
                        "/favicon.ico",
                        "/public/**",
                        "/error"
                );
    }
}
