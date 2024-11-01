/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app;

import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.web.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    public static final String[] STATIC_WHITELIST = {
        "/resources/**", "/static/**", "/css/**", "/bootstrap-5.2.3-dist/**", "/bootstrap-5.2.3-dist/css/**", "/bootstrap-5.2.3-dist/js/**", "/plugin/owlcarousel/**", "/fontawesome/css/**", "/fontawesome/webfonts/**", "/plugin/owlcarousel/assets/**", "/js/**", "/img/**", "/webjars/**", "/files/**"
    };

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

//     @Bean
//    public PasswordEncoder getPasswordEncoder() {
//        return NoOpPasswordEncoder.getInstance();
//    }
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity httpSecurity, UserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http 
                .authorizeHttpRequests(auth -> {
            try {
                auth
                        .requestMatchers(
                                "/", "/public/**", "/order/create",
                                "/users/uregistrations", "/users/usave",
                                "/users/userforgotpassword",
                                "/forgotpassword/**").permitAll()
                        .requestMatchers("/users/frontRegistrationSave").permitAll()
                        //.antMatchers("/dashboards/index").hasRole("admin")
                        .anyRequest().authenticated()
                        .and()
                        .formLogin()
                        .loginPage("/public/member-login")
                        .successHandler(customLoginSuccessHandler)
                        //.defaultSuccessUrl("/", true)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        //                .failureUrl("/login?error=true")
                        .permitAll()
                        .and()
                        .logout().logoutUrl("/users/logout")
                        .logoutSuccessUrl("/public/member-login")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .and()
                        .exceptionHandling()
                        .accessDeniedPage("/access-denied");
            } catch (Exception ex) {
                Logger.getLogger(SecurityConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        http.csrf().disable();
        return http.build();

    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
    return web -> web.ignoring().requestMatchers(
                "/resources/**", 
                "/static/**",
                "/css/**", 
//                "/bootstrap/**", 
//                "/bootstrap-5.2.3-dist/css/**", 
//                "/bootstrap-5.2.3-dist/js/**", 
//                "/plugin/owlcarousel/**", 
//                "/fontawesome/css/**",
//                "/fontawesome/webfonts/**", 
//                "/plugin/owlcarousel/assets/**",
                "/js/**", 
                "/img/**", 
                "/webjars/**", 
                "/files/**"
        );
    }

}
