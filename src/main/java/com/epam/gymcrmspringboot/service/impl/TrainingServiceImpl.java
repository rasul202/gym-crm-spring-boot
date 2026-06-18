package com.epam.gymcrmspringboot.service.impl;


import com.epam.gymcrmspringboot.dto.request.GetTraineeTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.request.GetTrainerTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.request.AddTrainingRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.GetTrainerTrainingsResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.mapper.TrainingMapper;
import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.TrainingEntity;
import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import com.epam.gymcrmspringboot.repository.TrainingCriteriaRepository;
import com.epam.gymcrmspringboot.repository.TrainingRepository;
import com.epam.gymcrmspringboot.service.*;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class TrainingServiceImpl implements TrainingService {

    static Logger LOGGER = LoggerFactory.getLogger(TrainingServiceImpl.class);

    TrainingRepository trainingRepository;
    TrainingCriteriaRepository trainingCriteriaRepository;
    UserService userService;
    TrainingMapper trainingMapper;
    RequestValidator requestValidator;
    TrainingTypeService trainingTypeService;
    TrainerService trainerService;
    TraineeService traineeService;

    public TrainingServiceImpl(
            TrainingRepository trainingRepository,
            TrainingCriteriaRepository trainingCriteriaRepository,
            UserService userService,
            TrainingMapper trainingMapper,
            RequestValidator requestValidator,
            TrainingTypeService trainingTypeService,
            TrainerService trainerService,
            @Lazy TraineeService traineeService) {
        this.trainingRepository = trainingRepository;
        this.trainingCriteriaRepository = trainingCriteriaRepository;
        this.userService = userService;
        this.trainingMapper = trainingMapper;
        this.requestValidator = requestValidator;
        this.trainingTypeService = trainingTypeService;
        this.trainerService = trainerService;
        this.traineeService = traineeService;
    }

    private void authenticate(String username, String password) {
        if (!userService.authenticateActiveUser(LoginRequest.builder().username(username).password(password).build())) {
            throw new AuthenticationException("Invalid username or password for user profile: " + username);
        }
    }


    @Override
    @Transactional
    public void addTraining(AddTrainingRequest request, String trainerPassword) {
        LOGGER.info("Add training operation has been started for trainerUsername={} traineeUsername={}",
                request == null ? null : request.getTrainerUsername(),
                request == null ? null : request.getTraineeUsername());

        requestValidator.validate(request);
        authenticate(request.getTrainerUsername(), trainerPassword);

        TraineeEntity trainee = traineeService.getTraineeByUsername(request.getTraineeUsername());

        TrainerEntity trainer = trainerService.getTrainerByUsername(request.getTrainerUsername());

        String trainingTypeName = trainer.getSpecialization() != null
                ? trainer.getSpecialization().getTrainingTypeName()
                : null;

        if (trainingTypeName == null) {
            throw new IllegalArgumentException("Trainer has no specialization assigned");
        }

        TrainingTypeEntity trainingType = trainingTypeService.getTrainingTypeByName(trainingTypeName.trim());

        TrainingEntity training = TrainingEntity.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName().trim())
                .trainingType(trainingType)
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .build();

        TrainingEntity saved = trainingRepository.save(training);
        LOGGER.info("Created training id={}", saved.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetTraineeTrainingsResponse> getTraineeTrainings(String traineeUsername , String password, GetTraineeTrainingsCriteriaRequest criteriaRequest) {
        LOGGER.info("Get trainee trainings operation has been started for traineeUsername={}", traineeUsername);

        requestValidator.validate(criteriaRequest);
        authenticate(traineeUsername, password);

        List<TrainingEntity> trainings = trainingCriteriaRepository.findTraineeTrainings(
                        traineeUsername,
                        criteriaRequest.getFromDate(),
                        criteriaRequest.getToDate(),
                        criteriaRequest.getTrainingType(),
                        criteriaRequest.getTrainerName() != null ? criteriaRequest.getTrainerName().trim() : null);


        return trainings.stream()
                .map(trainingMapper::toGetTraineeTrainingsResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetTrainerTrainingsResponse> getTrainerTrainings(String trainerUsername , String password , GetTrainerTrainingsCriteriaRequest criteriaRequest) {
        LOGGER.info("Get trainer trainings operation has been started for trainerUsername={}", trainerUsername);
        requestValidator.validate(criteriaRequest);
        authenticate(trainerUsername, password);

        return trainingCriteriaRepository.findTrainerTrainings(
                        trainerUsername,
                        criteriaRequest.getFromDate(),
                        criteriaRequest.getToDate(),
                        criteriaRequest.getTraineeName() != null ? criteriaRequest.getTraineeName().trim() : null)
                .stream()
                .map(trainingMapper::toGetTrainerTrainingsResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllByTrainee(TraineeEntity trainee) {
        trainingRepository.deleteByTrainee(trainee);
    }

    @Override
    public void saveAll(List<TrainingEntity> newTrainerAssignments) {
        trainingRepository.saveAll(newTrainerAssignments);
    }

}
