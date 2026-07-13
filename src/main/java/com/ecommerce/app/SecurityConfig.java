/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app;

import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.*;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    public static final String[] STATIC_WHITELIST = {
        "/assets/**",
        "/css/**",
        "/js/**",
        "/img/**",
        "/plugin/**",
        "/webjars/**",
        "/files/**"
    };

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity httpSecurity, UserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }

    String[] PUBLIC_URLS = {
        "/",
        "/public/**",
        "/cart/**",
        "/carts/**",
        "/cart_address/**",
        "/checkout/guest/mobile/**",
        "/order/create",
        "/order/savebyvendor",
        "/order/savebyvendorupdate",
        "/order/placed",
        "/users/uregistrations",
        "/users/usave",
        "/users/frontRegistrationSave",
        "/customer_registration/registration",
        "/customer_registration/customer_registration_save",
        "/users/userforgotpassword",
        "/forgotpassword/**",
        "/district/select-district",
        "/district/save-district",
        "/district/thanas",
        "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(
                        csrf -> csrf.disable()
                //                        csrf -> csrf
                //                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                //                .ignoringRequestMatchers(PUBLIC_URLS)
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(STATIC_WHITELIST).permitAll()
                .requestMatchers(PUBLIC_URLS).permitAll()
                .anyRequest().authenticated()
                )
                .formLogin(login -> login
                .loginPage("/public/member-login")
                .successHandler(customLoginSuccessHandler)
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/users/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/users/logout"))
                .logoutSuccessUrl("/public/member-login")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
                )
                .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

}
