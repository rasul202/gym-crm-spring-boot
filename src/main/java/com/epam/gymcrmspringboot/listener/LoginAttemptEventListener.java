package com.epam.gymcrmspringboot.listener;

import com.epam.gymcrmspringboot.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginAttemptEventListener {

    private final LoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication() != null ? event.getAuthentication().getName() : null;
        loginAttemptService.loginSucceeded(username);
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication() != null ? event.getAuthentication().getName() : null;
        loginAttemptService.loginFailed(username);
    }
}

