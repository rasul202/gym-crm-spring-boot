package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    AuthenticationManager authenticationManager;
    JwtUtil jwtUtil;
    Environment environment;


    @Override
    public String authenticate(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        String token = jwtUtil.generateToken(authentication.getName(), authentication.getAuthorities());
        if (environment.acceptsProfiles(Profiles.of("local"))) {
            log.info("Local profile detected: generated JWT for testing. username={}, token={}", authentication.getName(), token);
        }
        return token;
    }


    public void assertAuthenticatedUser(String username, Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException("Authenticated user does not match path username");
        }
    }

}
