package com.epam.gymcrmspringboot.listener;

import com.epam.gymcrmspringboot.service.LoginAttemptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptEventListener Tests")
class LoginAttemptEventListenerTest {

    @Mock
    private LoginAttemptService loginAttemptService;

    @Test
    @DisplayName("Should clear login attempts after authentication success")
    void shouldClearAttemptsOnSuccess() {
        LoginAttemptEventListener listener = new LoginAttemptEventListener(loginAttemptService);
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                new UsernamePasswordAuthenticationToken("John.Doe", "password"));

        listener.onAuthenticationSuccess(event);

        verify(loginAttemptService).loginSucceeded("John.Doe");
    }

    @Test
    @DisplayName("Should record failed login attempts on bad credentials")
    void shouldRecordFailureOnBadCredentials() {
        LoginAttemptEventListener listener = new LoginAttemptEventListener(loginAttemptService);
        AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("John.Doe", "password"),
                new BadCredentialsException("Bad credentials"));

        listener.onAuthenticationFailure(event);

        verify(loginAttemptService).loginFailed("John.Doe");
    }
}

