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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
@ActiveProfiles("test")
@Transactional
@DisplayName("TrainerRepository Tests")
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    private TrainerEntity trainerEntity;
    private UserEntity userEntity;
    private TrainingTypeEntity trainingTypeEntity;
    private TraineeEntity traineeEntity;

    @BeforeEach
    void setUp() {
        trainingTypeEntity = TrainingTypeEntity.builder()
                .trainingTypeName("Yoga")
                .build();
        trainingTypeEntity = trainingTypeRepository.save(trainingTypeEntity);

        userEntity = UserEntity.builder()
                .firstName("John")
                .lastName("Smith")
                .username("John.Smith")
                .password("password123")
                .isActive(true)
                .build();
        userEntity = userRepository.save(userEntity);

        trainerEntity = TrainerEntity.builder()
                .user(userEntity)
                .specialization(trainingTypeEntity)
                .build();

        UserEntity traineeUser = UserEntity.builder()
                .firstName("Jane")
                .lastName("Doe")
                .username("jane.doe")
                .password("password456")
                .isActive(true)
                .build();
        traineeUser = userRepository.save(traineeUser);

        traineeEntity = TraineeEntity.builder()
                .user(traineeUser)
                .build();
        traineeEntity = traineeRepository.save(traineeEntity);
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create and retrieve trainer by ID")
        void testCreateAndRetrieveTrainer() {
            // Arrange & Act
            TrainerEntity saved = trainerRepository.save(trainerEntity);

            // Assert
            assertNotNull(saved.getId());
            assertTrue(trainerRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should update trainer successfully")
        void testUpdateTrainer() {
            // Arrange
            TrainerEntity saved = trainerRepository.save(trainerEntity);

            TrainingTypeEntity pilates = TrainingTypeEntity.builder()
                    .trainingTypeName("Pilates")
                    .build();
            pilates = trainingTypeRepository.save(pilates);

            // Act
            saved.setSpecialization(pilates);
            TrainerEntity updated = trainerRepository.save(saved);

            // Assert
            assertEquals("Pilates", updated.getSpecialization().getTrainingTypeName());
        }

        @Test
        @DisplayName("Should delete trainer successfully")
        void testDeleteTrainer() {
            // Arrange
            TrainerEntity saved = trainerRepository.save(trainerEntity);

            // Act
            trainerRepository.delete(saved);

            // Assert
            assertFalse(trainerRepository.findById(saved.getId()).isPresent());
        }
    }

    @Nested
    @DisplayName("findByUserUsername Tests")
    class FindByUserUsernameTests {

        @Test
        @DisplayName("Should find trainer by username")
        void testFindTrainerByUsername() {
            // Arrange
            trainerRepository.save(trainerEntity);

            // Act
            Optional<TrainerEntity> result = trainerRepository.findByUserUsername("John.Smith");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("John.Smith", result.get().getUser().getUsername());
        }

        @Test
        @DisplayName("Should return empty Optional when trainer not found")
        void testFindByUsernameNotFound() {
            // Act
            Optional<TrainerEntity> result = trainerRepository.findByUserUsername("NonExistent");

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should find trainer regardless of active status")
        void testFindByUsernameIgnoresActiveStatus() {
            // Arrange
            trainerRepository.save(trainerEntity);
            userEntity.setIsActive(false);
            userRepository.save(userEntity);

            // Act
            Optional<TrainerEntity> result = trainerRepository.findByUserUsername("John.Smith");

            // Assert
            assertTrue(result.isPresent());
        }
    }

    @Nested
    @DisplayName("findByUserUsernameInAndUserIsActiveTrue Tests")
    class FindByUserUsernameInAndUserIsActiveTrueTests {

        @Test
        @DisplayName("Should find active trainers by usernames")
        void testFindActiveTrainersByUsernames() {
            // Arrange
            trainerRepository.save(trainerEntity);

            UserEntity anotherUser = UserEntity.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .username("Jane.Doe")
                    .password("password456")
                    .isActive(true)
                    .build();
            anotherUser = userRepository.save(anotherUser);

            TrainerEntity anotherTrainer = TrainerEntity.builder()
                    .user(anotherUser)
                    .specialization(trainingTypeEntity)
                    .build();
            trainerRepository.save(anotherTrainer);

            // Act
            List<TrainerEntity> result = trainerRepository
                    .findByUserUsernameInAndUserIsActiveTrue(List.of("John.Smith", "Jane.Doe"));

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should not include inactive trainers")
        void testDoesNotIncludeInactiveTrainers() {
            // Arrange
            trainerRepository.save(trainerEntity);

            UserEntity inactiveUser = UserEntity.builder()
                    .firstName("Jane")
                    .lastName("Doe")
                    .username("Jane.Doe")
                    .password("password456")
                    .isActive(false)
                    .build();
            inactiveUser = userRepository.save(inactiveUser);

            TrainerEntity inactiveTrainer = TrainerEntity.builder()
                    .user(inactiveUser)
                    .specialization(trainingTypeEntity)
                    .build();
            trainerRepository.save(inactiveTrainer);

            // Act
            List<TrainerEntity> result = trainerRepository
                    .findByUserUsernameInAndUserIsActiveTrue(List.of("John.Smith", "Jane.Doe"));

            // Assert
            assertEquals(1, result.size());
            assertEquals("John.Smith", result.get(0).getUser().getUsername());
        }

        @Test
        @DisplayName("Should return empty list when no trainers found")
        void testReturnsEmptyListWhenNoTrainersFound() {
            // Act
            List<TrainerEntity> result = trainerRepository
                    .findByUserUsernameInAndUserIsActiveTrue(List.of("NonExistent1", "NonExistent2"));

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findAvailableTrainersForTrainee Tests")
    class FindAvailableTrainersForTraineeTests {

        @Test
        @DisplayName("Should find active trainer when trainee exists and no assignments")
        void testFindAvailableTrainersForTrainee() {
            // Arrange
            trainerRepository.save(trainerEntity);

            // Act
            List<TrainerEntity> result = trainerRepository
                    .findAvailableTrainersForTrainee("jane.doe");

            // Assert
            assertEquals(1, result.size());
            assertEquals("John.Smith", result.get(0).getUser().getUsername());
        }

        @Test
        @DisplayName("Should not return trainer already assigned to trainee")
        void testDoesNotIncludeAssignedTrainer() {
            // Arrange
            TrainerEntity savedTrainer = trainerRepository.save(trainerEntity);
            TrainingEntity assignment = TrainingEntity.builder()
                    .trainee(traineeEntity)
                    .trainer(savedTrainer)
                    .trainingName("Trainer Link")
                    .trainingType(trainingTypeEntity)
                    .trainingDate(java.time.LocalDate.of(2026, 1, 1))
                    .trainingDuration(60)
                    .build();
            trainingRepository.save(assignment);

            // Act
            List<TrainerEntity> result = trainerRepository
                    .findAvailableTrainersForTrainee("jane.doe");

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should not include inactive trainers in available list")
        void testDoesNotIncludeInactiveInAvailableList() {
            // Arrange
            userEntity.setIsActive(false);
            userRepository.save(userEntity);
            trainerRepository.save(trainerEntity);

            // Act
            List<TrainerEntity> result = trainerRepository
                    .findAvailableTrainersForTrainee("jane.doe");

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {

        @Test
        @DisplayName("Should maintain user relationship")
        void testMaintainUserRelationship() {
            // Arrange & Act
            TrainerEntity saved = trainerRepository.save(trainerEntity);

            // Assert
            assertEquals("John.Smith", saved.getUser().getUsername());
            assertEquals(userEntity.getId(), saved.getUser().getId());
        }

        @Test
        @DisplayName("Should maintain specialization relationship")
        void testMaintainSpecializationRelationship() {
            // Arrange & Act
            TrainerEntity saved = trainerRepository.save(trainerEntity);

            // Assert
            assertEquals("Yoga", saved.getSpecialization().getTrainingTypeName());
            assertEquals(trainingTypeEntity.getId(), saved.getSpecialization().getId());
        }

    }
}

