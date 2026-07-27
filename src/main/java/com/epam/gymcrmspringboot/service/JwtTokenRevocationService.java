package com.epam.gymcrmspringboot.service;

public interface JwtTokenRevocationService {

    void revokeToken(String token);

    boolean isTokenRevoked(String token);
}
