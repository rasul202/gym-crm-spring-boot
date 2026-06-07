package com.epam.gymcrmspringboot.mapper;

import com.epam.gymcrmspringboot.dto.response.GetTrainerProfileResponse;
import com.epam.gymcrmspringboot.dto.response.TraineeSummary;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTrainerProfileResponse;
import com.epam.gymcrmspringboot.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrainerMapper Tests")
class TrainerMapperTest {

    private TrainerMapper trainerMapper;

    @BeforeEach
    void setUp() {
        trainerMapper = Mappers.getMapper(TrainerMapper.class);
    }

    @Test
    @DisplayName("Should map TrainerEntity to GetTrainerProfileResponse")
    void shouldMapTrainerEntityToGetProfileResponse() {
        // Arrange
        TrainerEntity entity = buildTrainerEntityWithTrainings();

        // Act
        GetTrainerProfileResponse response = trainerMapper.toGetTrainerProfileResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("Yoga", response.getSpecialization());
        assertTrue(response.getIsActive());
        assertNotNull(response.getTrainees());
        assertEquals(1, response.getTrainees().size());
        assertEquals("john.doe", response.getTrainees().get(0).getUsername());
    }

    @Test
    @DisplayName("Should map TrainerEntity to UpdateTrainerProfileResponse")
    void shouldMapTrainerEntityToUpdateProfileResponse() {
        // Arrange
        TrainerEntity entity = buildTrainerEntityWithTrainings();

        // Act
        UpdateTrainerProfileResponse response = trainerMapper.toUpdateTrainerProfileResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals("jane.smith", response.getUsername());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("Yoga", response.getSpecialization());
        assertTrue(response.getIsActive());
        assertEquals(1, response.getTrainees().size());
    }

    @Test
    @DisplayName("Should map TrainerEntity to TrainerSummary")
    void shouldMapTrainerEntityToTrainerSummary() {
        // Arrange
        TrainerEntity entity = buildTrainerEntityWithTrainings();

        // Act
        TrainerSummary summary = trainerMapper.trainerEntityToTrainerSummary(entity);

        // Assert
        assertNotNull(summary);
        assertEquals("jane.smith", summary.getUsername());
        assertEquals("Jane", summary.getFirstName());
        assertEquals("Smith", summary.getLastName());
        assertEquals("Yoga", summary.getSpecialization());
    }

    @Test
    @DisplayName("Should return empty trainees list when trainings are null")
    void shouldReturnEmptyTraineesWhenTrainingsNull() {
        // Arrange
        TrainerEntity entity = TrainerEntity.builder()
                .user(UserEntity.builder().firstName("A").lastName("B").username("ab").isActive(true).build())
                .specialization(TrainingTypeEntity.builder().trainingTypeName("Yoga").build())
                .trainings(null)
                .build();

        // Act
        GetTrainerProfileResponse response = trainerMapper.toGetTrainerProfileResponse(entity);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getTrainees());
        assertTrue(response.getTrainees().isEmpty());
    }

    @Test
    @DisplayName("Should filter out inactive trainees from summaries")
    void shouldFilterOutInactiveTrainees() {
        // Arrange
        TrainerEntity entity = buildTrainerEntityWithMixedTraineeStates();

        // Act
        List<TraineeSummary> trainees = trainerMapper.toTraineeSummaries(entity);

        // Assert
        assertEquals(1, trainees.size());
        assertEquals("john.doe", trainees.get(0).getUsername());
    }

    @Test
    @DisplayName("Should return null when source trainer is null for profile mappings")
    void shouldReturnNullWhenSourceIsNull() {
        // Act
        GetTrainerProfileResponse getResponse = trainerMapper.toGetTrainerProfileResponse(null);
        UpdateTrainerProfileResponse updateResponse = trainerMapper.toUpdateTrainerProfileResponse(null);

        // Assert
        assertNull(getResponse);
        assertNull(updateResponse);
    }

    private TrainerEntity buildTrainerEntityWithTrainings() {
        UserEntity trainerUser = UserEntity.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith")
                .password("secret")
                .isActive(true)
                .build();

        UserEntity activeTraineeUser = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .isActive(true)
                .build();

        TraineeEntity activeTrainee = TraineeEntity.builder()
                .user(activeTraineeUser)
                .build();

        TrainingEntity training = TrainingEntity.builder()
                .trainee(activeTrainee)
                .trainingDate(LocalDate.of(2026, 1, 1))
                .trainingDuration(60)
                .trainingName("Morning Session")
                .build();

        return TrainerEntity.builder()
                .user(trainerUser)
                .specialization(TrainingTypeEntity.builder().trainingTypeName("Yoga").build())
                .trainings(List.of(training))
                .build();
    }

    private TrainerEntity buildTrainerEntityWithMixedTraineeStates() {
        TrainerEntity trainer = buildTrainerEntityWithTrainings();

        TraineeEntity inactiveTrainee = TraineeEntity.builder()
                .user(UserEntity.builder().username("inactive.trainee").isActive(false).build())
                .build();

        TrainingEntity inactiveTraining = TrainingEntity.builder()
                .trainee(inactiveTrainee)
                .trainingName("Evening Session")
                .trainingDate(LocalDate.of(2026, 1, 2))
                .trainingDuration(45)
                .build();

        trainer.setTrainings(List.of(trainer.getTrainings().get(0), inactiveTraining));
        return trainer;
    }
}
