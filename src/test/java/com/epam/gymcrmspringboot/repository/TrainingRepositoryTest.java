package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
@ActiveProfiles("test")
@Transactional
@DisplayName("TrainingRepository Tests")
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private UserRepository userRepository;

    private TrainingEntity trainingEntity;
    private TraineeEntity traineeEntity;
    private TrainerEntity trainerEntity;

    @BeforeEach
    void setUp() {
        // Create training type
        TrainingTypeEntity trainingType = TrainingTypeEntity.builder()
                .trainingTypeName("Yoga")
                .build();
        trainingType = trainingTypeRepository.save(trainingType);

        // Create trainer user and trainer
        UserEntity trainerUser = UserEntity.builder()
                .firstName("John")
                .lastName("Smith")
                .username("trainer.smith")
                .password("password123")
                .isActive(true)
                .build();
        trainerUser = userRepository.save(trainerUser);

        trainerEntity = TrainerEntity.builder()
                .user(trainerUser)
                .specialization(trainingType)
                .build();
        trainerEntity = trainerRepository.save(trainerEntity);

        // Create trainee user and trainee
        UserEntity traineeUser = UserEntity.builder()
                .firstName("Jane")
                .lastName("Doe")
                .username("trainee.doe")
                .password("password456")
                .isActive(true)
                .build();
        traineeUser = userRepository.save(traineeUser);

        traineeEntity = TraineeEntity.builder()
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .trainings(new ArrayList<>())
                .build();
        traineeEntity = traineeRepository.save(traineeEntity);

        // Create training
        trainingEntity = TrainingEntity.builder()
                .trainee(traineeEntity)
                .trainer(trainerEntity)
                .trainingName("Yoga Session")
                .trainingType(trainingType)
                .trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(60)
                .build();

        // Keep the bidirectional relation in-memory after both objects exist.
        traineeEntity.getTrainings().add(trainingEntity);
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations        {

        @Test
        @DisplayName("Should create and retrieve training by ID")
        void testCreateAndRetrieveTraining() {
            // Arrange & Act
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Assert
            assertNotNull(saved.getId());
            assertTrue(trainingRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should update training successfully")
        void testUpdateTraining() {
            // Arrange
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Act
            saved.setTrainingName("Advanced Yoga");
            TrainingEntity updated = trainingRepository.save(saved);

            // Assert
            assertEquals("Advanced Yoga", updated.getTrainingName());
        }

        @Test
        @DisplayName("Should delete training successfully")
        void testDeleteTraining() {
            // Arrange
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Act
            trainingRepository.delete(saved);

            // Assert
            assertFalse(trainingRepository.findById(saved.getId()).isPresent());
        }

    }

    @Nested
    @DisplayName("Cascade Delete Tests")
    class CascadeDeleteTests {

        @Test
        @DisplayName("Should delete trainee trainings when trainee is hard deleted")
        void testCascadeDeleteTrainingsOnTraineeDelete() {
            // Arrange
            TrainingEntity saved = trainingRepository.save(trainingEntity);
            Long trainingId = saved.getId();

            // Act
            traineeRepository.delete(traineeEntity);

            // Assert
            assertFalse(trainingRepository.findById(trainingId).isPresent());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain trainee relationship")
        void testMaintainTraineeRelationship() {
            // Arrange & Act
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Assert
            assertEquals("trainee.doe", saved.getTrainee().getUser().getUsername());
        }

        @Test
        @DisplayName("Should maintain trainer relationship")
        void testMaintainTrainerRelationship() {
            // Arrange & Act
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Assert
            assertEquals("trainer.smith", saved.getTrainer().getUser().getUsername());
        }

        @Test
        @DisplayName("Should maintain training type relationship")
        void testMaintainTrainingTypeRelationship() {
            // Arrange & Act
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Assert
            assertEquals("Yoga", saved.getTrainingType().getTrainingTypeName());
        }
    }

    @Nested
    @DisplayName("Field Validation Tests")
    class FieldValidationTests {

        @Test
        @DisplayName("Should persist training name correctly")
        void testPersistTrainingName() {
            // Arrange & Act
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Assert
            assertEquals("Yoga Session", saved.getTrainingName());
        }

        @Test
        @DisplayName("Should persist training date correctly")
        void testPersistTrainingDate() {
            // Arrange & Act
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Assert
            assertEquals(LocalDate.of(2024, 1, 15), saved.getTrainingDate());
        }

        @Test
        @DisplayName("Should persist training duration correctly")
        void testPersistTrainingDuration() {
            // Arrange & Act
            TrainingEntity saved = trainingRepository.save(trainingEntity);

            // Assert
            assertEquals(60, saved.getTrainingDuration());
        }
    }
}

