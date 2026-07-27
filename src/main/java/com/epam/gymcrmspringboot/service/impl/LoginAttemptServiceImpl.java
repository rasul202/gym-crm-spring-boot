package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.model.LoginAttemptInfo;
import com.epam.gymcrmspringboot.service.LoginAttemptService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private final Cache<String, LoginAttemptInfo> loginAttemptCache;
    private final Clock clock;

    @Override
    public void loginSucceeded(String username) {
        if (hasText(username)) {
            loginAttemptCache.invalidate(username);
        }
    }

    @Override
    public void loginFailed(String username) {
        if (!hasText(username)) {
            return;
        }

        LocalDateTime now = now();
        loginAttemptCache.asMap().compute(username, (key, current) -> {
            LoginAttemptInfo entry = current != null ? current : new LoginAttemptInfo();
            normalizeExpiredBlock(entry, now);

            if (isBlockActive(entry, now)) {
                return entry;
            }

            int failedAttempts = entry.getFailedAttempts() + 1;
            entry.setFailedAttempts(failedAttempts);
            entry.setBlockedUntil(failedAttempts >= MAX_FAILED_ATTEMPTS ? now.plus(BLOCK_DURATION) : null);
            return entry;
        });
    }

    @Override
    public boolean isBlocked(String username) {
        if (!hasText(username)) {
            return false;
        }

        LocalDateTime now = now();
        AtomicBoolean blocked = new AtomicBoolean(false);
        loginAttemptCache.asMap().computeIfPresent(username, (key, entry) -> {
            normalizeExpiredBlock(entry, now);
            if (isBlockActive(entry, now)) {
                blocked.set(true);
            }
            return entry;
        });
        return blocked.get();
    }

    @Override
    public int getFailedAttempts(String username) {
        if (!hasText(username)) {
            return 0;
        }

        LocalDateTime now = now();
        AtomicInteger failedAttempts = new AtomicInteger(0);
        loginAttemptCache.asMap().computeIfPresent(username, (key, entry) -> {
            normalizeExpiredBlock(entry, now);
            failedAttempts.set(entry.getFailedAttempts());
            return entry;
        });
        return failedAttempts.get();
    }



    private void normalizeExpiredBlock(LoginAttemptInfo entry, LocalDateTime now) {
        if (entry.getBlockedUntil() != null && !entry.getBlockedUntil().isAfter(now)) {
            entry.setBlockedUntil(null);
            entry.setFailedAttempts(0);
        }
    }

    private boolean isBlockActive(LoginAttemptInfo entry, LocalDateTime now) {
        return entry.getBlockedUntil() != null && entry.getBlockedUntil().isAfter(now);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

