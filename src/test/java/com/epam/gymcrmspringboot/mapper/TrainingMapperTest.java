package com.epam.gymcrmspringboot.mapper;

import com.epam.gymcrmspringboot.dto.response.GetTraineeTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.GetTrainerTrainingsResponse;
import com.epam.gymcrmspringboot.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TrainingMapper Tests")
class TrainingMapperTest {

    private TrainingMapper trainingMapper;

    @BeforeEach
    void setUp() {
        trainingMapper = Mappers.getMapper(TrainingMapper.class);
    }

    @Test
    @DisplayName("Should map TrainingEntity to GetTrainerTrainingsResponse")
    void shouldMapToGetTrainerTrainingsResponse() {
        // Arrange
        TrainingEntity entity = buildTrainingEntity();

        // Act
        GetTrainerTrainingsResponse response = trainingMapper.toGetTrainerTrainingsResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals("Morning Yoga", response.getTrainingName());
        assertEquals("Yoga", response.getTrainingType());
        assertEquals(LocalDate.of(2026, 5, 1), response.getTrainingDate());
        assertEquals(60, response.getTrainingDuration());
        assertEquals("trainer.user", response.getTrainerUsername());
        assertEquals("trainee.user", response.getTraineeUsername());
    }

    @Test
    @DisplayName("Should map TrainingEntity to GetTraineeTrainingsResponse")
    void shouldMapToGetTraineeTrainingsResponse() {
        // Arrange
        TrainingEntity entity = buildTrainingEntity();

        // Act
        GetTraineeTrainingsResponse response = trainingMapper.toGetTraineeTrainingsResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals("Morning Yoga", response.getTrainingName());
        assertEquals("Yoga", response.getTrainingType());
        assertEquals(LocalDate.of(2026, 5, 1), response.getTrainingDate());
        assertEquals(60, response.getTrainingDuration());
        assertEquals("trainer.user", response.getTrainerUsername());
        assertEquals("trainee.user", response.getTraineeUsername());
    }

    @Test
    @DisplayName("Should return null when source training is null")
    void shouldReturnNullWhenSourceIsNull() {
        // Act
        GetTrainerTrainingsResponse trainerResponse = trainingMapper.toGetTrainerTrainingsResponse(null);
        GetTraineeTrainingsResponse traineeResponse = trainingMapper.toGetTraineeTrainingsResponse(null);

        // Assert
        assertNull(trainerResponse);
        assertNull(traineeResponse);
    }

    private TrainingEntity buildTrainingEntity() {
        UserEntity traineeUser = UserEntity.builder()
                .username("trainee.user")
                .build();

        UserEntity trainerUser = UserEntity.builder()
                .username("trainer.user")
                .build();

        TraineeEntity trainee = TraineeEntity.builder()
                .user(traineeUser)
                .build();

        TrainerEntity trainer = TrainerEntity.builder()
                .user(trainerUser)
                .build();

        TrainingTypeEntity type = TrainingTypeEntity.builder()
                .trainingTypeName("Yoga")
                .build();

        return TrainingEntity.builder()
                .trainingName("Morning Yoga")
                .trainingType(type)
                .trainingDate(LocalDate.of(2026, 5, 1))
                .trainingDuration(60)
                .trainer(trainer)
                .trainee(trainee)
                .build();
    }
}

