package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.service.JwtTokenRevocationService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtTokenRevocationServiceImpl implements JwtTokenRevocationService {

    Cache<String, Boolean> revokedTokens;

    public JwtTokenRevocationServiceImpl(@Value("${security.jwt.expiration-ms}") long expirationMs) {
        this.revokedTokens = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMillis(expirationMs))
                .build();
    }

    @Override
    public void revokeToken(String token) {
        revokedTokens.put(token, Boolean.TRUE);
    }

    @Override
    public boolean isTokenRevoked(String token) {
        Boolean result = revokedTokens.getIfPresent(token);
        return Boolean.TRUE.equals(result);
    }
}
