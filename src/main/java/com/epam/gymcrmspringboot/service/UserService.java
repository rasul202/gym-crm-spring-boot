package com.epam.gymcrmspringboot.service;

import com.epam.gymcrmspringboot.dto.request.ChangePasswordRequest;
import com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse;
import com.epam.gymcrmspringboot.dto.request.CreateUserRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import org.springframework.security.core.Authentication;

public interface UserService {

    CreateUserProfileResponse createUserProfile(CreateUserRequest request);

    boolean deactivateUserProfile(String username);

    boolean activateUserProfile(String username);

    void changePassword(ChangePasswordRequest request, Authentication authentication);

}