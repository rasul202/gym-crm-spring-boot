package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.model.LoginAttemptInfo;
import com.epam.gymcrmspringboot.service.impl.LoginAttemptServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginAttemptServiceImpl Tests")
class LoginAttemptServiceImplTest {

    private static final String USERNAME = "John.Doe";

    private MutableClock clock;
    private MutableTicker ticker;
    private Cache<String, LoginAttemptInfo> cache;
    private LoginAttemptServiceImpl service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-06-20T10:00:00Z"), ZoneOffset.UTC);
        ticker = new MutableTicker();
        cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(2))
                .ticker(ticker)
                .build();
        service = new LoginAttemptServiceImpl(cache, clock);
    }

    @Test
    @DisplayName("Should block after third failure and clear the cache on success")
    void shouldBlockAfterThirdFailureAndClearOnSuccess() {
        service.loginFailed(USERNAME);
        assertEquals(1, service.getFailedAttempts(USERNAME));
        assertFalse(service.isBlocked(USERNAME));

        service.loginFailed(USERNAME);
        assertEquals(2, service.getFailedAttempts(USERNAME));
        assertFalse(service.isBlocked(USERNAME));

        service.loginFailed(USERNAME);
        assertEquals(3, service.getFailedAttempts(USERNAME));
        assertTrue(service.isBlocked(USERNAME));

        LoginAttemptInfo attemptInfo = cache.getIfPresent(USERNAME);
        assertNotNull(attemptInfo);
        assertNotNull(attemptInfo.getBlockedUntil());
        assertTrue(attemptInfo.getBlockedUntil().isAfter(LocalDateTime.now(clock)));

        service.loginSucceeded(USERNAME);
        assertEquals(0, service.getFailedAttempts(USERNAME));
        assertFalse(service.isBlocked(USERNAME));
        assertNull(cache.getIfPresent(USERNAME));
    }

    @Test
    @DisplayName("Should expire failed attempt history after two hours of inactivity")
    void shouldExpireFailedAttemptsAfterTwoHoursOfInactivity() {
        service.loginFailed(USERNAME);
        service.loginFailed(USERNAME);
        assertEquals(2, service.getFailedAttempts(USERNAME));

        advance(Duration.ofHours(2).plusSeconds(1));
        cache.cleanUp();

        assertEquals(0, service.getFailedAttempts(USERNAME));
        assertFalse(service.isBlocked(USERNAME));
        assertNull(cache.getIfPresent(USERNAME));
    }

    @Test
    @DisplayName("Should release blocked user after five minutes and reset the counter")
    void shouldReleaseBlockedUserAfterBlockExpires() {
        service.loginFailed(USERNAME);
        service.loginFailed(USERNAME);
        service.loginFailed(USERNAME);

        assertTrue(service.isBlocked(USERNAME));

        advance(Duration.ofMinutes(5).plusSeconds(1));
        cache.cleanUp();

        assertFalse(service.isBlocked(USERNAME));
        assertEquals(0, service.getFailedAttempts(USERNAME));

        service.loginFailed(USERNAME);
        assertEquals(1, service.getFailedAttempts(USERNAME));
        assertFalse(service.isBlocked(USERNAME));
    }

    @Test
    @DisplayName("Should remain blocked while block duration has not expired")
    void shouldRemainBlockedBeforeBlockExpires() {
        service.loginFailed(USERNAME);
        service.loginFailed(USERNAME);
        service.loginFailed(USERNAME);

        assertTrue(service.isBlocked(USERNAME));

        advance(Duration.ofMinutes(4));
        cache.cleanUp();

        assertTrue(service.isBlocked(USERNAME));
    }

    private void advance(Duration duration) {
        clock.advance(duration);
        ticker.advance(duration);
    }

    private static final class MutableTicker implements Ticker {
        private long nanos;

        @Override
        public long read() {
            return nanos;
        }

        void advance(Duration duration) {
            nanos += duration.toNanos();
        }
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}

