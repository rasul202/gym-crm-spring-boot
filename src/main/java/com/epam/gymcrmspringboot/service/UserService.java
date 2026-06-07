package com.epam.gymcrmspringboot.service;

import com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse;
import com.epam.gymcrmspringboot.dto.request.CreateUserRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;

public interface UserService {

    CreateUserProfileResponse createUserProfile(CreateUserRequest request);

    boolean authenticateActiveUser(LoginRequest request);

    boolean authenticateAnyUser(LoginRequest request);

    boolean deactivateUserProfile(String username);

    boolean activateUserProfile(String username);

    void changePassword(String username , String newPassword);

}