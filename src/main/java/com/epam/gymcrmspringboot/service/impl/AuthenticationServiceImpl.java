package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.util.JwtUtil;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    final AuthenticationManager authenticationManager;
    final JwtUtil jwtUtil;

    public AuthenticationServiceImpl(@Lazy AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String authenticate(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        String token = jwtUtil.generateToken(authentication.getName(), authentication.getAuthorities());
        return token;
    }


    public void assertAuthenticatedUser(String username, Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException("Authenticated user does not match path username");
        }
    }

    public String extractTokenFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = authorizationHeader.substring(7).trim();
        return token.isBlank() ? null : token;
    }

}
