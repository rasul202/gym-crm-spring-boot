package com.epam.gymcrmspringboot.util;

import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private static final String ROLES_CLAIM = "roles";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        List<String> roleNames = authorities == null ? List.of() : authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .toList();
        if (roleNames.isEmpty()) {
            throw new IllegalArgumentException("Cannot generate JWT without at least one role");
        }

        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim(ROLES_CLAIM, roleNames)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        List<?> rawRoles = extractAllClaims(token).get(ROLES_CLAIM, List.class);
        if (rawRoles == null || rawRoles.isEmpty()) {
            return List.of();
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Object rawRole : rawRoles) {
            if (rawRole instanceof String role && !role.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }
        return authorities;
    }

    public boolean isTokenValid(String token) {
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();
        List<?> rawRoles = claims.get(ROLES_CLAIM, List.class);
        return username != null
                && !username.isBlank()
                && rawRoles != null
                && !rawRoles.isEmpty()
                && !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}

