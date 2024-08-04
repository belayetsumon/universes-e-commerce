/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author User
 */
@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
public class AuditConfiguration {

    @Bean

    public AuditorAware<String> auditorAware() {

        return new AuditorAwareImpl();

    }
}
