package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.service.impl.AuthenticationServiceImpl;
import com.epam.gymcrmspringboot.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl Tests")
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Environment environment;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    @DisplayName("Should authenticate and return generated JWT")
    void shouldAuthenticateAndGenerateToken() {
        Authentication authentication = mock(Authentication.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_TRAINEE"));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("john.doe");
        when(authentication.getAuthorities()).thenReturn((List) authorities);
        when(jwtUtil.generateToken("john.doe", authorities)).thenReturn("jwt-token");
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        String token = authenticationService.authenticate("john.doe", "pass");

        assertEquals("jwt-token", token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("john.doe", authorities);
    }

    @Test
    @DisplayName("Should allow access when authenticated user matches username")
    void shouldAllowWhenAuthenticatedUserMatches() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("john.doe");

        authenticationService.assertAuthenticatedUser("john.doe", authentication);

        verify(authentication).getName();
    }

    @Test
    @DisplayName("Should deny access when authenticated user does not match username")
    void shouldDenyWhenAuthenticatedUserDoesNotMatch() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("john.doe");

        assertThrows(AccessDeniedException.class,
                () -> authenticationService.assertAuthenticatedUser("jane.doe", authentication));
    }
}


