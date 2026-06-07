package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.dto.request.CreateUserRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.SamePasswordException;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.UserRepository;
import com.epam.gymcrmspringboot.service.UserService;
import com.epam.gymcrmspringboot.util.UsernamePasswordUtil;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UsernamePasswordUtil usernamePasswordUtil;
    private RequestValidator requestValidator;

    @Autowired
    public void setUsernamePasswordUtil(UsernamePasswordUtil usernamePasswordUtil) {this.usernamePasswordUtil = usernamePasswordUtil;}

    @Autowired
    public void setRequestValidator(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {this.passwordEncoder = passwordEncoder;}

    @Override
    @Transactional
    public CreateUserProfileResponse createUserProfile(CreateUserRequest request) {
        LOGGER.info("Create user profile operation has been started for firstName={} lastName={}",
                request == null ? null : request.getFirstName(),
                request == null ? null : request.getLastName());
        requestValidator.validate(request);

        Set<String> existingUsernames = userRepository.findAll().stream()
                .map(UserEntity::getUsername)
                .collect(Collectors.toSet());

        String username = usernamePasswordUtil.generateUsername(
                request.getFirstName().trim(),
                request.getLastName().trim(),
                existingUsernames);
        String rawPassword = usernamePasswordUtil.generatePassword();

        UserEntity user = UserEntity.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
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
    public boolean authenticateActiveUser(LoginRequest request) {
        LOGGER.info("Authenticate active user operation has been started for username={}",
                request == null ? null : request.getUsername());
        requestValidator.validate(request);

        Optional<UserEntity> optionalUser = userRepository.findByUsernameAndIsActiveTrue(request.getUsername());
        return authenticate(optionalUser , request);
    }

    @Override
    public boolean authenticateAnyUser(LoginRequest request) {
        LOGGER.info("Authenticate any user operation has been started for username={}",
                request == null ? null : request.getUsername());
        requestValidator.validate(request);

        Optional<UserEntity> optionalUser = userRepository.findByUsername(request.getUsername());
        return authenticate(optionalUser , request);
    }

    private boolean authenticate(Optional<UserEntity> persistentUser , LoginRequest request){
        if (persistentUser.isEmpty()) {
            LOGGER.warn("Authentication failed because user was not found username={}", request == null ? null : request.getUsername());
            throw new EntityNotFoundException("User not found: " + request.getUsername());
        }
        boolean matched = persistentUser
                .map(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .orElse(false);
        LOGGER.info("Authenticate any user operation has been completed for username={} , Authenticated={}",
                request.getUsername(),
                matched);
        return matched;
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
                    if (user.getIsActive() == false) {
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
                    if (user.getIsActive() == true) {
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
    public void changePassword(String username, String newPassword) {
        LOGGER.info("Change password operation has been started for username={}", username);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword must not be blank");
        }

        UserEntity user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException("New password cannot be same as the old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        LOGGER.info("Change password operation has been completed for username={}", username);
    }
}
