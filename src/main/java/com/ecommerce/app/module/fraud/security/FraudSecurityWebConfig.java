package com.ecommerce.app.module.fraud.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FraudSecurityWebConfig implements WebMvcConfigurer {

    private final FraudAdminCsrfInterceptor fraudAdminCsrfInterceptor;

    public FraudSecurityWebConfig(FraudAdminCsrfInterceptor fraudAdminCsrfInterceptor) {
        this.fraudAdminCsrfInterceptor = fraudAdminCsrfInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fraudAdminCsrfInterceptor)
                .addPathPatterns("/admin/fraud/**");
    }
}
