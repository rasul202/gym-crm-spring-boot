package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTrainerProfileRequest;
import com.epam.gymcrmspringboot.dto.response.*;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.mapper.TrainerMapper;
import com.epam.gymcrmspringboot.mapper.UserMapper;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.TrainerRepository;
import com.epam.gymcrmspringboot.service.TrainerService;
import com.epam.gymcrmspringboot.service.TrainingTypeService;
import com.epam.gymcrmspringboot.service.UserService;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import com.epam.gymcrmspringboot.validation.TrainerTraineeRegistrationValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class TrainerServiceImpl implements TrainerService {

    static Logger LOGGER = LoggerFactory.getLogger(TrainerServiceImpl.class);

    TrainerRepository trainerRepository;
    UserService userService;
    RequestValidator requestValidator;
    TrainerMapper trainerMapper;
    UserMapper userMapper;
    TrainingTypeService trainingTypeService;
    TrainerTraineeRegistrationValidator trainerTraineeRegistrationValidator;

    private void authenticateActiveUser(String username, String password) {
        if (!userService.authenticateActiveUser(LoginRequest.builder().username(username).password(password).build())) {
            throw new AuthenticationException("Invalid username or password for trainer user profile: " + username);
        }
    }

    private void authenticateAnyUser(String username, String password) {
        if (!userService.authenticateAnyUser(LoginRequest.builder().username(username).password(password).build())) {
            throw new AuthenticationException("Invalid username or password for trainer user profile: " + username);
        }
    }

    @Override
    @Transactional
    public RegistrationResponse registerTrainer(CreateTrainerRequest request) {
        LOGGER.info("Register trainer operation has been started for firstName={} lastName={} specialization={}",
                request == null ? null : request.getFirstName(),
                request == null ? null : request.getLastName(),
                request == null ? null : request.getSpecialization());
        requestValidator.validate(request);
        trainerTraineeRegistrationValidator.validateTrainerRegistration(request);

        TrainingTypeEntity specialization = trainingTypeService.getTrainingTypeByName(request.getSpecialization().trim());
        CreateUserProfileResponse user = userService.createUserProfile(userMapper.toCreateUserRequest(request));
        if (user == null) {
            throw new IllegalStateException("Failed to create user profile for trainer");
        }

        TrainerEntity trainer = TrainerEntity.builder()
                .specialization(specialization)
                .user(UserEntity.builder().id(user.getId()).build())
                .build();
        trainerRepository.save(trainer);

        LOGGER.info("Registered Trainer profile username={}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    @Override
    @Transactional
    public UpdateTrainerProfileResponse updateTrainer(String username, String password, UpdateTrainerProfileRequest request) {
        LOGGER.info("Update trainer operation has been started for username={}", username);
        requestValidator.validate(request);
        authenticateActiveUser(username, password);

        boolean hasFirstName = request.getFirstName() != null && !request.getFirstName().isBlank();
        boolean hasLastName = request.getLastName() != null && !request.getLastName().isBlank();
        boolean hasIsActive = request.getIsActive() != null;

        TrainerEntity trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));

        UserEntity user = trainer.getUser();
        if (hasFirstName) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (hasLastName) {
            user.setLastName(request.getLastName().trim());
        }
        if (hasIsActive) {
            if (request.getIsActive() == true) {
                userService.activateUserProfile(username);
            } else {
                userService.deactivateUserProfile(username);
            }
        }

        TrainerEntity updated = trainerRepository.save(trainer);
        LOGGER.info("Updated Trainer username={}", username);
        return trainerMapper.toUpdateTrainerProfileResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public GetTrainerProfileResponse getTrainerByUsername(String username, String password) {
        LOGGER.info("Get trainer profile operation has been started for username={}", username);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be blank or null");
        }

        authenticateActiveUser(username, password);
        TrainerEntity trainer = getTrainerByUsername(username);
        LOGGER.debug("Fetched Trainer profile username={}", username);
        return trainerMapper.toGetTrainerProfileResponse(trainer);
    }

    @Override
    @Transactional
    public void deactivateTrainer(String username, String password) {
        LOGGER.info("Deactivate trainer operation has been started for username={}", username);
        authenticateAnyUser(username, password);
        if (userService.deactivateUserProfile(username)) {
            LOGGER.info("Deactivated Trainer username={}", username);
        } else {
            throw new IllegalStateException("Trainer profile is already deactivated; username= " + username);
        }
    }

    @Override
    @Transactional
    public void activateTrainer(String username, String password) {
        LOGGER.info("Activate trainer operation has been started for username={}", username);
        authenticateAnyUser(username, password);
        if (userService.activateUserProfile(username)) {
            LOGGER.info("Activated Trainer username={}", username);
        } else {
            throw new IllegalStateException("Trainer profile is already active; username= " + username);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<TrainerSummary> getAvailableTrainersForTrainee(String traineeUsername, String password) {
        LOGGER.info("Get available trainers operation has been started for traineeUsername={}", traineeUsername);
        if (traineeUsername == null || traineeUsername.isBlank()) {
            throw new IllegalArgumentException("traineeUsername must not be blank");
        }

        authenticateActiveUser(traineeUsername, password);
        return trainerRepository.findAvailableTrainersForTrainee(traineeUsername).stream()
                .map(trainerMapper::trainerEntityToTrainerSummary)
                .toList();
    }

    @Override
    public List<TrainerEntity> getAllTrainersUsernamesIn(List<String> usernames) {
        return trainerRepository.findByUserUsernameInAndUserIsActiveTrue(usernames);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRegisteredAsTrainer(String firstName, String lastName) {
        boolean registered = trainerRepository.existsByUserFirstNameIgnoreCaseAndUserLastNameIgnoreCase(
                firstName.trim(),
                lastName.trim());
        LOGGER.debug("Trainer existence check by full name firstName={} lastName={} registered={}",
                firstName,
                lastName,
                registered);
        return registered;
    }

    @Override
    public TrainerEntity getTrainerByUsername(String username) {
        TrainerEntity trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
        LOGGER.debug("Get trainer entity by username operation has been completed for username={} trainerId={}",
                username,
                trainer.getId());
        return trainer;
    }
}
