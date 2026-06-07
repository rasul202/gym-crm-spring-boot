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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
@ActiveProfiles("test")
@Transactional
@DisplayName("TraineeRepository Tests")
class TraineeRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    private TraineeEntity traineeEntity;
    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("password123")
                .isActive(true)
                .build();
        userEntity = userRepository.save(userEntity);

        traineeEntity = TraineeEntity.builder()
                .user(userEntity)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .build();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create and retrieve trainee by ID")
        void testCreateAndRetrieveTrainee() {
            // Arrange & Act
            TraineeEntity saved = traineeRepository.save(traineeEntity);

            // Assert
            assertNotNull(saved.getId());
            assertTrue(traineeRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should update trainee successfully")
        void testUpdateTrainee() {
            // Arrange
            TraineeEntity saved = traineeRepository.save(traineeEntity);

            // Act
            saved.setAddress("456 Oak Ave");
            TraineeEntity updated = traineeRepository.save(saved);

            // Assert
            assertEquals("456 Oak Ave", updated.getAddress());
        }

        @Test
        @DisplayName("Should delete trainee successfully")
        void testDeleteTrainee() {
            // Arrange
            TraineeEntity saved = traineeRepository.save(traineeEntity);

            // Act
            traineeRepository.delete(saved);

            // Assert
            assertFalse(traineeRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should retrieve all trainees")
        void testFindAllTrainees() {
            // Arrange
            traineeRepository.save(traineeEntity);

            UserEntity anotherUser = UserEntity.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .username("Jane.Smith")
                    .password("password456")
                    .build();
            anotherUser = userRepository.save(anotherUser);

            TraineeEntity anotherTrainee = TraineeEntity.builder()
                    .user(anotherUser)
                    .dateOfBirth(LocalDate.of(1992, 2, 2))
                    .address("456 Oak Ave")
                    .build();
            traineeRepository.save(anotherTrainee);

            // Act
            var allTrainees = traineeRepository.findAll();

            // Assert
            assertEquals(2, allTrainees.size());
        }
    }

    @Nested
    @DisplayName("findByUserUsernameAndUserIsActiveTrue Tests")
    class FindByUserUsernameAndUserIsActiveTrueTests {

        @Test
        @DisplayName("Should find active trainee by username")
        void testFindActiveTraineeByUsername() {
            // Arrange
            traineeRepository.save(traineeEntity);

            // Act
            Optional<TraineeEntity> result = traineeRepository
                    .findByUserUsernameAndUserIsActiveTrue("John.Doe");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("John.Doe", result.get().getUser().getUsername());
        }

        @Test
        @DisplayName("Should not find trainee when user is inactive")
        void testDoesNotFindTraineeWhenUserInactive() {
            // Arrange
            traineeRepository.save(traineeEntity);
            userEntity.setIsActive(false);
            userRepository.save(userEntity);

            // Act
            Optional<TraineeEntity> result = traineeRepository
                    .findByUserUsernameAndUserIsActiveTrue("John.Doe");

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty Optional when trainee not found")
        void testFindByUsernameNotFound() {
            // Act
            Optional<TraineeEntity> result = traineeRepository
                    .findByUserUsernameAndUserIsActiveTrue("NonExistent");

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should find correct trainee among multiple trainees")
        void testFindCorrectTraineeAmongMultiple() {
            // Arrange
            traineeRepository.save(traineeEntity);

            UserEntity anotherUser = UserEntity.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .username("Jane.Smith")
                    .password("password456")
                    .build();
            anotherUser = userRepository.save(anotherUser);

            TraineeEntity anotherTrainee = TraineeEntity.builder()
                    .user(anotherUser)
                    .dateOfBirth(LocalDate.of(1992, 2, 2))
                    .build();
            traineeRepository.save(anotherTrainee);

            // Act
            Optional<TraineeEntity> result = traineeRepository
                    .findByUserUsernameAndUserIsActiveTrue("John.Doe");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("John.Doe", result.get().getUser().getUsername());
        }
    }

    @Nested
    @DisplayName("findByUserUsernameAndUserIsActiveTrueWithTrainings Tests")
    class FindByUserUsernameAndUserIsActiveTrueWithTrainingsTests {

        @Test
        @DisplayName("Should find active trainee by username without trainings fetch")
        void testFindActiveTraineeByUsernameWithTrainings() {
            // Arrange
            TraineeEntity savedTrainee = traineeRepository.save(traineeEntity);

            TrainingTypeEntity type = TrainingTypeEntity.builder().trainingTypeName("Yoga").build();
            type = trainingTypeRepository.save(type);

            UserEntity trainerUser = UserEntity.builder()
                    .firstName("Trainer")
                    .lastName("User")
                    .username("trainer.user")
                    .password("password")
                    .isActive(true)
                    .build();
            trainerUser = userRepository.save(trainerUser);

            TrainerEntity trainer = TrainerEntity.builder()
                    .user(trainerUser)
                    .specialization(type)
                    .build();
            trainer = trainerRepository.save(trainer);

            trainingRepository.save(TrainingEntity.builder()
                    .trainee(savedTrainee)
                    .trainer(trainer)
                    .trainingName("Yoga Session")
                    .trainingType(type)
                    .trainingDate(LocalDate.of(2026, 1, 1))
                    .trainingDuration(60)
                    .build());

            // Ensure data is persisted before query
            traineeRepository.flush();

            // Act
            Optional<TraineeEntity> result = traineeRepository
                    .findByUserUsernameAndUserIsActiveTrueWithTrainings("John.Doe");

            // Assert
            assertTrue(result.isPresent());
            TraineeEntity trainee = result.get();
            assertEquals("John.Doe", trainee.getUser().getUsername());
            assertNull(trainee.getTrainings());
        }
    }
}

