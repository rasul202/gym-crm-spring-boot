package com.epam.gymcrmspringboot.service.impl;


import com.epam.gymcrmspringboot.dto.request.GetTraineeTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.request.GetTrainerTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.request.AddTrainingRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.GetTrainerTrainingsResponse;
import com.epam.gymcrmspringboot.mapper.TrainingMapper;
import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.TrainingEntity;
import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import com.epam.gymcrmspringboot.repository.TrainingCriteriaRepository;
import com.epam.gymcrmspringboot.repository.TrainingRepository;
import com.epam.gymcrmspringboot.service.*;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class TrainingServiceImpl implements TrainingService {

    static Logger LOGGER = LoggerFactory.getLogger(TrainingServiceImpl.class);

    TrainingRepository trainingRepository;
    TrainingCriteriaRepository trainingCriteriaRepository;
    AuthenticationService authenticationService;
    TrainingMapper trainingMapper;
    RequestValidator requestValidator;
    TrainingTypeService trainingTypeService;
    TrainerService trainerService;
    TraineeService traineeService;
    WorkloadClientServiceImpl workloadClientServiceImpl;

    public TrainingServiceImpl(
            TrainingRepository trainingRepository,
            TrainingCriteriaRepository trainingCriteriaRepository,
            AuthenticationService authenticationService,
            TrainingMapper trainingMapper,
            RequestValidator requestValidator,
            TrainingTypeService trainingTypeService,
            TrainerService trainerService,
            @Lazy TraineeService traineeService,
            WorkloadClientServiceImpl workloadClientServiceImpl) {
        this.trainingRepository = trainingRepository;
        this.trainingCriteriaRepository = trainingCriteriaRepository;
        this.authenticationService = authenticationService;
        this.trainingMapper = trainingMapper;
        this.requestValidator = requestValidator;
        this.trainingTypeService = trainingTypeService;
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.workloadClientServiceImpl = workloadClientServiceImpl;
    }

    @Override
    @Transactional
    public void addTraining(AddTrainingRequest request, Authentication authentication) {
        LOGGER.info("Add training operation has been started for trainerUsername={} traineeUsername={}",
                request == null ? null : request.getTrainerUsername(),
                request == null ? null : request.getTraineeUsername());

        requestValidator.validate(request);
        authenticationService.assertAuthenticatedUser(request.getTrainerUsername(), authentication);

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

        // Capture values into local variables — safe from lazy-loading issues post-transaction
        String trainerUsername = trainer.getUser().getUsername();
        String trainerFirstName = trainer.getUser().getFirstName();
        String trainerLastName = trainer.getUser().getLastName();
        Boolean trainerIsActive = trainer.getUser().getIsActive();
        LocalDate trainingDate = saved.getTrainingDate();
        Integer trainingDuration = saved.getTrainingDuration();

        executeAfterCommit(
                () -> workloadClientServiceImpl.notifyWorkloadAdd(
                        trainerUsername,
                        trainerFirstName,
                        trainerLastName,
                        trainerIsActive,
                        trainingDate,
                        trainingDuration
                ),
                String.format("ADD training id=%d, trainerUsername=%s", saved.getId(), trainerUsername)
        );
    }

    @Override
    @Transactional
    public void deleteTraining(Long trainingId, Authentication authentication) {
        LOGGER.info("Delete training operation has been started for trainingId={}", trainingId);

        TrainingEntity training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new IllegalArgumentException("Training not found with id: " + trainingId));

        TrainerEntity trainer = training.getTrainer();
        authenticationService.assertAuthenticatedUser(trainer.getUser().getUsername(), authentication);

        // Capture all values BEFORE delete — the entity may become detached/inaccessible after removal
        String trainerUsername = trainer.getUser().getUsername();
        String trainerFirstName = trainer.getUser().getFirstName();
        String trainerLastName = trainer.getUser().getLastName();
        Boolean trainerIsActive = trainer.getUser().getIsActive();
        LocalDate trainingDate = training.getTrainingDate();
        Integer trainingDuration = training.getTrainingDuration();

        trainingRepository.deleteById(trainingId);
        LOGGER.info("Deleted training id={}", trainingId);

        // Send notification ONLY after the transaction commits successfully
        executeAfterCommit(
                () -> workloadClientServiceImpl.notifyWorkloadDelete(
                        trainerUsername,
                        trainerFirstName,
                        trainerLastName,
                        trainerIsActive,
                        trainingDate,
                        trainingDuration
                ),
                String.format("DELETE training id=%d, trainerUsername=%s", trainingId, trainerUsername)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetTraineeTrainingsResponse> getTraineeTrainings(String traineeUsername , Authentication authentication, GetTraineeTrainingsCriteriaRequest criteriaRequest) {
        LOGGER.info("Get trainee trainings operation has been started for traineeUsername={}", traineeUsername);

        requestValidator.validate(criteriaRequest);
        authenticationService.assertAuthenticatedUser(traineeUsername, authentication);

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
    public List<GetTrainerTrainingsResponse> getTrainerTrainings(String trainerUsername , Authentication authentication , GetTrainerTrainingsCriteriaRequest criteriaRequest) {
        LOGGER.info("Get trainer trainings operation has been started for trainerUsername={}", trainerUsername);
        requestValidator.validate(criteriaRequest);
        authenticationService.assertAuthenticatedUser(trainerUsername, authentication);

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

    private void executeAfterCommit(Runnable notificationAction, String operationContext) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        notificationAction.run();
                    } catch (RuntimeException ex) {
                        LOGGER.error("Failed to send workload notification after commit for [{}]",
                                operationContext, ex);
                    }
                }
            });
            LOGGER.debug("Registered post-commit workload notification for [{}]", operationContext);
        } else {
            LOGGER.warn("Transaction synchronization is inactive; sending workload notification immediately for [{}]",
                    operationContext);
            try {
                notificationAction.run();
            } catch (RuntimeException ex) {
                LOGGER.error("Failed to send workload notification (no active transaction) for [{}]",
                        operationContext, ex);
            }
        }
    }


}
