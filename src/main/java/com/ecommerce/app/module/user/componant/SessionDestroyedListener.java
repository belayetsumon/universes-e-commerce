package com.ecommerce.app.module.user.componant;

import com.ecommerce.app.module.user.services.LoginEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
public class SessionDestroyedListener implements ApplicationListener<SessionDestroyedEvent> {

    @Autowired
    private LoginEventService loginEventService;

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        for (SecurityContext securityContext : event.getSecurityContexts()) {
            Authentication authentication = securityContext.getAuthentication();
            if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                continue;
            }

            loginEventService.logSessionExpired(authentication.getName(), event.getId());
        }
    }
}
