package com.epam.gymcrmspringboot.config;

import com.epam.gymcrmspringboot.model.LoginAttemptInfo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class LoginAttemptCacheConfig {

    private static final Duration LOGIN_ATTEMPT_TTL = Duration.ofHours(2);

    @Bean
    public Cache<String, LoginAttemptInfo> loginAttemptCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(LOGIN_ATTEMPT_TTL)
                .build();
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}

