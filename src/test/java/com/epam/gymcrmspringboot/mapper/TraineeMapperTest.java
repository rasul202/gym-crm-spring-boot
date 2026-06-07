package com.epam.gymcrmspringboot.mapper;

import com.epam.gymcrmspringboot.dto.response.GetTraineeProfileResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTraineeProfileResponse;
import com.epam.gymcrmspringboot.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraineeMapper Tests")
class TraineeMapperTest {

    private TraineeMapper traineeMapper;

    @BeforeEach
    void setUp() {
        traineeMapper = Mappers.getMapper(TraineeMapper.class);
    }

    @Test
    @DisplayName("Should map TraineeEntity to GetTraineeProfileResponse")
    void shouldMapToGetTraineeProfileResponse() {
        // Arrange
        TraineeEntity entity = buildTraineeEntityWithTrainings();

        // Act
        GetTraineeProfileResponse response = traineeMapper.toGetTraineeProfileResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(LocalDate.of(1995, 5, 20), response.getDateOfBirth());
        assertEquals("Main street", response.getAddress());
        assertTrue(response.getIsActive());
        assertEquals(1, response.getTrainers().size());
        assertEquals("jane.smith", response.getTrainers().get(0).getUsername());
    }

    @Test
    @DisplayName("Should map TraineeEntity to UpdateTraineeProfileResponse")
    void shouldMapToUpdateTraineeProfileResponse() {
        // Arrange
        TraineeEntity entity = buildTraineeEntityWithTrainings();

        // Act
        UpdateTraineeProfileResponse response = traineeMapper.toUpdateTraineeProfileResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals("john.doe", response.getUsername());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(LocalDate.of(1995, 5, 20), response.getDateOfBirth());
        assertEquals("Main street", response.getAddress());
        assertTrue(response.getIsActive());
        assertEquals(1, response.getTrainers().size());
    }

    @Test
    @DisplayName("Should map TrainerEntity to TrainerSummary")
    void shouldMapTrainerEntityToTrainerSummary() {
        // Arrange
        TrainerEntity trainer = TrainerEntity.builder()
                .user(UserEntity.builder().username("jane.smith").firstName("Jane").lastName("Smith").build())
                .specialization(TrainingTypeEntity.builder().trainingTypeName("Yoga").build())
                .build();

        // Act
        TrainerSummary summary = traineeMapper.trainerEntityToTrainerSummary(trainer);

        // Assert
        assertNotNull(summary);
        assertEquals("jane.smith", summary.getUsername());
        assertEquals("Jane", summary.getFirstName());
        assertEquals("Smith", summary.getLastName());
        assertEquals("Yoga", summary.getSpecialization());
    }

    @Test
    @DisplayName("Should return empty trainers list when trainings are null")
    void shouldReturnEmptyTrainersWhenTrainingsNull() {
        // Arrange
        TraineeEntity entity = TraineeEntity.builder()
                .user(UserEntity.builder().firstName("John").lastName("Doe").username("john.doe").isActive(true).build())
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .address("Main street")
                .trainings(null)
                .build();

        // Act
        GetTraineeProfileResponse response = traineeMapper.toGetTraineeProfileResponse(entity);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getTrainers());
        assertTrue(response.getTrainers().isEmpty());
    }

    @Test
    @DisplayName("Should filter inactive trainers from trainer summaries")
    void shouldFilterInactiveTrainers() {
        // Arrange
        TraineeEntity entity = buildTraineeEntityWithMixedTrainerStates();

        // Act
        List<TrainerSummary> summaries = traineeMapper.toTrainerSummaries(entity);

        // Assert
        assertEquals(1, summaries.size());
        assertEquals("jane.smith", summaries.get(0).getUsername());
    }

    @Test
    @DisplayName("Should return null when source trainee is null for profile mappings")
    void shouldReturnNullWhenSourceIsNull() {
        // Act
        GetTraineeProfileResponse getResponse = traineeMapper.toGetTraineeProfileResponse(null);
        UpdateTraineeProfileResponse updateResponse = traineeMapper.toUpdateTraineeProfileResponse(null);

        // Assert
        assertNull(getResponse);
        assertNull(updateResponse);
    }

    private TraineeEntity buildTraineeEntityWithTrainings() {
        UserEntity traineeUser = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("secret")
                .isActive(true)
                .build();

        TrainerEntity activeTrainer = TrainerEntity.builder()
                .user(UserEntity.builder().firstName("Jane").lastName("Smith").username("jane.smith").isActive(true).build())
                .specialization(TrainingTypeEntity.builder().trainingTypeName("Yoga").build())
                .build();

        TrainingEntity training = TrainingEntity.builder()
                .trainer(activeTrainer)
                .trainingName("Morning Session")
                .trainingDate(LocalDate.of(2026, 1, 1))
                .trainingDuration(60)
                .build();

        return TraineeEntity.builder()
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .address("Main street")
                .user(traineeUser)
                .trainings(List.of(training))
                .build();
    }

    private TraineeEntity buildTraineeEntityWithMixedTrainerStates() {
        TraineeEntity entity = buildTraineeEntityWithTrainings();

        TrainerEntity inactiveTrainer = TrainerEntity.builder()
                .user(UserEntity.builder().username("inactive.trainer").isActive(false).build())
                .specialization(TrainingTypeEntity.builder().trainingTypeName("Pilates").build())
                .build();

        TrainingEntity inactiveTraining = TrainingEntity.builder()
                .trainer(inactiveTrainer)
                .trainingName("Evening Session")
                .trainingDate(LocalDate.of(2026, 1, 2))
                .trainingDuration(45)
                .build();

        entity.setTrainings(List.of(entity.getTrainings().get(0), inactiveTraining));
        return entity;
    }
}
