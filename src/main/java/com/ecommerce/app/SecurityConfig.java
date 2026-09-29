/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ecommerce.app;

import com.ecommerce.app.module.user.componant.CustomLoginSuccessHandler;
import com.ecommerce.app.module.user.componant.CredentialVersionFilter;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.*;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Browser-owned state-changing workflows. Fraud admin forms retain their
    // dedicated token interceptor, while signed provider callbacks remain out
    // of Spring's session-bound CSRF matcher.
    private static final RequestMatcher BROWSER_CSRF_PROTECTED_ROUTES = new OrRequestMatcher(
            new AntPathRequestMatcher("/role/**"),
            new AntPathRequestMatcher("/privilege/**"),
            new AntPathRequestMatcher("/module/**"),
            new AntPathRequestMatcher("/adminvendorusers/**"),
            new AntPathRequestMatcher("/vendor-users/**"),
            new AntPathRequestMatcher("/adminvendor/**"),
            new AntPathRequestMatcher("/admin-vendor-payout-methods/**"),
            new AntPathRequestMatcher("/vendor-payout-methods/**"),
            new AntPathRequestMatcher("/admin/ads/**"),
            new AntPathRequestMatcher("/catalog-attributes/**"),
            new AntPathRequestMatcher("/manufacturer/**"),
            new AntPathRequestMatcher("/uom/**"),
            new AntPathRequestMatcher("/product/**"),
            new AntPathRequestMatcher("/productimage/**"),
            new AntPathRequestMatcher("/productcategory/**"),
            new AntPathRequestMatcher("/productvendor/**"),
            new AntPathRequestMatcher("/vendor_productimage/**"),
            new AntPathRequestMatcher("/vendorprofile/**"),
            new AntPathRequestMatcher("/vendorlogo/**"),
            new AntPathRequestMatcher("/vendor-payout/**"),
            new AntPathRequestMatcher("/vendorverifications/**"),
            new AntPathRequestMatcher("/customer/**"),
            new AntPathRequestMatcher("/customerorder/**"),
            new AntPathRequestMatcher("/customerprofileimage/**"),
            new AntPathRequestMatcher("/admin-customer/**"),
            new AntPathRequestMatcher("/vendor-order/**"),
            new AntPathRequestMatcher("/vendor/shipping-profile"),
            new AntPathRequestMatcher("/vendor/shipping-profile/**"),
            new AntPathRequestMatcher("/public/home-contact-save"),
            new AntPathRequestMatcher("/forgotpassword/**"),
            new AntPathRequestMatcher("/admin/settings/**"),
            new AntPathRequestMatcher("/admin/communication/**"),
            new AntPathRequestMatcher("/users/save"),
            new AntPathRequestMatcher("/users/change-password/**"),
            new AntPathRequestMatcher("/users/generate-referral-code/**"),
            new AntPathRequestMatcher("/users/delete/**"),
            new AntPathRequestMatcher("/users/deletewithexception/**"),
            new AntPathRequestMatcher("/users/logout"),
            new AntPathRequestMatcher("/changepassword/**"),
            new AntPathRequestMatcher("/users/usave"),
            new AntPathRequestMatcher("/users/frontRegistrationSave"),
            new AntPathRequestMatcher("/customer_registration/customer_registration_save"),
            new AntPathRequestMatcher("/register"),
            new AntPathRequestMatcher("/customerregister/register"),
            new AntPathRequestMatcher("/customer-profile/**"),
            new AntPathRequestMatcher("/checkout/guest/mobile/**"),
            new AntPathRequestMatcher("/checkout/customer/mobile/**"),
            new AntPathRequestMatcher("/cart/**"),
            new AntPathRequestMatcher("/carts/**"),
            new AntPathRequestMatcher("/cart_address/**"),
            new AntPathRequestMatcher("/district/**"),
            new AntPathRequestMatcher("/order/**"),
            new AntPathRequestMatcher("/coupon/**"),
            new AntPathRequestMatcher("/giftcard/**"),
            new AntPathRequestMatcher("/cashoutrequest/**"),
            new AntPathRequestMatcher("/admin/cashouts/**"),
            new AntPathRequestMatcher("/customerwallet/**"),
            new AntPathRequestMatcher("/customer-giftcard/**"),
            new AntPathRequestMatcher("/customerredeem/**"),
            new AntPathRequestMatcher("/wallet/**"),
            new AntPathRequestMatcher("/referral/**"),
            new AntPathRequestMatcher("/referral-reward/**"),
            new AntPathRequestMatcher("/reward-redemption/**"),
            new AntPathRequestMatcher("/lavelratesettings/**"),
            new AntPathRequestMatcher("/cashback-policy/**"),
            new AntPathRequestMatcher("/admin/promotions/**")
    );

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
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public CredentialVersionFilter credentialVersionFilter(ObjectProvider<UsersRepository> usersRepositoryProvider) {
        return new CredentialVersionFilter(usersRepositoryProvider.getIfAvailable());
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
        "/robots.txt",
        "/sitemap.xml",
        "/llms.txt",
        "/maintenance",
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
        "/register",
        "/customerregister/register",
        "/users/login",
        "/customer_registration/registration",
        "/customer_registration/customer_registration_save",
        "/users/userforgotpassword",
        "/forgotpassword",
        "/forgotpassword/",
        "/forgotpassword/index",
        "/forgotpassword/userforgotpassword",
        "/forgotpassword/showemail",
        "/forgotpassword/reset",
        "/district/select-district",
        "/district/save-district",
        "/district/select",
        "/district/thanas",
        "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SessionRegistry sessionRegistry,
            CredentialVersionFilter credentialVersionFilter) throws Exception {
        http
                .csrf(
                        csrf -> csrf.requireCsrfProtectionMatcher(
                                new AndRequestMatcher(
                                        CsrfFilter.DEFAULT_CSRF_MATCHER,
                                        BROWSER_CSRF_PROTECTED_ROUTES
                                )
                        )
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(STATIC_WHITELIST).permitAll()
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers("/adminvendorusers/**").hasAnyAuthority(
                        "platform.vendor.management.read",
                        "platform.vendor.management.manage",
                        "platform.vendor.management.delete",
                        "platform.vendor.management.privilege",
                        "admin", "ROLE_ADMIN")
                .requestMatchers("/admin/settings/**").hasAnyAuthority("admin", "ROLE_ADMIN")
                .requestMatchers("/admin/communication/**").hasAnyAuthority("admin", "ROLE_ADMIN")
                .requestMatchers("/order", "/order/", "/order/index").hasAnyAuthority(
                        "platform.order.payment.read", "admin", "ROLE_ADMIN")
                .requestMatchers("/admin/fraud/**").hasAnyAuthority(
                        "admin", "fraud-admin", "fraud-supervisor", "fraud-analyst", "finance",
                        "ROLE_ADMIN", "ROLE_FRAUD_ADMIN", "ROLE_FRAUD_SUPERVISOR", "ROLE_FRAUD_ANALYST", "ROLE_FINANCE")
                .requestMatchers("/api/fraud/**").hasAnyAuthority(
                        "admin", "fraud-admin", "fraud-supervisor", "fraud-analyst", "finance",
                        "ROLE_ADMIN", "ROLE_FRAUD_ADMIN", "ROLE_FRAUD_SUPERVISOR", "ROLE_FRAUD_ANALYST", "ROLE_FINANCE")
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
                .logoutRequestMatcher(new AntPathRequestMatcher("/users/logout", "POST"))
                .logoutSuccessUrl("/public/member-login")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
                )
                .sessionManagement(session -> session
                .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                .maximumSessions(-1)
                .sessionRegistry(sessionRegistry)
                .expiredUrl("/public/member-login?sessionExpired=true"))
                .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31_536_000))
                .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .addFilterAfter(credentialVersionFilter, SecurityContextHolderFilter.class)
                .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

}
