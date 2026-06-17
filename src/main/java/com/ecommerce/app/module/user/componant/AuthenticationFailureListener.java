package com.ecommerce.app.module.user.componant;

import com.ecommerce.app.module.user.services.LoginEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    @Autowired
    private LoginEventService loginEventService;

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        Authentication authentication = event.getAuthentication();
        String attemptedUsername = authentication == null ? null : authentication.getName();
        loginEventService.logFailedLogin(attemptedUsername, resolveFailureReason(event.getException()));
    }

    private String resolveFailureReason(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return "Bad credentials";
        }
        if (exception instanceof UsernameNotFoundException) {
            return "User not found";
        }
        if (exception instanceof DisabledException) {
            return "Account disabled";
        }
        if (exception instanceof LockedException) {
            return "Account locked";
        }
        if (exception instanceof CredentialsExpiredException) {
            return "Credentials expired";
        }
        if (exception instanceof AccountExpiredException) {
            return "Account expired";
        }

        return exception == null ? "Authentication failed" : exception.getMessage();
    }
}
