package com.epam.gymcrmspringboot.config;

import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.service.JwtTokenRevocationService;
import com.epam.gymcrmspringboot.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationService authenticationService;
    private final JwtTokenRevocationService jwtTokenRevocationService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            @Lazy AuthenticationService authenticationService,
            JwtTokenRevocationService jwtTokenRevocationService) {
        this.jwtUtil = jwtUtil;
        this.authenticationService = authenticationService;
        this.jwtTokenRevocationService = jwtTokenRevocationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = authenticationService.extractTokenFromAuthorizationHeader(request.getHeader("Authorization"));
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (jwtTokenRevocationService.isTokenRevoked(token)) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtUtil.extractUsername(token);
            List<GrantedAuthority> authorities = jwtUtil.extractAuthorities(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.isTokenValid(token)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

}
