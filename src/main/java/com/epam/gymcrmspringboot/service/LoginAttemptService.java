package com.epam.gymcrmspringboot.service;

public interface LoginAttemptService {

    void loginSucceeded(String username);

    void loginFailed(String username);

    boolean isBlocked(String username);

    int getFailedAttempts(String username);

}

