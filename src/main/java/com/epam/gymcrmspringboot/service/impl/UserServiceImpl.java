package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.dto.request.ChangePasswordRequest;
import com.epam.gymcrmspringboot.dto.request.CreateUserRequest;
import com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.SamePasswordException;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.UserRepository;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.service.UserService;
import com.epam.gymcrmspringboot.util.UsernamePasswordUtil;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UsernamePasswordUtil usernamePasswordUtil;
    RequestValidator requestValidator;
    AuthenticationService authenticationService;

    @Override
    @Transactional
    public CreateUserProfileResponse createUserProfile(CreateUserRequest request) {
        LOGGER.info("Create user profile operation has been started for firstName={} lastName={}",
                request == null ? null : request.getFirstName(),
                request == null ? null : request.getLastName());
        requestValidator.validate(request);

        String firstName = request.getFirstName().trim();
        String lastName = request.getLastName().trim();
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + suffix;
            suffix++;
        }
        String rawPassword = usernamePasswordUtil.generatePassword();

        UserEntity user = UserEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .build();

        UserEntity saved = userRepository.save(user);
        LOGGER.info("Created User profile id={}, username={}", saved.getId(), username);
        CreateUserProfileResponse response = new CreateUserProfileResponse(saved.getId(), saved.getUsername() , rawPassword);
        LOGGER.info("Create user profile operation has been completed for userId={} username={}",
                response.getId(),
                response.getUsername());
        return response;
    }

    @Override
    @Transactional
    public boolean deactivateUserProfile(String username) {
        LOGGER.info("Deactivate user profile operation has been started for username={}", username);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (!Boolean.TRUE.equals(user.getIsActive())) {
                        LOGGER.info("Deactivate user profile operation skipped because user is already inactive username={}", username);
                        return false;
                    }
                    user.setIsActive(false);
                    userRepository.save(user);
                    LOGGER.info("Deactivate user profile operation has been completed for username={}", username);
                    return true;
                })
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    @Override
    @Transactional
    public boolean activateUserProfile(String username) {
        LOGGER.info("Activate user profile operation has been started for username={}", username);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (Boolean.TRUE.equals(user.getIsActive())) {
                        LOGGER.info("Activate user profile operation skipped because user is already active username={}", username);
                        return false;
                    }
                    user.setIsActive(true);
                    userRepository.save(user);
                    LOGGER.info("Activate user profile operation has been completed for username={}", username);
                    return true;
                })
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, Authentication authentication) {
        LOGGER.info("Change password operation has been started for username={}",
                request == null ? null : request.getUsername());
        requestValidator.validate(request);

        String username = request.getUsername().trim();
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        authenticationService.assertAuthenticatedUser(username, authentication);

        if (oldPassword.equals(newPassword)) {
            throw new SamePasswordException("New password cannot be same as the old password");
        }

        UserEntity user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid current password for user: " + username);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        LOGGER.info("Change password operation has been completed for username={}", username);
    }
}
