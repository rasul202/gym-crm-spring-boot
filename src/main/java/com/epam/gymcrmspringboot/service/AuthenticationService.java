package com.epam.gymcrmspringboot.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    String authenticate(@NotBlank(message = "Username must not be blank") String username, @NotBlank(message = "password must not be blank") String password);

    void assertAuthenticatedUser(String username, Authentication authentication);

}
