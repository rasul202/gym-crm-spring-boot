package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTraineeProfileRequest;
import com.epam.gymcrmspringboot.dto.response.*;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.mapper.TraineeMapper;
import com.epam.gymcrmspringboot.mapper.UserMapper;
import com.epam.gymcrmspringboot.model.*;
import com.epam.gymcrmspringboot.repository.TraineeRepository;
import com.epam.gymcrmspringboot.service.TraineeService;
import com.epam.gymcrmspringboot.service.TrainerService;
import com.epam.gymcrmspringboot.service.TrainingService;
import com.epam.gymcrmspringboot.service.UserService;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraineeServiceImpl.class);
    private static final String TRAINER_LINK_TRAINING_NAME_PREFIX = "Trainee-Trainer Link";
    private static final Integer TRAINER_LINK_DEFAULT_DURATION_MINUTES = 60;

    @Autowired
    private TraineeRepository traineeRepository;

    private TrainerService trainerService;
    private UserService userService;
    private RequestValidator requestValidator;
    private TraineeMapper traineeMapper;
    private UserMapper userMapper;
    private TrainingService trainingService;

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRequestValidator(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Autowired
    public void setTrainingService(TrainingService trainingService) {this.trainingService = trainingService;}

    private void authenticateActiveUser(String username, String password) {
        if (!userService.authenticateActiveUser(LoginRequest.builder().username(username).password(password).build())) {
            throw new AuthenticationException("Invalid username or password for trainee user profile: " + username);
        }
    }

    private void authenticateAnyUser(String username, String password) {
        if (!userService.authenticateAnyUser(LoginRequest.builder().username(username).password(password).build())) {
            throw new AuthenticationException("Invalid username or password for trainee user profile: " + username);
        }
    }

    @Override
    @Transactional
    public RegistrationResponse registerTrainee(CreateTraineeRequest request) {
        LOGGER.info("Register trainee operation has been started for firstName={} lastName={}",
                request == null ? null : request.getFirstName(),
                request == null ? null : request.getLastName());
        requestValidator.validate(request);

        CreateUserProfileResponse user = userService.createUserProfile(userMapper.toCreateUserRequest(request));
        if (user == null) {
            throw new IllegalStateException("Failed to create user profile for trainee");
        }

        TraineeEntity trainee = TraineeEntity.builder()
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress() != null ? request.getAddress().trim() : null)
                .user(UserEntity.builder().id(user.getId()).build())
                .build();

        traineeRepository.save(trainee);
        LOGGER.info("Registered Trainee profile username={}", user.getUsername());
        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public GetTraineeProfileResponse getTraineeByUsername(String username, String password) {
        LOGGER.info("Get trainee profile operation has been started for username={}", username);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or blank");
        }

        authenticateActiveUser(username, password);
        TraineeEntity trainee = getTraineeByUsername(username);

        LOGGER.debug("Fetched Trainee profile username={}", username);
        return traineeMapper.toGetTraineeProfileResponse(trainee);
    }

    @Override
    @Transactional
    public UpdateTraineeProfileResponse updateTrainee(
            String username,
            String password,
            UpdateTraineeProfileRequest request) {
        LOGGER.info("Update trainee operation has been started for username={}", username);
        requestValidator.validate(request);
        authenticateActiveUser(username, password);

        boolean hasFirstName = request.getFirstName() != null && !request.getFirstName().isBlank();
        boolean hasLastName = request.getLastName() != null && !request.getLastName().isBlank();
        boolean hasDateOfBirth = request.getDateOfBirth() != null;
        boolean hasAddress = request.getAddress() != null && !request.getAddress().isBlank();
        boolean hasIsActive = request.getIsActive() != null;

        if (!hasFirstName && !hasLastName && !hasDateOfBirth && !hasAddress && !hasIsActive) {
            throw new IllegalArgumentException("At least one updatable field must be provided for trainee update");
        }

        TraineeEntity trainee = getTraineeByUsername(username);
        UserEntity user = trainee.getUser();

        if (hasFirstName) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (hasLastName) {
            user.setLastName(request.getLastName().trim());
        }
        if (hasDateOfBirth) {
            trainee.setDateOfBirth(request.getDateOfBirth());
        }
        if (hasAddress) {
            trainee.setAddress(request.getAddress().trim());
        }
        if (hasIsActive) {
            if (request.getIsActive() == true) {
                userService.activateUserProfile(username);
            } else {
                userService.deactivateUserProfile(username);
            }
        }

        TraineeEntity updated = traineeRepository.save(trainee);

        LOGGER.info("Updated Trainee username={}", username);
        return traineeMapper.toUpdateTraineeProfileResponse(updated);
    }

    // Replaces all current trainee-trainer links by removing existing assignments
    // and creating new default assignment trainings for the provided trainers.
    @Override
    @Transactional
    public List<TrainerSummary> updateTraineeTrainers(String username, String password, List<String> trainerUsernames) {
        LOGGER.info("Update trainee trainers operation has been started for username={} trainerCount={}",
                username,
                trainerUsernames == null ? null : trainerUsernames.size());
        if (trainerUsernames == null || trainerUsernames.isEmpty()) {
            throw new IllegalArgumentException("trainerUsernames must contain at least one value");
        }

        authenticateActiveUser(username, password);
        TraineeEntity trainee = getTraineeByUsername(username);

        List<String> normalizedUsernames = trainerUsernames.stream()
                .map(trainerUsername -> trainerUsername == null ? null : trainerUsername.trim())
                .filter(trainerUsername -> trainerUsername != null && !trainerUsername.isBlank())
                .distinct()
                .toList();

        if (normalizedUsernames.isEmpty()) {
            throw new IllegalArgumentException("trainerUsernames must contain at least one non-blank value");
        }

        List<TrainerEntity> trainers = trainerService.getAllTrainersUsernamesIn(normalizedUsernames);
        Map<String, TrainerEntity> trainersByUsername = trainers.stream()
                .collect(Collectors.toMap(
                        trainer -> trainer.getUser().getUsername(),
                        Function.identity()));

        List<String> absentTrainers = normalizedUsernames.stream()
                .filter(trainerUsername -> !trainersByUsername.containsKey(trainerUsername))
                .toList();

        if (!absentTrainers.isEmpty()) {
            throw new EntityNotFoundException("Trainer(s) not found: " + String.join(", ", absentTrainers));
        }

        trainingService.deleteAllByTrainee(trainee);

        List<TrainingEntity> newTrainerAssignments = new ArrayList<>();
        LocalDate assignmentDate = LocalDate.now();

        for (TrainerEntity trainer : trainers) {
            TrainingTypeEntity trainingType = trainer.getSpecialization();
            if (trainingType == null) {
                throw new IllegalStateException("Trainer has no specialization assigned: " + trainer.getUser().getUsername());
            }

            newTrainerAssignments.add(TrainingEntity.builder()
                    .trainee(trainee)
                    .trainer(trainer)
                    .trainingName(TRAINER_LINK_TRAINING_NAME_PREFIX + " " + trainee.getUser().getUsername() + "-" + trainer.getUser().getUsername())
                    .trainingType(trainingType)
                    .trainingDate(assignmentDate)
                    .trainingDuration(TRAINER_LINK_DEFAULT_DURATION_MINUTES)
                    .build());
        }

        trainingService.saveAll(newTrainerAssignments);

        return normalizedUsernames.stream()
                .map(trainersByUsername::get)
                .map(traineeMapper::trainerEntityToTrainerSummary)
                .toList();
    }

    @Override
    @Transactional
    public void deactivateTrainee(String username, String password) {
        LOGGER.info("Deactivate trainee operation has been started for username={}", username);
        authenticateAnyUser(username, password);
        if (userService.deactivateUserProfile(username)) {
            LOGGER.info("Deactivated Trainee username={}", username);
        } else {
            throw new IllegalStateException("Trainee profile is already deactivated; username= " + username);
        }
    }

    @Override
    @Transactional
    public void activateTrainee(String username, String password) {
        LOGGER.info("Activate trainee operation has been started for username={}", username);
        authenticateAnyUser(username, password);
        if (userService.activateUserProfile(username)){
            LOGGER.info("Activated Trainee username={}", username);
        }else {
            throw new IllegalStateException("Trainee profile is already active; username= " + username);
        }
    }

    @Override
    @Transactional
    public void deleteTrainee(String username, String password) {
        LOGGER.info("Delete trainee operation has been started for username={}", username);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }

        authenticateActiveUser(username, password);

        TraineeEntity trainee = getTraineeByUsernameWithTrainings(username);
        traineeRepository.delete(trainee);

        LOGGER.info("Deleted Trainee username={}", username);
    }

    @Override
    public TraineeEntity getTraineeByUsername(String username) {
        return traineeRepository.findByUserUsernameAndUserIsActiveTrue(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
    }

    @Override
    public List<TrainerSummary> getAvailableTrainersForTrainee(String traineeUsername, String password) {
        return trainerService.getAvailableTrainersForTrainee(traineeUsername, password);
    }

    private TraineeEntity getTraineeByUsernameWithTrainings(String username) {
        return traineeRepository.findByUserUsernameAndUserIsActiveTrueWithTrainings(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
    }
}
